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

import org.openmrs.Obs;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Set;

public class EncounterObservationsMapper {
    private  ObservationMapper observationMapper;
    private  DiagnosisMapper diagnosisMapper;
    private  DispositionMapper dispositionMapper;
    private EmrApiProperties emrApiProperties;
    private DiagnosisMetadata diagnosisMetadata;


    public EncounterObservationsMapper(ObservationMapper observationMapper, DiagnosisMapper diagnosisMapper, DispositionMapper dispositionMapper, EmrApiProperties emrApiProperties) {
        this.observationMapper = observationMapper;
        this.diagnosisMapper = diagnosisMapper;
        this.dispositionMapper = dispositionMapper;
        this.emrApiProperties = emrApiProperties;
    }

    public void update(EncounterTransaction encounterTransaction, Set<Obs> allObs) {
        for (Obs obs : allObs) {
            if (getDiagnosisMetadata().isDiagnosis(obs)) {
                encounterTransaction.addDiagnosis(diagnosisMapper.map(obs, getDiagnosisMetadata()));
            } else if(dispositionMapper.isDispositionGroup(obs)) {
                encounterTransaction.setDisposition(dispositionMapper.getDisposition(obs));
            }
            else {
                encounterTransaction.addObservation(observationMapper.map(obs));
            }
        }
    }

    private DiagnosisMetadata getDiagnosisMetadata() {
        if (this.diagnosisMetadata == null) {
            this.diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
        }
        return this.diagnosisMetadata;
    }

}
