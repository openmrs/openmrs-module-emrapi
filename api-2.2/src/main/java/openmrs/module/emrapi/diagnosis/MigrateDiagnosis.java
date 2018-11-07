/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package openmrs.module.emrapi.diagnosis;

import java.util.ArrayList;
import java.util.List;
import org.openmrs.CodedOrFreeText;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.module.emrapi.diagnosis.DiagnosisServiceImpl;
import org.openmrs.module.emrapi.visit.EmrVisitService;

/**
 * Migrates existing Diagnosis from the obs table to the new encounter_diagnosis table by getting all existing diagnosis
 * using the emrapi DiagnosisService and then saves them using the openmrs api DiagnosisService.
 */
public class MigrateDiagnosis {
	
	/**
	 * Creates new Diagnosis using the Diagnosis model from the openmrs-core and saves it using the new DiagnosisService
	 * @return true if at least one Diagnosis was migrated
	 */
	public Boolean migrate(DiagnosisMetadata diagnosisMetadata) {
		// Flag that identifies whether atleast one Diagnosis was migrated
		Boolean migratedAtleastOneEncounterDiagosis = false;
		
		EmrVisitService emrVisitService = Context.getService(EmrVisitService.class);
		DiagnosisService oldDiagnosisService = getDeprecatedDiagnosisService();
		
		org.openmrs.api.DiagnosisService newDiagnosisService = Context.getService(org.openmrs.api.DiagnosisService.class);
		List<Integer> patientsIds = emrVisitService.getAllPatientsWithDiagnosis(diagnosisMetadata);
		
		for (int id : patientsIds) {
			Patient patient = Context.getPatientService().getPatient(id);
			List<org.openmrs.Diagnosis> diagnoses = convert(oldDiagnosisService.getDiagnoses(patient, null));
			
			for (org.openmrs.Diagnosis diagnosis : diagnoses) {
				newDiagnosisService.save(diagnosis);
				migratedAtleastOneEncounterDiagosis = true;
			}
		}
		return migratedAtleastOneEncounterDiagosis;
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
	public static DiagnosisService getDeprecatedDiagnosisService() {
		DiagnosisService oldDiagnosisService = new DiagnosisServiceImpl();
		((DiagnosisServiceImpl)oldDiagnosisService).setEncounterService(Context.getEncounterService());
		((DiagnosisServiceImpl)oldDiagnosisService).setObsService(Context.getObsService());
		((DiagnosisServiceImpl)oldDiagnosisService).setEmrApiProperties(Context.getRegisteredComponent("emrApiProperties", EmrApiProperties.class));
		return oldDiagnosisService;
	}
}
