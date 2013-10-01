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

    public EncounterDispositionServiceHelper(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public EncounterDispositionServiceHelper() {

    }

    public void update(Encounter encounter, EncounterTransaction.Disposition disposition, Date observationDateTime) {
        try {
            Set<Obs> existingObservations = encounter.getAllObs();
            if(disposition != null){
                updateDisposition(encounter,disposition,observationDateTime,existingObservations);
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private Concept getConceptByUuid(String conceptUuid) {
        Concept concept = conceptService.getConceptByUuid(conceptUuid);
        if (concept == null) {
            throw new ConceptNotFoundException("Observation concept does not exist" + conceptUuid);
        }
        return concept;
    }


    private void updateDisposition(Encounter encounter,EncounterTransaction.Disposition disposition,Date dispositionDatetime,Set<Obs> existingObservations) throws ParseException {
        Concept dispositionGroupConcept = conceptService.getConceptByMapping(EmrApiConstants.CONCEPT_CODE_DISPOSITION_CONCEPT_SET, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
        Concept dispositionConcept = conceptService.getConceptByMapping(EmrApiConstants.CONCEPT_CODE_DISPOSITION_CONCEPT, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);

        Obs obs = createObsGroupForDisposition(disposition,dispositionConcept, dispositionGroupConcept);

        if(!hasDisposition(encounter, dispositionGroupConcept.getUuid())){
            encounter.addObs(obs);
            return;
        }

        editExistingObservations(encounter,obs);
    }

    private void editExistingObservations(Encounter encounter, Obs obs) {
        for (Obs childObs : obs.getGroupMembers()) {
            Obs matchingObservation = getMatchingObservation(encounter.getAllObs(), childObs.getConcept().getUuid());
            matchingObservation.setValueCoded(obs.getValueCoded());
            matchingObservation.setValueCoded(obs.getValueCoded());
            matchingObservation.setValueCoded(obs.getValueCoded());
        }
    }


    private void editExistingObservations(Encounter encounter, Concept dispositionConcept,String dispositionCode, Date dispositionDatetime) {
        Obs existingDisposition = getMatchingObservation(encounter.getAllObs(), dispositionConcept.getUuid());
        createDispositionObservation(dispositionCode,dispositionConcept,existingDisposition);

    }

    private boolean hasDisposition(Encounter encounter, String dispositionGroupConceptUuid) {
        Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(false);
        for (Obs obs : obsAtTopLevel) {
            if(obs.getConcept().getUuid().equals(dispositionGroupConceptUuid)){
                return true;
            }
        }
        return false;
    }

    private Obs createObsGroupForDisposition(EncounterTransaction.Disposition disposition, Concept dispositionConcept, Concept dispositionGroupConcept) throws ParseException {
        Obs obs = new Obs();
        obs.setConcept(dispositionGroupConcept);
        Obs dispositionObservation = createDispositionObservation(disposition.getCode(), dispositionConcept, new Obs());
        obs.addGroupMember(dispositionObservation);
        for (EncounterTransaction.Observation observation : disposition.getAdditionalObs()) {
            obs.addGroupMember(createObservation(observation, new Obs()));
        }
        return obs;
    }

    private Obs createObservation(EncounterTransaction.Observation observation, Obs obs) throws ParseException {
        obs.setConcept(getConceptByUuid(observation.getConceptUuid()));
        obs.setValueAsString((String) observation.getValue());
        if(observation.isVoided()){
            obs.setVoided(observation.isVoided());
            obs.setVoidReason(observation.getVoidReason());
        }
        obs.setComment(observation.getComment());
        return obs;  //To change body of created methods use File | Settings | File Templates.
    }

    private Obs createDispositionObservation(String dispositionCode, Concept dispositionConcept, Obs dispositionObs) {
        dispositionObs.setConcept(dispositionConcept);
        dispositionObs.setValueCoded(getMatchingAnswer(dispositionConcept.getAnswers(), dispositionCode));
        return dispositionObs;
    }

    private Concept getMatchingAnswer(Collection<ConceptAnswer> answers, String dispositionCode) {
        Concept answerConcept = conceptService.getConceptByMapping(dispositionCode, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
        for (ConceptAnswer answer : answers) {
            if(answerConcept.equals(answer)){
                return answerConcept;
            }
        }
        throw  new IllegalArgumentException("Concept with code "+dispositionCode+" does not belong to this observation group");
    }

    // private

    private Obs constructObsWithoutValue(Encounter encounter, Obs observation, String conceptUuid, Date observationDateTime,boolean isVoided, String voidedReason) {
        if (isVoided) {
            observation.setVoided(true);
            observation.setVoidReason(voidedReason);
            return null;
        }
        if (observation == null) {
            observation = createNewObs(encounter,conceptUuid);
        }
        observation.setObsDatetime(observationDateTime);
        return observation;
    }

    private Obs createNewObs(Encounter encounter, String obsConceptUuid) {
        Obs observation = new Obs();
        Concept concept = conceptService.getConceptByUuid(obsConceptUuid);
        if (concept == null) {
            throw new ConceptNotFoundException("Observation concept does not exist : " + obsConceptUuid);
        }
        observation.setPerson(encounter.getPatient());
        observation.setEncounter(encounter);
        observation.setConcept(concept);
        encounter.addObs(observation);
        return observation;
    }


    private Obs getMatchingObservation(Set<Obs> existingObservations, String conceptUUID) {
        for (Obs obs : existingObservations) {
            if (StringUtils.equals(obs.getConcept().getUuid(), conceptUUID)) return obs;
        }
        return null;
    }
}
