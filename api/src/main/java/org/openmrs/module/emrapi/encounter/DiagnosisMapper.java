/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.encounter;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DiagnosisMapper {
	
	private final ConceptMapper conceptMapper = new ConceptMapper();
	
	private final EncounterProviderMapper encounterProviderMapper = new EncounterProviderMapper();
	
	public EncounterTransaction.Diagnosis map(Obs obs, DiagnosisMetadata diagnosisMetadata) {
		Diagnosis diagnosis = diagnosisMetadata.toDiagnosis(obs);
		return convert(diagnosis);
	}
	
	public List<EncounterTransaction.Diagnosis> convert(List<Diagnosis> pastDiagnoses) {
		List<EncounterTransaction.Diagnosis> pastEncounterDiagnoses = new ArrayList<EncounterTransaction.Diagnosis>();
		for (Diagnosis diagnosis : pastDiagnoses) {
			pastEncounterDiagnoses.add(convert(diagnosis));
		}
		return pastEncounterDiagnoses;
	}
	
	public EncounterTransaction.Diagnosis convert(Diagnosis diagnosis) {
		EncounterTransaction.Diagnosis encounterDiagnosis = new EncounterTransaction.Diagnosis();
		encounterDiagnosis.setCertainty(String.valueOf(diagnosis.getCertainty()));
		CodedOrFreeTextAnswer codedOrFreeTextAnswer = diagnosis.getDiagnosis();
		if (StringUtils.isNotBlank(codedOrFreeTextAnswer.getNonCodedAnswer())) {
			encounterDiagnosis.setFreeTextAnswer(codedOrFreeTextAnswer.getNonCodedAnswer());
		} else {
			encounterDiagnosis.setCodedAnswer(conceptMapper.map(codedOrFreeTextAnswer.getCodedAnswer()));
		}
		encounterDiagnosis.setOrder(String.valueOf(diagnosis.getOrder()));
		Obs existingObs = diagnosis.getExistingObs();
		if (existingObs != null) {
			encounterDiagnosis.setDiagnosisDateTime(existingObs.getObsDatetime());
			encounterDiagnosis.setExistingObs(existingObs.getUuid());
			
			Set<EncounterProvider> encounterProviders = existingObs.getEncounter().getEncounterProviders();
			encounterDiagnosis.setProviders(encounterProviderMapper.convert(encounterProviders));
			encounterDiagnosis.setComments(existingObs.getComment());
		}
		
		return encounterDiagnosis;
	}
}
