/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.emrapi.encounter;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Obs;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public class DiagnosisMapper {
    private final ConceptMapper conceptMapper = new ConceptMapper();

    public EncounterTransaction.Diagnosis map(Obs obs, DiagnosisMetadata diagnosisMetadata) {
        Diagnosis diagnosis = diagnosisMetadata.toDiagnosis(obs);
        EncounterTransaction.Diagnosis encounterDiagnosis = new EncounterTransaction.Diagnosis();
        encounterDiagnosis.setCertainty(String.valueOf(diagnosis.getCertainty()));
        CodedOrFreeTextAnswer codedOrFreeTextAnswer = diagnosis.getDiagnosis();
        if(StringUtils.isNotBlank(codedOrFreeTextAnswer.getNonCodedAnswer())) {
            encounterDiagnosis.setFreeTextAnswer(codedOrFreeTextAnswer.getNonCodedAnswer());
        } else {
            encounterDiagnosis.setCodedAnswer(conceptMapper.map(codedOrFreeTextAnswer.getCodedAnswer()));
        }
        encounterDiagnosis.setOrder(String.valueOf(diagnosis.getOrder()));
        encounterDiagnosis.setDiagnosisDate(obs.getObsDatetime());
        encounterDiagnosis.setExistingObs(diagnosis.getExistingObs() != null ? diagnosis.getExistingObs().getUuid() : null);
        return encounterDiagnosis;
    }
}
