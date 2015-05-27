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
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.openmrs.Concept;
import org.openmrs.ConceptComplex;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import static org.openmrs.module.emrapi.utils.GeneralUtils.getCurrentDateIfNull;

/**
 * Add/update/delete a {@link org.openmrs.Obs} on an {@link org.openmrs.Encounter}.
 */
public class EncounterObservationServiceHelper {

    private ConceptService conceptService;
    private EmrApiProperties emrApiProperties;
    private ObsService obsService;
    private OrderService orderService;


    public EncounterObservationServiceHelper(ConceptService conceptService, EmrApiProperties emrApiProperties, ObsService obsService, OrderService orderService) {
        this.conceptService = conceptService;
        this.emrApiProperties = emrApiProperties;
        this.obsService = obsService;
        this.orderService = orderService;
    }

    public void update(Encounter encounter, List<EncounterTransaction.Observation> observations) {
        try {
            Set<Obs> existingObservations = encounter.getObsAtTopLevel(false);
            for (EncounterTransaction.Observation observationData : observations) {
                updateObservation(encounter, null, existingObservations, observationData);
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void updateObservation(Encounter encounter, Obs parentObs, Set<Obs> existingObservations, EncounterTransaction.Observation observationData) throws ParseException {
        Obs observation = getMatchingObservation(existingObservations, observationData.getUuid());
        if (observation == null) {
            observation = newObservation(encounter, observationData);
            if (parentObs == null) {
                encounter.addObs(observation);
            }
            else parentObs.addGroupMember(observation);
        }
        if (observationData.getVoided()) {
            observation.setVoided(true);
            observation.setVoidReason(observationData.getVoidReason());
        } else {
            mapObservationProperties(observationData, observation);
        }

        for (EncounterTransaction.Observation member : observationData.getGroupMembers()) {
            updateObservation(encounter, observation, observation.getGroupMembers(), member);
        }
    }

    private void mapObservationProperties(EncounterTransaction.Observation observationData, Obs observation) throws ParseException {
        observation.setComment(observationData.getComment());
        if (observationData.getValue() != null) {
            if (observation.getConcept().getDatatype().isCoded()) {
                String uuid = getUuidOfCodedObservationValue(observationData.getValue());
                Concept conceptByUuid = conceptService.getConceptByUuid(uuid);
                if (conceptByUuid == null) {
                    Drug drug = conceptService.getDrugByUuid(uuid);
                    observation.setValueDrug(drug);
                    observation.setValueCoded(drug.getConcept());
                } else {
                    observation.setValueCoded(conceptByUuid);
                }
            } else if (observation.getConcept().isComplex()) {
                observation.setValueComplex(observationData.getValue().toString());
                Concept conceptComplex = observation.getConcept();
                if (conceptComplex instanceof HibernateProxy) {
                    Hibernate.initialize(conceptComplex);
                    conceptComplex = (ConceptComplex) ((HibernateProxy) conceptComplex).getHibernateLazyInitializer().getImplementation();
                }
                obsService.getHandler(((ConceptComplex) conceptComplex).getHandler()).saveObs(observation);
            } else if (!observation.getConcept().getDatatype().getUuid().equals(ConceptDatatype.N_A_UUID)) {
                observation.setValueAsString(observationData.getValue().toString());
            }
        }
        if(observationData.getOrderUuid() != null && !observationData.getOrderUuid().isEmpty()){
            observation.setOrder(getOrderByUuid(observationData.getOrderUuid()));
        }
        observation.setObsDatetime(getCurrentDateIfNull(observationData.getObservationDateTime()));
    }

    private String getUuidOfCodedObservationValue(Object codeObsVal) {
        if (codeObsVal instanceof LinkedHashMap) return (String) ((LinkedHashMap) codeObsVal).get("uuid");
        return (String) codeObsVal;
    }

    private Order getOrderByUuid(String orderUuid){
        return orderService.getOrderByUuid(orderUuid);
    }

    private Obs newObservation(Encounter encounter, EncounterTransaction.Observation observationData) {
        Obs observation;
        observation = new Obs();
        if(!StringUtils.isBlank(observationData.getUuid())){
            observation.setUuid(observationData.getUuid());
        }
        Date observationDateTime = getCurrentDateIfNull(observationData.getObservationDateTime());
        Concept concept = conceptService.getConceptByUuid(observationData.getConceptUuid());
        if (concept == null) {
            throw new ConceptNotFoundException("Observation concept does not exist" + observationData.getConceptUuid());
        }
        observation.setPerson(encounter.getPatient());
        observation.setEncounter(encounter);
        observation.setConcept(concept);
        observation.setObsDatetime(observationDateTime);
        return observation;
    }

    private Obs getMatchingObservation(Set<Obs> existingObservations, String observationUuid) {
        if (existingObservations == null) return null;
        for (Obs obs : existingObservations) {
            if (StringUtils.equals(obs.getUuid(), observationUuid)) return obs;
        }
        return null;
    }

    public void updateDiagnoses(Encounter encounter, List<EncounterTransaction.Diagnosis> diagnoses) {
        for (EncounterTransaction.Diagnosis diagnosisRequest : diagnoses) {
            org.openmrs.module.emrapi.diagnosis.Diagnosis diagnosis = createDiagnosis(diagnosisRequest);
            Obs obs = emrApiProperties.getDiagnosisMetadata().buildDiagnosisObsGroup(diagnosis);
            Date diagnosisDateTime = getCurrentDateIfNull(diagnosisRequest.getDiagnosisDateTime());
            obs.setObsDatetime(diagnosisDateTime);
            if (diagnosisRequest.isVoided()) {
                voidDiagnosisObservation(diagnosisRequest, obs);
            }
            encounter.addObs(obs);
        }
    }

    private void voidDiagnosisObservation(EncounterTransaction.Diagnosis diagnosisRequest, Obs obs) {
        obs.setVoided(diagnosisRequest.isVoided());
        obs.setVoidReason(diagnosisRequest.getVoidReason());
        for (Obs groupMember : obs.getGroupMembers()) {
            groupMember.setVoided(diagnosisRequest.isVoided());
            groupMember.setVoidReason(diagnosisRequest.getVoidReason());
        }
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
