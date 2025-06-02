/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.diagnosis;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Migrates existing Diagnosis from the obs table to the new encounter_diagnosis table by getting all existing diagnosis
 * using the emrapi DiagnosisService and then saves them using the openmrs api DiagnosisService.
 */
public class MigrateDiagnosis {
	
	private final Logger log = LoggerFactory.getLogger(MigrateDiagnosis.class);
	
	private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
	
	private final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	
	// Flag that identifies whether at least one Diagnosis was migrated
	private final AtomicBoolean migratedAtLeastOne = new AtomicBoolean(false);
	
	/**
	 * Creates a new Diagnosis using the Diagnosis model from the openmrs-core and saves it using the new DiagnosisService
	 * @return true if at least one Diagnosis was migrated
	 */
	public Boolean migrate(DiagnosisMetadata diagnosisMetadata, EmrApiProperties emrApiProperties) {
		try {
			log.info("Starting migration of diagnoses from obs to encounter_diagnosis table");
			processPatientsInBatch(diagnosisMetadata, emrApiProperties);
		}
		finally {
			executorService.shutdown();
			try {
				if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
					executorService.shutdownNow();
				}
			}
			catch (InterruptedException e) {
				executorService.shutdownNow();
				Thread.currentThread().interrupt();
			}
		}
		
		return migratedAtLeastOne.get();
	}
	
	/**
	 * Processes patients in batches to migrate their diagnoses
	 * @param diagnosisMetadata metadata containing information about the diagnosis migration
	 */
	private void processPatientsInBatch(DiagnosisMetadata diagnosisMetadata, EmrApiProperties emrApiProperties) {
		// This is to avoid loading all patients into memory at once
		// and to allow processing them in manageable chunks.
		// The batch size is configurable via the global property emrapi.diagnosisMigrationBatchSize, defaulting to 500.
		int batchSize = Integer.parseInt(Context.getAdministrationService().getGlobalProperty("emrapi.diagnosisMigrationBatchSize", "500"));
		if (batchSize <= 0) {
			throw new IllegalArgumentException("emrapi.diagnosisMigrationBatchSize must be a positive integer");
		}
		int startIndex = 0;
		// Loop until no more patients are found with the specified diagnosis
		while (true) {
			List<Integer> patientIds = getDeprecatedDiagnosisService().getPatientsWithDiagnosis(diagnosisMetadata, startIndex, batchSize);
			if (patientIds == null || patientIds.isEmpty()) {
				log.info("No more patients found with the specified diagnosis. Migration completed.");
				break;
			}
			log.info("Processing batch of {} patients starting from index {}", patientIds.size(), startIndex);
			// Submit tasks for each patient in the current batch
			List<Future<?>> tasks = patientIds.stream()
					.map(ptId -> executorService.submit(() -> migrateDiagnosisForSinglePatient(ptId, diagnosisMetadata, emrApiProperties)))
					.collect(Collectors.toList());
			
			// Wait for all tasks in the current batch to complete
			waitForPatientDiagnosisMigrationTasksToComplete(tasks);
			startIndex += batchSize; // Move to the next batch
		}
	}
	
	/**
	 * Waits for all patient diagnosis migration tasks in the current batch to complete
	 * @param tasks the list of tasks to wait for
	 */
	private void waitForPatientDiagnosisMigrationTasksToComplete(List<Future<?>> tasks) {
		if (tasks == null || tasks.isEmpty()) {
			log.warn("No tasks to wait for. Skipping waiting for patient diagnosis migration tasks.");
			return;
		}
		tasks.forEach(task -> {
			try {
				task.get();
			} catch (ExecutionException | InterruptedException e) {
				throw new DiagnosisMigrationException("Error while migrating diagnoses for a patient", e);
			}
		});
	}
	
	/**
	 * Migrates diagnoses for a single patient by fetching existing diagnoses from the emrapi service
	 * and saving them using the new DiagnosisService.
	 *
	 * @param patientId the ID of the patient whose diagnoses are to be migrated
	 */
	private void migrateDiagnosisForSinglePatient(Integer patientId, DiagnosisMetadata diagnosisMetadata, EmrApiProperties emrApiProperties) {
		Context.openSession();
		DiagnosisUtils.getRequiredPrivilegesForDiagnosisMigration().forEach(Context::addProxyPrivilege);
		Patient patient = Context.getPatientService().getPatient(patientId);
		try {
			List<Diagnosis> emrapiDiagnosis = getDeprecatedDiagnosisService().getDiagnoses(patient, null,
					diagnosisMetadata, emrApiProperties);
			log.error("emrapi diagnosis size: {}", emrapiDiagnosis.size());
			List<org.openmrs.Diagnosis> diagnoses = emrapiDiagnosis.stream()
					.map(DiagnosisUtils::convert)
					.collect(Collectors.toList());
			if (diagnoses.isEmpty()) {
				log.warn("No diagnoses found for patient with ID: {}. Skipping migration.", patientId);
				return;
			}
			diagnoses.forEach(getNewDiagnosisService()::save);
			// Void the existing Obs for the migrated diagnoses
			emrapiDiagnosis.forEach(this::voidObsForEmrApiDiagnosis);
			migratedAtLeastOne.set(true);
		} catch (Exception e) {
			throw new DiagnosisMigrationException("Error while migrating diagnoses for patient with UUID: " + patient.getUuid(), e);
		} finally {
			DiagnosisUtils.getRequiredPrivilegesForDiagnosisMigration().forEach(Context::removeProxyPrivilege);
			Context.closeSession();
		}
	}

	/**
	 * Voids the existing Obs for the given emrapi Diagnosis.
	 * If the Obs is a grouping Obs, it also voids all its children.
	 *
	 * @param emrapiDiagnosis the emrapi Diagnosis containing the Obs to be voided
	 */
	private void voidObsForEmrApiDiagnosis(Diagnosis emrapiDiagnosis) {
		ObsService obsService = Context.getObsService();
		Obs obs = emrapiDiagnosis.getExistingObs();
		if (obs != null) {
			obs.setVoided(true);
			obs.setVoidedBy(Context.getAuthenticatedUser());
			obs.setDateVoided(new Date());
			// If the Obs is a grouping Obs, void all its children as well
			if (obs.isObsGrouping()) {
				List<Obs> affectedObsChildren = new ArrayList<>(obs.getGroupMembers());
				for (Obs child : affectedObsChildren) {
					child.setVoided(true);
					child.setVoidedBy(Context.getAuthenticatedUser());
					child.setDateVoided(new Date());
					obsService.voidObs(child,
							"Voided this Obs due to parent Obs migration to new encounter_diagnosis table");
				}
			}
			obsService.voidObs(obs, "Voided this Obs due to its migration to new encounter_diagnosis table");
		}
	}

	/**
	 * Gets the old deprecated diagnosis service found in the emrapi module. 
	 * The one which was used before platform 2.2
	 * 
	 * @return the deprecated diagnosis service
	 */
	public static ObsGroupDiagnosisService getDeprecatedDiagnosisService() {
		return Context.getRegisteredComponent("obsGroupDiagnosisService", ObsGroupDiagnosisService.class);
	}
	
	/**
	 * Gets the new diagnosis service found in the openmrs-core module.
	 * The one which was introduced in platform 2.2
	 *
	 * @return the new diagnosis service
	 */
	public static org.openmrs.api.DiagnosisService getNewDiagnosisService() {
		return Context.getService(org.openmrs.api.DiagnosisService.class);
	}
}
