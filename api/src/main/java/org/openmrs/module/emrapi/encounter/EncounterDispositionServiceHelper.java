/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.emrapi.encounter;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import static org.openmrs.module.emrapi.utils.GeneralUtils.getCurrentDateIfNull;

public class EncounterDispositionServiceHelper {

    private ConceptService conceptService;
    private Concept dispositionConcept;
    private Concept dispositionGroupConcept;

    public EncounterDispositionServiceHelper(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public EncounterDispositionServiceHelper() {

    }

    public void update(Encounter encounter, EncounterTransaction.Disposition disposition) {
        try {
            if (isValid(disposition)) {
                dispositionGroupConcept = getDispositionGroupConcept();
                dispositionConcept = getDispositionConcept();
                Obs dispositionObsGroup = getExistingDisposition(encounter);
                if (dispositionObsGroup == null) {
                    dispositionObsGroup = createObsGroupForDisposition(disposition);
                } else if (disposition.isVoided()) {
                    dispositionObsGroup = voidDisposition(dispositionObsGroup, disposition);
                } else {
                    dispositionObsGroup = editExistingDisposition(dispositionObsGroup, disposition);
                }
                encounter.addObs(dispositionObsGroup);
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Obs voidDisposition(Obs existingDispositionObsGroup, EncounterTransaction.Disposition disposition) {
        existingDispositionObsGroup.setVoided(disposition.isVoided());
        existingDispositionObsGroup.setVoidReason(disposition.getVoidReason());
        for (Obs groupMemberObs : existingDispositionObsGroup.getGroupMembers()) {
            groupMemberObs.setVoided(disposition.isVoided());
            groupMemberObs.setVoidReason(disposition.getVoidReason());
        }
        return existingDispositionObsGroup;
    }

    private Obs createObsGroupForDisposition(EncounterTransaction.Disposition disposition) throws ParseException {
        Date dispositionDateTime = new Date();
        Obs obs = new Obs();
        obs.setConcept(dispositionGroupConcept);
        obs.setObsDatetime(dispositionDateTime);

        Obs dispositionAsObservation = mapDispositionProperties(new Obs(), disposition.getCode());
        dispositionAsObservation.setObsDatetime(dispositionDateTime);
        obs.addGroupMember(dispositionAsObservation);

        if (disposition.getAdditionalObs() != null) {
            for (EncounterTransaction.Observation etObservation : disposition.getAdditionalObs()) {
                if (etObservation.getValue() != null && !((String) etObservation.getValue()).isEmpty()) {
                    obs.addGroupMember(createObsFromETObservation(etObservation));
                }
            }
        }
        return obs;
    }

    private Obs editExistingDisposition(Obs existingDispositionObsGroup, EncounterTransaction.Disposition disposition) throws ParseException {
        Obs existingDisposition = getMatchingObservation(existingDispositionObsGroup.getGroupMembers(),
                dispositionConcept.getUuid());
        mapDispositionProperties(existingDisposition, disposition.getCode());
        if (disposition.getAdditionalObs() != null) {
            for (EncounterTransaction.Observation additionalObs : disposition.getAdditionalObs()) {
                Obs matchingObs = getMatchingObservation(existingDispositionObsGroup.getGroupMembers(),
                        additionalObs.getConceptUuid());
                if (matchingObs == null) {
                    existingDispositionObsGroup.addGroupMember(createObsFromETObservation(additionalObs));
                } else {
                    updateObsFromObservation(additionalObs, matchingObs);
                }
            }
        }
        return existingDispositionObsGroup;
    }


    private Obs getExistingDisposition(Encounter encounter) {
        Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(false);
        for (Obs obs : obsAtTopLevel) {
            if (obs.getConcept().getUuid().equals(dispositionGroupConcept.getUuid())) {
                return obs;
            }
        }
        return null;
    }

    private Obs createObsFromETObservation(EncounterTransaction.Observation observation) throws ParseException {
        Obs obs = new Obs();
        updateObsFromObservation(observation, obs);
        obs.setObsDatetime(new Date());
        return obs;
    }

    private Obs updateObsFromObservation(EncounterTransaction.Observation observation, Obs obs) throws ParseException {
        if (observation != null && obs != null) {
            obs.setConcept(getConceptByUuid(observation.getConceptUuid()));
            obs.setComment(observation.getComment());
            if (observation.getVoided()) {
                obs.setVoided(observation.getVoided());
                obs.setVoidReason(observation.getVoidReason());
                return obs;
            }
            obs.setValueAsString((String) observation.getValue());
        }
        return obs;
    }


    private Obs mapDispositionProperties(Obs dispositionObs, String dispositionCode) {
        if (dispositionObs != null) {
            dispositionObs.setConcept(dispositionConcept);
            dispositionObs.setValueCoded(getMatchingAnswer(dispositionConcept.getAnswers(), dispositionCode));
        }
        return dispositionObs;
    }

    private Concept getDispositionConcept() {
        Concept concept = conceptService.getConceptByMapping(EmrApiConstants.CONCEPT_CODE_DISPOSITION, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
        if (concept == null) {
            throw new ConceptNotFoundException("Disposition concept does not exist. Code : " + EmrApiConstants.CONCEPT_CODE_DISPOSITION);
        }
        return concept;
    }

    private Concept getDispositionGroupConcept() {
        Concept concept = conceptService.getConceptByMapping(EmrApiConstants.CONCEPT_CODE_DISPOSITION_CONCEPT_SET, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
        if (concept == null) {
            throw new ConceptNotFoundException("Disposition group concept does not exist. Code : " + EmrApiConstants.CONCEPT_CODE_DISPOSITION_CONCEPT_SET);
        }
        return concept;
    }


    private Concept getConceptByUuid(String conceptUuid) {
        Concept concept = conceptService.getConceptByUuid(conceptUuid);
        if (concept == null) {
            throw new ConceptNotFoundException("Observation concept does not exist" + conceptUuid);
        }
        return concept;
    }

    private Obs getMatchingObservation(Set<Obs> existingObservations, String conceptUUID) {
        for (Obs obs : existingObservations) {
            if (StringUtils.equals(obs.getConcept().getUuid(), conceptUUID)) return obs;
        }
        return null;
    }

    private Concept getMatchingAnswer(Collection<ConceptAnswer> answers, String dispositionCode) {
        Concept answerConcept = conceptService.getConceptByMapping(dispositionCode, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
        for (ConceptAnswer answer : answers) {
            if (answerConcept.getUuid().equals(answer.getAnswerConcept().getUuid())) {
                return answerConcept;
            }
        }
        throw new IllegalArgumentException("Concept with code " + dispositionCode + " does not belong to this observation group");
    }

    private boolean isValid(EncounterTransaction.Disposition disposition) {
        if (disposition == null || StringUtils.isEmpty(disposition.getCode())) {
            return false;
        }
        return true;
    }


}
