/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.util;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.CodedOrFreeText;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.module.emrapi.visit.EmrVisitService;

/**
 * Migrates existing Diagnosis from the obs table to the new encounter_diagnosis table by getting all existing diagnosis
 * using the emrapi DiagnosisService and then saves them using the openmrs api DiagnosisService.
 */
public class MigrateDiagnosis {

	/**
	 * Creates new Diagnosis using the Diagnosis model from the openmrs-core and saves it using the new DiagnosisService
	 *
	 */
	public void migrate(DiagnosisMetadata diagnosisMetadata) {
		EmrVisitService emrVisitService = Context.getService(EmrVisitService.class);

		DiagnosisService oldDiagnosisService = Context.getService(DiagnosisService.class);

		org.openmrs.api.DiagnosisService newDiagnosisService = Context.getService(org.openmrs.api.DiagnosisService.class);

		List<Diagnosis> allEmrapiDiagnosis = new ArrayList<Diagnosis>();

		List<Integer> patientsIds = emrVisitService.getAllPatientsWithDiagnosis(diagnosisMetadata);

		for (int id: patientsIds) {
			Patient patient= Context.getPatientService().getPatient(id);

			//add all diagnoses of Patient patient to list of all emrapiDiagnosis
			allEmrapiDiagnosis.addAll(oldDiagnosisService.getDiagnoses(patient, null));
		}

		List<org.openmrs.Diagnosis> diagnoses = convert(allEmrapiDiagnosis);

		for (org.openmrs.Diagnosis diagnosis:diagnoses) {

			newDiagnosisService.save(diagnosis);
		}
	}

	/**
	 * Converts a list of emrapi diagnosis objects to a list of core diagnosis objects
	 * @param emrapiDiagnoses list of emrapi diagnosis
	 * @return a list of core diagnosis objects.
	 */
	private List<org.openmrs.Diagnosis> convert(List<Diagnosis> emrapiDiagnoses) {
		List<org.openmrs.Diagnosis> coreDiagnoses = new ArrayList<org.openmrs.Diagnosis>();

		for(Diagnosis emrapiDiagnosis: emrapiDiagnoses) {
			org.openmrs.Diagnosis coreDiagnosis = new org.openmrs.Diagnosis();

			Obs obs = emrapiDiagnosis.getExistingObs();

			coreDiagnosis.setEncounter(obs.getEncounter());
			coreDiagnosis.setPatient((Patient)obs.getPerson());
			coreDiagnosis.setDiagnosis(new CodedOrFreeText(emrapiDiagnosis.getDiagnosis().getCodedAnswer(),
					emrapiDiagnosis.getDiagnosis().getSpecificCodedAnswer(), emrapiDiagnosis.getDiagnosis().getNonCodedAnswer()));
			coreDiagnosis.setCertainty(emrapiDiagnosis.getCertainty() == Diagnosis.Certainty.CONFIRMED ? ConditionVerificationStatus.CONFIRMED: ConditionVerificationStatus.PROVISIONAL);
			coreDiagnosis.setCreator(obs.getCreator());
			coreDiagnosis.setDateCreated(obs.getDateCreated());
			coreDiagnosis.setVoided(obs.getVoided());
			coreDiagnosis.setRank(1);
			if(obs.getVoided()) {
				coreDiagnosis.setVoidedBy(obs.getVoidedBy());
				coreDiagnosis.setDateVoided(obs.getDateVoided());
				coreDiagnosis.setVoidReason(obs.getVoidReason());
			}

			//Void the emrapi diagnosis obs after copying over its contents
			obs.setVoided(true);
			obs.setVoidReason("Migrated to the new encounter_diagnosis table");

			coreDiagnoses.add(coreDiagnosis);

		}
		return coreDiagnoses;
	}
}
