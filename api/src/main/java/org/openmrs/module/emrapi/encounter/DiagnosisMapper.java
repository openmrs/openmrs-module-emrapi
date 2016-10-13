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
        if(existingObs != null) {
            encounterDiagnosis.setDiagnosisDateTime(existingObs.getObsDatetime());
            encounterDiagnosis.setExistingObs(existingObs.getUuid());

            Set<EncounterProvider> encounterProviders = existingObs.getEncounter().getEncounterProviders();
            encounterDiagnosis.setProviders(encounterProviderMapper.convert(encounterProviders));
            encounterDiagnosis.setComments(existingObs.getComment());
        }

        return encounterDiagnosis;
    }
}
