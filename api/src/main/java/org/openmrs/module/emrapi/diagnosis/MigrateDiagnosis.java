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

/**
 * Migrates existing Diagnosis from the obs table to the new encounter_diagnosis table by getting all existing diagnosis
 * using the emrapi DiagnosisService and then saves them using the openmrs api DiagnosisService.
 */
public class MigrateDiagnosis {
	
	private static final int BATCH_SIZE = 10;
	
	private static final Logger log = LoggerFactory.getLogger(MigrateDiagnosis.class);
	
	/**
	 * Creates new Diagnosis using the Diagnosis model from the openmrs-core and saves it using the new DiagnosisService
	 * @return true if at least one Diagnosis was migrated
	 */
	public Boolean migrate(DiagnosisMetadata diagnosisMetadata) {
		// Flag that identifies whether at least one Diagnosis was migrated
		boolean migratedAtleastOneEncounterDiagosis = false;

		ObsGroupDiagnosisService oldDiagnosisService = getDeprecatedDiagnosisService();
		
		org.openmrs.api.DiagnosisService newDiagnosisService = Context.getService(org.openmrs.api.DiagnosisService.class);
		List<Integer> patientsIds = oldDiagnosisService.getAllPatientsWithDiagnosis(diagnosisMetadata);
		
		// Process patients in batches
		for (int i = 0; i < patientsIds.size(); i += BATCH_SIZE) {
			int endIndex = Math.min(i + BATCH_SIZE, patientsIds.size());
			List<Integer> patientBatch = patientsIds.subList(i, endIndex);
			
			for (int id : patientBatch) {
				Patient patient = Context.getPatientService().getPatient(id);
				List<org.openmrs.Diagnosis> diagnoses = convert(oldDiagnosisService.getDiagnoses(patient, null));
				
				for (org.openmrs.Diagnosis diagnosis : diagnoses) {
					newDiagnosisService.save(diagnosis);
					migratedAtleastOneEncounterDiagosis = true;
				}
			}
			
			// Flush and clear session after each batch
			Context.flushSession();
			Context.clearSession();
			
			log.info("Processed {} of {} patients", endIndex, patientsIds.size());
		}
		return migratedAtleastOneEncounterDiagosis;
	}

	/**
	 * Converts a list of emrapi diagnosis objects to a list of core diagnosis objects
	 * @param emrapiDiagnoses list of emrapi diagnosis
	 * @return a list of core diagnosis objects.
	 */
	private List<org.openmrs.Diagnosis> convert(List<Diagnosis> emrapiDiagnoses) {
		List<org.openmrs.Diagnosis> coreDiagnoses = new ArrayList<>();
		
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
				List<Obs> affectedObsChildren = new ArrayList<>(obs.getGroupMembers());
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
}
