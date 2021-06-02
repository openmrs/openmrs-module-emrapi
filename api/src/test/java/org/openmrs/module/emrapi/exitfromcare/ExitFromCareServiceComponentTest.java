package org.openmrs.module.emrapi.exitfromcare;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.VisitService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hibernate.validator.util.Contracts.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class ExitFromCareServiceComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private ExitFromCareService exitFromCareService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private ProgramWorkflowService programWorkflowService;

    @Autowired
    private ConceptService conceptService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("baseTestDataset.xml");
    }

    @Test
    public void shouldMarkPatientDiedAndCloseActiveProgramsAndVisits() {

        Program mdrTBProgram = programWorkflowService.getProgram(2);
        Program hivProgram = programWorkflowService.getProgram(1);

        // hack in adding a outcome concept to one of the programs, as none of the test programs have outcomes
        Concept died = conceptService.getConcept(16);  // DIED concept in the standard test dataset
        Concept outcomeSet = new Concept();
        outcomeSet.addSetMember(died);
        outcomeSet.setSet(true);
        ConceptName outcomeSetName = new ConceptName();
        outcomeSetName.setName("Program Outcomes");
        outcomeSetName.setLocale(Locale.ENGLISH);
        outcomeSet.setFullySpecifiedName(outcomeSetName);
        conceptService.saveConcept(outcomeSet);
        mdrTBProgram.setOutcomesConcept(outcomeSet);
        programWorkflowService.saveProgram(mdrTBProgram);

        Patient patient = patientService.getPatient(2);
        Date now = new Date();
        Concept unknown = conceptService.getConcept(22);

        // sanity checks
        assertFalse(patient.isDead());
        List<Visit> visits = visitService.getVisitsByPatient(patient);
        assertThat(visits.size(), is(3));
        List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(patient, null, null, null, null, null, false);
        assertThat(patientPrograms.size(), is(2));
        for (PatientProgram pp : patientPrograms) {
            assertNull(pp.getDateCompleted());
            assertNull(pp.getOutcome());
        }

        exitFromCareService.markPatientDied(patient, unknown, now);

        assertTrue(patient.isDead());
        assertThat(patient.getCauseOfDeath(), is(unknown));
        assertThat(patient.getDeathDate(), is(now));

        // all the visits in the test dataset were open, so they should now be closed
        for (Visit visit : visitService.getVisitsByPatient(patient)) {
            assertNotNull(visit.getStopDatetime());
        }

        // confirm the MDR-TB program has been closed with the specified outcome
        patientPrograms = programWorkflowService.getPatientPrograms(patient, mdrTBProgram, null, null, null, null, false);
        assertThat(patientPrograms.size(), is(1));
        assertThat(patientPrograms.get(0).getDateCompleted(), is(now));
        assertThat(patientPrograms.get(0).getOutcome(), is(died));

        // assert that Malaria program has not been closed
        patientPrograms = programWorkflowService.getPatientPrograms(patient, hivProgram, null, null, null, null, false);
        assertThat(patientPrograms.size(), is(1));
        assertNull(patientPrograms.get(0).getDateCompleted());
        assertNull(patientPrograms.get(0).getOutcome());

    }
}
