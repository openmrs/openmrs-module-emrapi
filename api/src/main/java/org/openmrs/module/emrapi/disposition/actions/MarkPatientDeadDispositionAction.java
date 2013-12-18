package org.openmrs.module.emrapi.disposition.actions;


import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Sets the death and deathDate fields on a Patient, and saves those changes.
 * TODO implement an API method for recording a patient's death in this module (based on, but cleaner than, the PatientService.processDeath method)
 */
@Component("markPatientDeadDispositionAction")
public class MarkPatientDeadDispositionAction implements DispositionAction {

    @Autowired
    private PatientService patientService;

    @Autowired
    private DispositionService dispositionService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Override
    public void action(EncounterDomainWrapper encounterDomainWrapper, Obs dispositionObsGroupBeingCreated, Map<String, String[]> requestParameters) {

        Date deathDate = dispositionService.getDispositionDescriptor().getDateOfDeath(dispositionObsGroupBeingCreated);

        // TODO: support pulling cause of death from the disposition
        Concept causeOfDeath = emrApiProperties.getUnknownCauseOfDeathConcept();

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

    public void setDispositionService(DispositionService dispositionService) {
        this.dispositionService = dispositionService;
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }
}
