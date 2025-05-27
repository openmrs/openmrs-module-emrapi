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

import org.openmrs.CodedOrFreeText;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
	public Boolean migrate(DiagnosisMetadata diagnosisMetadata) {
		try {
			log.info("Starting migration of diagnoses from obs to encounter_diagnosis table");
			processPatientsInBatch(diagnosisMetadata);
		} finally {
			executorService.shutdown();
			try {
				if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
					executorService.shutdownNow();
				}
			} catch (InterruptedException e) {
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
	private void processPatientsInBatch(DiagnosisMetadata diagnosisMetadata) {
		List<Integer> patientIds = getDeprecatedDiagnosisService().getPatientsWithDiagnosis(diagnosisMetadata);
		
		while (patientIds != null && !patientIds.isEmpty()) {
			List<Future<?>> tasks = patientIds.stream()
					.map(ptId -> executorService.submit(() -> migrateDiagnosisForSinglePatient(ptId)))
					.collect(Collectors.toList());
			// Wait for all tasks in the current batch to complete
			waitForPatientDiagnosisMigrationTasksToComplete(tasks);
			patientIds = getDeprecatedDiagnosisService().getPatientsWithDiagnosis(diagnosisMetadata);
		}
	}
	
	/**
	 * Waits for all patient diagnosis migration tasks in the current batch to complete
	 * @param tasks the list of tasks to wait for
	 */
	private void waitForPatientDiagnosisMigrationTasksToComplete(List<Future<?>> tasks) {
		tasks.forEach(task -> {
			try {
				task.get();
			} catch (ExecutionException | InterruptedException e) {
				log.error("Error during patient diagnosis migration", e.getCause());
			}
		});
	}
	
	private void migrateDiagnosisForSinglePatient(Integer patientId) {
		Context.openSession();
		DiagnosisUtils.getRequiredPrivilegesForDiagnosisMigration().forEach(Context::addProxyPrivilege);
		try {
			Patient patient = Context.getPatientService().getPatient(patientId);
			List<Diagnosis> emrapiDiagnoses = getDeprecatedDiagnosisService().getDiagnoses(patient, null);
			List<org.openmrs.Diagnosis> diagnoses = convert(emrapiDiagnoses);
			if (diagnoses.isEmpty()) {
				log.warn("No diagnoses found for patient with ID: {}. Skipping migration.", patientId);
				return;
			}
			diagnoses.forEach(getNewDiagnosisService()::save);
			migratedAtLeastOne.set(true);
		} catch (Exception e) {
			log.error("Failed to migrate diagnoses for patient with ID: {}", patientId, e);
			throw new RuntimeException(e);
		}
		finally {
			DiagnosisUtils.getRequiredPrivilegesForDiagnosisMigration().forEach(Context::removeProxyPrivilege);
			Context.closeSession();
		}
	}

	/**
	 * Converts a list of emrapi diagnosis objects to a list of core diagnosis objects
	 * @param emrapiDiagnoses list of emrapi diagnosis
	 * @return a list of core diagnosis objects.
	 */
	private List<org.openmrs.Diagnosis> convert(List<Diagnosis> emrapiDiagnoses) {
		List<org.openmrs.Diagnosis> coreDiagnoses = new ArrayList<org.openmrs.Diagnosis>();
		
		for (Diagnosis emrapiDiagnosis : emrapiDiagnoses) {
			org.openmrs.Diagnosis coreDiagnosis = new org.openmrs.Diagnosis();
			Obs obs = emrapiDiagnosis.getExistingObs();
			coreDiagnosis.setEncounter(obs.getEncounter());
			coreDiagnosis.setPatient((Patient)obs.getPerson());
			coreDiagnosis.setDiagnosis(new CodedOrFreeText(emrapiDiagnosis.getDiagnosis().getCodedAnswer(),
					emrapiDiagnosis.getDiagnosis().getSpecificCodedAnswer(), emrapiDiagnosis.getDiagnosis().getNonCodedAnswer()));
			coreDiagnosis.setCertainty(emrapiDiagnosis.getCertainty() == Diagnosis.Certainty.CONFIRMED ? ConditionVerificationStatus.CONFIRMED : ConditionVerificationStatus.PROVISIONAL);
			coreDiagnosis.setCreator(obs.getCreator());
			coreDiagnosis.setDateCreated(obs.getDateCreated());
			coreDiagnosis.setVoided(obs.getVoided());
			coreDiagnosis.setRank(1);
			if (obs.getVoided()) {
				coreDiagnosis.setVoidedBy(obs.getVoidedBy());
				coreDiagnosis.setDateVoided(obs.getDateVoided());
				coreDiagnosis.setVoidReason(obs.getVoidReason());
			}
			obs.setVoided(true);
			if (obs.isObsGrouping()) {
				List<Obs> affectedObsChildren = new ArrayList<Obs>(obs.getGroupMembers());
				for (Obs child : affectedObsChildren) {
					child.setVoided(true);
					child.setVoidReason("Migrated parent to the new encounter_diagnosis table");
				}
				
			}
			
			Context.getObsService().saveObs(obs, "Voided this Obs due to its migration to new encounter_diagnosis table");
			coreDiagnoses.add(coreDiagnosis);

		}
		return coreDiagnoses;
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
