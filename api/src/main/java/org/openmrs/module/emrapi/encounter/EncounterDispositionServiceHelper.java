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

public class EncounterDispositionServiceHelper {

    private ConceptService conceptService;
    private Concept dispositionConcept;
    private Concept dispositionGroupConcept;

    public EncounterDispositionServiceHelper(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public EncounterDispositionServiceHelper() {

    }

    public void update(Encounter encounter, EncounterTransaction.Disposition disposition, Date observationDateTime) {
        try {
            if(disposition != null){
                dispositionGroupConcept = getDispositionGroupConcept();
                dispositionConcept = geDispositionConcept();

                if(!hasDisposition(encounter)){
                    Obs obs = createObsGroupForDisposition(disposition,encounter,observationDateTime);
                    encounter.addObs(obs);
                    return;
                }

                editExistingObservations(disposition,encounter,observationDateTime);
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Obs createObsGroupForDisposition(EncounterTransaction.Disposition disposition,Encounter encounter,Date observationDateTime) throws ParseException {
        Obs obs = new Obs();
        obs.setConcept(dispositionGroupConcept);
        Obs dispositionAsObservation = constructDispositionObs(encounter, new Obs(), disposition.getCode(), observationDateTime);
        obs.addGroupMember(dispositionAsObservation);

        if(disposition.getAdditionalObs() != null){
            for (EncounterTransaction.Observation observation : disposition.getAdditionalObs()) {
                if(observation.getValue() != null && !((String) observation.getValue()).isEmpty() ){
                    obs.addGroupMember(createObsFromObservation(observation, encounter, observationDateTime));
                }
            }
        }
        return obs;
    }

    private void editExistingObservations(EncounterTransaction.Disposition disposition,Encounter encounter,Date observationDateTime) throws ParseException {
        Set<Obs> allEncounterObs = encounter.getAllObs();
        Obs existingDispositionGroup = getMatchingObservation(allEncounterObs, dispositionGroupConcept.getUuid());
        Obs existingDisposition = getMatchingObservation(existingDispositionGroup.getGroupMembers(), dispositionConcept.getUuid());
        constructDispositionObs(encounter, existingDisposition, disposition.getCode(), observationDateTime);
        if(disposition.getAdditionalObs() != null){
            for (EncounterTransaction.Observation observation : disposition.getAdditionalObs()) {
                Obs matchingObservation = getMatchingObservation(existingDispositionGroup.getGroupMembers(), observation.getConceptUuid());
                updateObsFromObservation(observation, matchingObservation,observationDateTime);
            }
        }
    }


    private boolean hasDisposition(Encounter encounter) {
        Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(false);
        for (Obs obs : obsAtTopLevel) {
            if(obs.getConcept().getUuid().equals(dispositionGroupConcept.getUuid())){
                return true;
            }
        }
        return false;
    }

    private Obs createObsFromObservation(EncounterTransaction.Observation observation,Encounter encounter,Date observationDateTime) throws ParseException {
        Obs obs = new Obs();
        updateObsFromObservation(observation,obs,observationDateTime);
        obs.setPerson(encounter.getPatient());
        obs.setEncounter(encounter);
        return obs;
    }

    private Obs updateObsFromObservation(EncounterTransaction.Observation observation, Obs obs,Date observationDateTime) throws ParseException {
        obs.setConcept(getConceptByUuid(observation.getConceptUuid()));
        obs.setValueAsString((String) observation.getValue());
        if(observation.isVoided()){
            obs.setVoided(observation.isVoided());
            obs.setVoidReason(observation.getVoidReason());
        }
        obs.setComment(observation.getComment());
        obs.setObsDatetime(observationDateTime);
        return obs;
    }


    private Obs constructDispositionObs(Encounter encounter,Obs dispositionObs, String dispositionCode, Date observationDateTime) {
        if(dispositionObs != null){
            dispositionObs.setConcept(dispositionConcept);
            dispositionObs.setValueCoded(getMatchingAnswer(dispositionConcept.getAnswers(), dispositionCode));
            dispositionObs.setObsDatetime(observationDateTime);
            dispositionObs.setPerson(encounter.getPatient());
            dispositionObs.setEncounter(encounter);
        }
        return dispositionObs;
    }

    private Concept geDispositionConcept() {
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
            if(answerConcept.getUuid().equals(answer.getAnswerConcept().getUuid())){
                return answerConcept;
            }
        }
        throw  new IllegalArgumentException("Concept with code "+dispositionCode+" does not belong to this observation group");
    }
}
