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
import org.openmrs.module.emrapi.encounter.matcher.ObservationTypeMatcher;

import java.util.Set;

public class EncounterObservationsMapper {
    private  ObservationMapper observationMapper;
    private  DiagnosisMapper diagnosisMapper;
    private  DispositionMapper dispositionMapper;
    private ObservationTypeMatcher observationTypeMatcher;
    private DiagnosisMetadata diagnosisMetadata;
    private EmrApiProperties emrApiProperties;

    public EncounterObservationsMapper(ObservationMapper observationMapper, DiagnosisMapper diagnosisMapper, DispositionMapper dispositionMapper, EmrApiProperties emrApiProperties, ObservationTypeMatcher observationTypeMatcher) {
        this.observationMapper = observationMapper;
        this.diagnosisMapper = diagnosisMapper;
        this.dispositionMapper = dispositionMapper;
        this.emrApiProperties = emrApiProperties;
        this.observationTypeMatcher = observationTypeMatcher;
    }

    public void update(EncounterTransaction encounterTransaction, Set<Obs> allObs) {
        for (Obs obs : allObs) {
            ObservationTypeMatcher.ObservationType observationType = observationTypeMatcher.getObservationType(obs);
            switch (observationType) {
                case DIAGNOSIS:
                    if (!obs.isVoided()) {
                        encounterTransaction.addDiagnosis(diagnosisMapper.map(obs, getDiagnosisMetadata()));
                    }
                    break;
                case DISPOSITION:
                    encounterTransaction.setDisposition(dispositionMapper.getDisposition(obs));
                    break;
                default:
                    encounterTransaction.addObservation(observationMapper.map(obs));
                    break;
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
