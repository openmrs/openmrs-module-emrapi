package org.openmrs.module.emrapi.exitfromcare;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.VisitService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public class ExitFromCareServiceImpl  extends BaseOpenmrsService implements ExitFromCareService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private ProgramWorkflowService programWorkflowService;

    private VisitService visitService;

    private PatientService patientService;

    private AdtService adtService;

    private EmrApiProperties emrApiProperties;

    @Override
    @Transactional
    public void closePatientPrograms(Patient patient, Concept outcome, Date completionDate) {

        if (completionDate.after(new Date())) {
            throw new IllegalArgumentException("Date cannot be in the future");
        }

        for (PatientProgram patientProgram : programWorkflowService.getPatientPrograms(patient, null, null, null, null, null, false)) {
            if (patientProgram.getActive() && programWorkflowService.getPossibleOutcomes(patientProgram.getProgram().getProgramId()).contains(outcome)) {
                patientProgram.setOutcome(outcome);
                patientProgram.setDateCompleted(completionDate.after(patientProgram.getDateEnrolled()) ? completionDate : patientProgram.getDateEnrolled()); // if completion date is before date enrolled, just use completion date
                // we specifically aren't handling state transitions here
                programWorkflowService.savePatientProgram(patientProgram);
            }
        }

    }

    @Override
    public void reopenPatientPrograms(Patient patient, Concept outcome) {
        // iterate through all programs and reopen those with outcome that matches outcome
        for (PatientProgram patientProgram : programWorkflowService.getPatientPrograms(patient, null, null, null, null, null, false)) {
            if (outcome.equals(patientProgram.getOutcome())) {
                patientProgram.setOutcome(null);
                patientProgram.setDateCompleted(null);
                programWorkflowService.savePatientProgram(patientProgram);
            }
        }
    }

    @Override
    public void updatePatientProgramCompletionDate(Patient patient, Concept outcome, Date completionDate) {
        for (PatientProgram patientProgram : programWorkflowService.getPatientPrograms(patient, null, null, null, null, null, false)) {
            if (patientProgram.getOutcome() != null && patientProgram.getOutcome().equals(outcome)) {
                patientProgram.setDateCompleted(completionDate.after(patientProgram.getDateEnrolled()) ? completionDate : patientProgram.getDateEnrolled()); // if completion date is before date enrolled, just use completion date
            }
        }
    }

    @Override
    @Transactional
    public void closeActiveVisits(Patient patient) {
        List<Visit> visitList = visitService.getActiveVisitsByPatient(patient);
        if (visitList != null) {
            for (Visit visit : visitList) {
                adtService.closeAndSaveVisit(visit);
            }
        }
    }

    @Override
    @Transactional
    public void markPatientDead(Patient patient, Concept causeOfDeath, Date deathDate) {

        if (deathDate == null) {
            deathDate = new Date();
        }

        if (deathDate.after(new Date())) {
            throw new IllegalArgumentException("Death date cannot be in the future");
        }

        if (patient.getBirthdate() != null && deathDate.before(patient.getBirthdate())) {
            throw new IllegalArgumentException("Death date cannot be before birthdate");
        }

        // we intentionally bypass the PatientService exitFromCare and processDeath methods as we are providing a new implementation
        patient.setDead(true);
        patient.setCauseOfDeath(causeOfDeath);
        patient.setDeathDate(deathDate);
        patientService.savePatient(patient);

        closeActiveVisits(patient);

        Concept patientDied = emrApiProperties.getPatientDiedConcept();
        if (patientDied != null) {
            // update any programs that had previously been closed with outcome=patient died with matching death date
            updatePatientProgramCompletionDate(patient, patientDied, deathDate);
            // close any open programs as patient died with completion date=death date
            closePatientPrograms(patient, patientDied, deathDate);
        }
    }

    @Override
    public void markPatientNotDead(Patient patient) {

        Date deathDate = patient.getDeathDate();

        patient.setDead(false);
        patient.setCauseOfDeath(null);
        patient.setDeathDate(null);
        patientService.savePatient(patient);

        Concept patientDied = emrApiProperties.getPatientDiedConcept();
        if (patientDied != null && deathDate != null) {
            reopenPatientPrograms(patient, patientDied);
        }
    }

    public void setProgramWorkflowService(ProgramWorkflowService programWorkflowService) {
        this.programWorkflowService = programWorkflowService;
    }

    public void setVisitService(VisitService visitService) {
        this.visitService = visitService;
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public void setAdtService(AdtService adtService) {
        this.adtService = adtService;
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }
}
