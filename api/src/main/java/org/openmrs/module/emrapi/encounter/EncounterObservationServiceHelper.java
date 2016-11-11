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
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.openmrs.module.emrapi.encounter.mapper.ObsMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Add/update/delete a {@link org.openmrs.Obs} on an {@link org.openmrs.Encounter}.
 */
public class EncounterObservationServiceHelper {

    private ConceptService conceptService;
    private EmrApiProperties emrApiProperties;
    private ObsService obsService;
    private OrderService orderService;
    private ObsMapper obsMapper;

    @Autowired
    public EncounterObservationServiceHelper(ConceptService conceptService,
                                             EmrApiProperties emrApiProperties,
                                             ObsService obsService, OrderService orderService,
                                             ObsMapper obsMapper ) {
        this.conceptService = conceptService;
        this.emrApiProperties = emrApiProperties;
        this.obsService = obsService;
        this.orderService = orderService;
        this.obsMapper = obsMapper;
    }

    public void update(Encounter encounter, List<EncounterTransaction.Observation> observations) {
            Set<Obs> existingObservations = encounter.getObsAtTopLevel(false);
            for (EncounterTransaction.Observation observationData : observations) {
                Obs obsFound = this.obsMapper.getMatchingObservation(existingObservations,observationData.getUuid());
                encounter.addObs(this.obsMapper.transformEtObs(encounter,obsFound, observationData));
            }
    }

    public void updateDiagnoses(Encounter encounter, List<EncounterTransaction.Diagnosis> diagnoses) {
        for (EncounterTransaction.Diagnosis diagnosisRequest : diagnoses) {
            org.openmrs.module.emrapi.diagnosis.Diagnosis diagnosis = createDiagnosis(diagnosisRequest);
            Obs obs = emrApiProperties.getDiagnosisMetadata().buildDiagnosisObsGroup(diagnosis);
            if (diagnosisRequest.getDiagnosisDateTime() != null) {
                obs.setObsDatetime(diagnosisRequest.getDiagnosisDateTime());
            }
            if (obs.getObsDatetime() == null) {
                obs.setObsDatetime(new Date());
            }
            obs.setComment(diagnosisRequest.getComments());
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
