package org.openmrs.module.emrapi.disposition.actions;


import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.reporting.common.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Sets the death and deathDate fields on a Patient, and saves those changes.
 * TODO implement an API method for recording a patient's death in this module (based on, but cleaner than, the PatientService.processDeath method)
 * Note: this currently is failing because it isn't setting causeOfDeath
 */
@Component("markPatientDeadDispositionAction")
public class MarkPatientDeadDispositionAction implements DispositionAction {

    public static final String CAUSE_OF_DEATH_CONCEPT_PARAMETER = "causeOfDeathConceptId";
    public static final String DEATH_DATE_PARAMETER = "deathDate";

    @Autowired
    private PatientService patientService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    private ConceptService conceptService;

    @Override
    public void action(EncounterDomainWrapper encounterDomainWrapper, Obs dispositionObsGroupBeingCreated, Map<String, String[]> requestParameters) {
        String deathDateParam = DispositionActionUtils.getSingleRequiredParameter(requestParameters, DEATH_DATE_PARAMETER);
        Date deathDate = null;
        try {
            deathDate = DateUtil.parseDate(deathDateParam, "yyyy-MM-dd");
        } catch (Exception ex) {
            throw new IllegalArgumentException("cannot parse " + DEATH_DATE_PARAMETER, ex);
        }

        String causeOfDeathConceptIdParam = DispositionActionUtils.getSingleOptionalParameter(requestParameters, CAUSE_OF_DEATH_CONCEPT_PARAMETER);
        Concept causeOfDeath = null;
        if (causeOfDeathConceptIdParam != null) {
            try {
                causeOfDeath = conceptService.getConcept(Integer.valueOf(causeOfDeathConceptIdParam));
            } catch (Exception ex) {
                throw new IllegalArgumentException("cannot parse " + CAUSE_OF_DEATH_CONCEPT_PARAMETER, ex);
            }
        }
        if (causeOfDeath == null) {
            causeOfDeath = emrApiProperties.getUnknownCauseOfDeathConcept();
        }

        Patient patient = encounterDomainWrapper.getEncounter().getPatient();
        patient.setDead(true);
        if (deathDate != null) {
            patient.setDeathDate(deathDate);
        }
        patient.setCauseOfDeath(causeOfDeath);
        patientService.savePatient(patient);
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public void setConceptService(ConceptService conceptService) {
        this.conceptService = conceptService;
    }
}
