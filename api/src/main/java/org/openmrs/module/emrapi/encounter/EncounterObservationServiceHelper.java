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
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Add/update/delete a {@link org.openmrs.Obs} on an {@link org.openmrs.Encounter}.
 */
public class EncounterObservationServiceHelper {

    private ConceptService conceptService;
    private EmrApiProperties emrApiProperties;
    private DiagnosisMetadata diagnosisMetadata;
    private ObsService obsService;

    public EncounterObservationServiceHelper(ConceptService conceptService, EmrApiProperties emrApiProperties, ObsService obsService) {
        this.conceptService = conceptService;
        this.emrApiProperties = emrApiProperties;
        this.obsService = obsService;
    }

    public void update(Encounter encounter, List<EncounterTransaction.Observation> observations, Date observationDateTime) {
        try {
            Set<Obs> existingObservations = encounter.getAllObs();
            for (EncounterTransaction.Observation observationData : observations) {
                updateObservation(encounter, observationDateTime, existingObservations, observationData);
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void updateObservation(Encounter encounter, Date observationDateTime, Set<Obs> existingObservations, EncounterTransaction.Observation observationData) throws ParseException {
        Obs observation = getMatchingObservation(existingObservations, observationData.getConceptUuid());
        if (observationData.isVoided()) {
            observation.setVoided(true);
            observation.setVoidReason(observationData.getVoidReason());
            return;
        }
        if (observation == null) {
            observation = new Obs();
            Concept concept = conceptService.getConceptByUuid(observationData.getConceptUuid());
            if (concept == null) {
                throw new ConceptNotFoundException("Observation concept does not exist" + observationData.getConceptUuid());
            }
            observation.setPerson(encounter.getPatient());
            observation.setEncounter(encounter);
            observation.setConcept(concept);
            encounter.addObs(observation);
        }
        observation.setComment(observationData.getComment());
        if(observationData.getValue() != null) {
            observation.setValueAsString(observationData.getValue().toString());
        }
        observation.setObsDatetime(observationDateTime);
    }

    private Obs getMatchingObservation(Set<Obs> existingObservations, String conceptUUID) {
        for (Obs obs : existingObservations) {
            if (StringUtils.equals(obs.getConcept().getUuid(), conceptUUID)) return obs;
        }
        return null;
    }

    public void updateDiagnoses(Encounter encounter, List<EncounterTransaction.Diagnosis> diagnoses, Date observationDateTime) {
        for (EncounterTransaction.Diagnosis diagnosisRequest : diagnoses) {
            org.openmrs.module.emrapi.diagnosis.Diagnosis diagnosis = createDiagnosis(diagnosisRequest);
            Obs obs = getDiagnosisMetadata().buildDiagnosisObsGroup(diagnosis);
            obs.setObsDatetime(observationDateTime);
            encounter.addObs(obs);
        }
    }

    private DiagnosisMetadata getDiagnosisMetadata() {
        if (this.diagnosisMetadata == null) {
            this.diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
        }
        return this.diagnosisMetadata;
    }

    private org.openmrs.module.emrapi.diagnosis.Diagnosis createDiagnosis(EncounterTransaction.Diagnosis diagnosisRequest) {
        CodedOrFreeTextAnswer codedOrFreeTextAnswer = new CodedOrFreeTextAnswer(diagnosisRequest.getDiagnosis(), conceptService);
        org.openmrs.module.emrapi.diagnosis.Diagnosis.Order order = org.openmrs.module.emrapi.diagnosis.Diagnosis.Order.valueOf(diagnosisRequest.getOrder());
        org.openmrs.module.emrapi.diagnosis.Diagnosis.Certainty certainty = org.openmrs.module.emrapi.diagnosis.Diagnosis.Certainty.valueOf(diagnosisRequest.getCertainty());
        Obs existingObs = obsService.getObsByUuid(diagnosisRequest.getExistingObs());
        org.openmrs.module.emrapi.diagnosis.Diagnosis diagnosis = new org.openmrs.module.emrapi.diagnosis.Diagnosis(codedOrFreeTextAnswer, order);
        diagnosis.setCertainty(certainty);
        diagnosis.setExistingObs(existingObs);
        return diagnosis;
    }
}