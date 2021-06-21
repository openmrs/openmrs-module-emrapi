package org.openmrs.module.emrapi.exitfromcare;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;

import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExitFromCareServiceTest {

    ExitFromCareServiceImpl exitFromCareService;

    ProgramWorkflowService mockProgramWorkflowService;

    VisitService mockVisitService;

    PatientService mockPatientService;

    AdtService mockAdtService;

    EmrApiProperties mockEmrApiProperties;

    @Before
    public void setup() {
        mockProgramWorkflowService = mock(ProgramWorkflowService.class);
        mockVisitService = mock(VisitService.class);
        mockPatientService = mock(PatientService.class);
        mockAdtService = mock(AdtService.class);
        mockEmrApiProperties = mock(EmrApiProperties.class);
        exitFromCareService = spy(new ExitFromCareServiceImpl());
        exitFromCareService.setProgramWorkflowService(mockProgramWorkflowService);
        exitFromCareService.setVisitService(mockVisitService);
        exitFromCareService.setPatientService(mockPatientService);
        exitFromCareService.setAdtService(mockAdtService);
        exitFromCareService.setEmrApiProperties(mockEmrApiProperties);
    }


    @Test
    public void closePatientPrograms_shouldCloseActivePatientPrograms() {

        Patient patient = new Patient();

        Concept validOutcome1 = new Concept(2);
        Concept validOutcome2 = new Concept(3);

        Date now = new Date();

        Program p1 = new Program();
        Program p2 = new Program();

        PatientProgram pp1 = new PatientProgram();
        pp1.setProgram(p1);
        pp1.setDateEnrolled(now);

        PatientProgram pp2 = new PatientProgram();
        pp2.setProgram(p2);
        pp2.setDateEnrolled(now);

        pp1.setPatient(patient);
        pp2.setPatient(patient);

        when(mockProgramWorkflowService.getPatientPrograms(patient, null, null, null, null, null,false))
                .thenReturn(Arrays.asList(pp1, pp2));

        when(mockProgramWorkflowService.getPossibleOutcomes(pp1.getPatientProgramId())).thenReturn(Arrays.asList(validOutcome1, validOutcome2));

        exitFromCareService.closePatientPrograms(patient, validOutcome1, now);

        assertThat(pp1.getDateCompleted(), is(now));
        assertThat(pp2.getDateCompleted(), is(now));
        assertThat(pp1.getOutcome(), is(validOutcome1));
        assertThat(pp2.getOutcome(), is(validOutcome1));

        verify(mockProgramWorkflowService,times(1)).savePatientProgram(pp1);
        verify(mockProgramWorkflowService,times(1)).savePatientProgram(pp2);
    }

    @Test
    public void closePatientPrograms_shouldNotCloseActivePatientProgramIfOutcomeConceptNotValidForProgram() {

        Patient patient = new Patient();

        Concept outcome = new Concept(1);
        Concept validOutcome1 = new Concept(2);
        Concept validOutcome2 = new Concept(3);

        Date now = new Date();

        Program p1 = new Program();

        PatientProgram pp1 = new PatientProgram();
        pp1.setProgram(p1);
        pp1.setDateEnrolled(now);

        pp1.setPatient(patient);

        when(mockProgramWorkflowService.getPatientPrograms(patient, null, null, null, null, null,false))
                .thenReturn(Arrays.asList(pp1));

        when(mockProgramWorkflowService.getPossibleOutcomes(pp1.getPatientProgramId())).thenReturn(Arrays.asList(validOutcome1, validOutcome2));

        exitFromCareService.closePatientPrograms(patient, outcome, now);

        assertNull(pp1.getDateCompleted());
        assertNull(pp1.getOutcome());
    }

    @Test
    public void closePatientPrograms_shouldNotChangeCompletionDateOrOutcomeOfClosedProgram() {

        Patient patient = new Patient();

        Concept validOutcome1 = new Concept(2);
        Concept validOutcome2 = new Concept(3);

        Date dateEnrolled = new DateTime(2020, 1,1,1,1,0).toDate();
        Date dateCompleted = new DateTime(2020, 3,1,1,1,0).toDate();
        Date now = new Date();

        Program p1 = new Program();

        PatientProgram pp1 = new PatientProgram();
        pp1.setProgram(p1);
        pp1.setDateEnrolled(dateEnrolled);
        pp1.setDateCompleted(dateCompleted);
        pp1.setOutcome(validOutcome2);
        pp1.setPatient(patient);

        when(mockProgramWorkflowService.getPatientPrograms(patient, null, null, null, null, null,false))
                .thenReturn(Arrays.asList(pp1));

        when(mockProgramWorkflowService.getPossibleOutcomes(pp1.getPatientProgramId())).thenReturn(Arrays.asList(validOutcome1, validOutcome2));

        exitFromCareService.closePatientPrograms(patient, validOutcome1, now);

        assertThat(pp1.getDateCompleted(), is(dateCompleted));
        assertThat(pp1.getOutcome(), is(validOutcome2));
    }

    @Test(expected=IllegalArgumentException.class)
    public void closePatientPrograms_shouldThrowExceptionIfDateInFuture() {
        Patient patient = new Patient();
        Concept outcome = new Concept();
        Date futureDate = new DateTime().plusDays(1).toDate();
        exitFromCareService.closePatientPrograms(patient, outcome, futureDate);
    }

    @Test
    public void closePatientPrograms_shouldNotCloseActivePatientProgramIfCloseDateBeforeStartDate() {

        Patient patient = new Patient();

        Concept outcome = new Concept(1);

        Date dateEnrolled = new DateTime(2021, 1,1,1,1,0).toDate();
        Date dateCompleted = new DateTime(2020, 3,1,1,1,0).toDate();

        Program p1 = new Program();

        PatientProgram pp1 = new PatientProgram();
        pp1.setProgram(p1);
        pp1.setDateEnrolled(dateEnrolled);

        pp1.setPatient(patient);

        when(mockProgramWorkflowService.getPatientPrograms(patient, null, null, null, null, null,false))
                .thenReturn(Arrays.asList(pp1));

        when(mockProgramWorkflowService.getPossibleOutcomes(pp1.getPatientProgramId())).thenReturn(Arrays.asList(outcome));

        exitFromCareService.closePatientPrograms(patient, outcome, dateCompleted);

        assertNull(pp1.getDateCompleted());
        assertNull(pp1.getOutcome());
    }

    @Test
    public void closeActiveVisits_shouldEndActiveVisitsForPatient() {
        Patient patient = new Patient();

        Visit visit1 = new Visit();
        Visit visit2 = new Visit();

        Date dateStarted = new DateTime(2020, 1,1,1,1,0).toDate();
        Date dateStopped = new DateTime(2020, 3,1,1,1,0).toDate();
        Date now = new Date();

        visit1.setPatient(patient);
        visit1.setStartDatetime(dateStarted);
        visit1.setStopDatetime(dateStopped);

        visit2.setPatient(patient);
        visit2.setStartDatetime(dateStarted);

        when(mockVisitService.getActiveVisitsByPatient(patient)).thenReturn(Arrays.asList(visit2));

        exitFromCareService.closeActiveVisits(patient);

        verify(mockAdtService, times(0)).closeAndSaveVisit(visit1);
        verify(mockAdtService, times(1)).closeAndSaveVisit(visit2);
    }

    @Test
    public void markPatientDied_shouldMarkPatientDied() {
        Patient patient = new Patient();
        Concept causeOfDeath = new Concept();
        Concept patientDied = new Concept();
        Date now = new Date();

        when(mockEmrApiProperties.getPatientDiedConcept()).thenReturn(patientDied);

        exitFromCareService.markPatientDied(patient, causeOfDeath, now);

        assertTrue(patient.isDead());
        assertThat(patient.getCauseOfDeath(), is(causeOfDeath));
        assertThat(patient.getDeathDate(), is(now));

        verify(mockPatientService, times(1)).savePatient(patient);
        verify(exitFromCareService, times(1)).closeActiveVisits(patient);
        verify(exitFromCareService, times(1)).closePatientPrograms(patient, patientDied, now);
    }

    @Test
    public void markPatientDied_shouldMarkPatientDiedOnCurrentDateIfNoDateSpecified() {
        Patient patient = new Patient();
        Concept causeOfDeath = new Concept();
        Concept patientDied = new Concept();
        Date now = new Date();

        when(mockEmrApiProperties.getPatientDiedConcept()).thenReturn(patientDied);

        exitFromCareService.markPatientDied(patient, causeOfDeath, null);

        assertTrue(patient.isDead());
        assertThat(patient.getCauseOfDeath(), is(causeOfDeath));
        // we took a timestamp before calling "markPatientDied", so death date should be equal to or greater than that timestamp
        assertTrue(patient.getDeathDate().equals(now)|| patient.getDeathDate().after(now));
    }

    @Test(expected=IllegalArgumentException.class)
    public void markPatientDied_shouldFailIfDeathDateInFuture() {
        Patient patient = new Patient();
        Date futureDate = new DateTime().plusDays(1).toDate();

        exitFromCareService.markPatientDied(patient, null, futureDate);

    }

    @Test(expected=IllegalArgumentException.class)
    public void markPatientDied_shouldFailIfDeathDateBeforeBirthDate() {
        Patient patient = new Patient();
        Date birthDate = new DateTime().minusDays(20).toDate();
        patient.setBirthdate(birthDate);
        Date deathDate = new DateTime().minusDays(30).toDate();

        exitFromCareService.markPatientDied(patient, null, deathDate);
    }



}
