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
            Set<Obs> existingObservations = encounter.getObsAtTopLevel(false);
            for (EncounterTransaction.Observation observationData : observations) {
                updateObservation(encounter, null, existingObservations, observationDateTime, observationData);
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void updateObservation(Encounter encounter, Obs parentObs, Set<Obs> existingObservations, Date observationDateTime, EncounterTransaction.Observation observationData) throws ParseException {
        Obs observation = getMatchingObservation(existingObservations, observationData.getUuid());
        if (observationData.isVoided()) {
            observation.setVoided(true);
            observation.setVoidReason(observationData.getVoidReason());
            return;
        }
        if (observation == null) {
            observation = newObservation(encounter, observationData);
            if (parentObs == null)
                encounter.addObs(observation);
            else parentObs.addGroupMember(observation);
        }
        mapObservationProperties(observationDateTime, observationData, observation);

        for (EncounterTransaction.Observation member : observationData.getGroupMembers()) {
            updateObservation(encounter, observation, observation.getGroupMembers(), observationDateTime, member);
        }
    }

    private void mapObservationProperties(Date observationDateTime, EncounterTransaction.Observation observationData, Obs observation) throws ParseException {
        observation.setComment(observationData.getComment());
        if (observationData.getValue() != null) {
            if (observation.getConcept().getDatatype().getHl7Abbreviation().equals("CWE")) {
                observation.setValueCoded(conceptService.getConceptByUuid((String) observationData.getValue()));
            } else {
                observation.setValueAsString(observationData.getValue().toString());
            }
        }
        observation.setObsDatetime(observationDateTime);
    }

    private Obs newObservation(Encounter encounter, EncounterTransaction.Observation observationData) {
        Obs observation;
        observation = new Obs();
        Concept concept = conceptService.getConceptByUuid(observationData.getConceptUuid());
        if (concept == null) {
            throw new ConceptNotFoundException("Observation concept does not exist" + observationData.getConceptUuid());
        }
        observation.setPerson(encounter.getPatient());
        observation.setEncounter(encounter);
        observation.setConcept(concept);
        return observation;
    }

    private Obs getMatchingObservation(Set<Obs> existingObservations, String observationUuid) {
        if (existingObservations == null) return null;
        for (Obs obs : existingObservations) {
            if (StringUtils.equals(obs.getUuid(), observationUuid)) return obs;
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
        CodedOrFreeTextAnswer codedOrFreeTextAnswer = getCodedOrFreeTextAnswer(diagnosisRequest);
        org.openmrs.module.emrapi.diagnosis.Diagnosis.Order order = org.openmrs.module.emrapi.diagnosis.Diagnosis.Order.valueOf(diagnosisRequest.getOrder());
        org.openmrs.module.emrapi.diagnosis.Diagnosis.Certainty certainty = org.openmrs.module.emrapi.diagnosis.Diagnosis.Certainty.valueOf(diagnosisRequest.getCertainty());
        Obs existingObs = obsService.getObsByUuid(diagnosisRequest.getExistingObs());
        org.openmrs.module.emrapi.diagnosis.Diagnosis diagnosis = new org.openmrs.module.emrapi.diagnosis.Diagnosis(codedOrFreeTextAnswer, order);
        diagnosis.setCertainty(certainty);
        diagnosis.setExistingObs(existingObs);
        return diagnosis;
    }

    private CodedOrFreeTextAnswer getCodedOrFreeTextAnswer(EncounterTransaction.Diagnosis diagnosisRequest) {
        if (StringUtils.isNotBlank(diagnosisRequest.getFreeTextAnswer())) {
            return new CodedOrFreeTextAnswer(diagnosisRequest.getFreeTextAnswer());
        }
        EncounterTransaction.Concept codedAnswer = diagnosisRequest.getCodedAnswer();
        if(codedAnswer != null) {
            Concept concept = conceptService.getConceptByUuid(codedAnswer.getUuid());
            if (concept == null) {
                throw new ConceptNotFoundException("Coded answer concept does not exist" + codedAnswer.getUuid());
            }
            return new CodedOrFreeTextAnswer(concept);
        }
        throw new RuntimeException("Diagnosis should have either free text or coded answer");
    }
}