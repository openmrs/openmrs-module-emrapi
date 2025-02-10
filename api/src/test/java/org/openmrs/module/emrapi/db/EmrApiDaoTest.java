package org.openmrs.module.emrapi.db;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.EmrApiContextSensitiveTest;
import org.openmrs.module.emrapi.visit.VisitWithDiagnosesAndNotes;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EmrApiDaoTest extends EmrApiContextSensitiveTest {

    @Autowired
    private EmrApiDAO emrApiDAO;

    @Before
    public void setup() {
        executeDataSet("baseTestDataset.xml");
        executeDataSet("pastVisitSetup.xml");
    }

    @Test
    public void shouldFetchVisitsWithNotesAndDiagnosesByPatientId() {
        String visitNoteEncounterTypeUuid = "d7151f82-c1f3-4152-a605-2f9ea7414a79";

        Patient patient = new Patient();
        patient.setPatientId(109);

        List<VisitWithDiagnosesAndNotes> visits = emrApiDAO.getVisitsWithNotesAndDiagnosesByPatient(patient,0,10);
        assertNotNull(visits);
        assert visits.size() == 3;

        VisitWithDiagnosesAndNotes firstVisit = visits.get(2);
        Set<Encounter> firstVisitNotes = firstVisit.getVisitNotes();
        Set<Diagnosis> firstVisitDiagnoses = firstVisit.getDiagnoses();

        assert firstVisit.getVisit().getId() == 1014;
        assert firstVisit.getVisit().getPatient().getPatientId() == 109;
        assert firstVisitNotes.size() == 2;
        assert firstVisitDiagnoses.size() == 3;

        for (Encounter encounter : firstVisitNotes) {
            assert encounter.getEncounterType().getUuid().equals(visitNoteEncounterTypeUuid);
        }

        VisitWithDiagnosesAndNotes secondVisit = visits.get(1);
        Set<Encounter> secondVisitNotes = secondVisit.getVisitNotes();
        Set<Diagnosis> secondVisitDiagnoses = secondVisit.getDiagnoses();

        assert secondVisit.getVisit().getId() == 1015;
        assert secondVisit.getVisit().getPatient().getPatientId() == 109;
        assert secondVisitNotes.size() == 1;
        assert secondVisitDiagnoses.size() == 2;

        for (Encounter encounter : secondVisitNotes) {
            assert encounter.getEncounterType().getUuid().equals(visitNoteEncounterTypeUuid);
        }
        
        VisitWithDiagnosesAndNotes thirdVisit = visits.get(0);
        Set<Encounter> thirdVisitNotes = thirdVisit.getVisitNotes();
        Set<Diagnosis> thirdVisitDiagnoses = thirdVisit.getDiagnoses();
        
        assert thirdVisit.getVisit().getId() == 1017;
        assert thirdVisit.getVisit().getPatient().getPatientId() == 109;
        assert thirdVisitNotes.isEmpty();
        assert thirdVisitDiagnoses.isEmpty();
        
    }
    
    @Test
    public void shouldFetchVisitsWithNotesAndDiagnosesWithPagination() {
        Patient patient = new Patient();
        patient.setPatientId(109);

        List<VisitWithDiagnosesAndNotes> visits = emrApiDAO.getVisitsWithNotesAndDiagnosesByPatient(patient,0,1);
        assertNotNull(visits);
        assert visits.size() == 1;

        VisitWithDiagnosesAndNotes mostRecentVisit = visits.get(0);
        assert mostRecentVisit.getVisit().getId() == 1017;
    }

}
