package org.openmrs.module.emrapi.db;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.EmrApiContextSensitiveTest;
import org.openmrs.module.emrapi.visit.VisitWithDiagnoses;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VisitDAOTest extends EmrApiContextSensitiveTest {

    @Autowired
    private VisitDAO visitDAO;

    @Before
    public void setup() {
        executeDataSet("baseTestDataset.xml");
        executeDataSet("pastVisitSetup.xml");
    }

    @Test
    public void shouldFetchVisitsByPatientId() {
        String visitNoteEncounterTypeUuid = "d7151f82-c1f3-4152-a605-2f9ea7414a79";

        Patient patient = new Patient();
        patient.setPatientId(109);

        List<VisitWithDiagnoses> visits = visitDAO.getVisitsWithNotesAndDiagnosesByPatient(patient,0,10);
        assertNotNull(visits);
        assert visits.size() == 3;

        VisitWithDiagnoses firstVisit = visits.get(2);
        Set<Encounter> firstVisitEncounters = firstVisit.getEncounters();
        Set<Diagnosis> firstVisitDiagnoses = firstVisit.getDiagnoses();

        assert firstVisit.getId() == 1014;
        assert firstVisit.getPatient().getPatientId() == 109;
        assert firstVisitEncounters.size() == 2;
        assert firstVisitDiagnoses.size() == 3;

        for (Encounter encounter : firstVisitEncounters) {
            assert encounter.getEncounterType().getUuid().equals(visitNoteEncounterTypeUuid);
        }

        VisitWithDiagnoses secondVisit = visits.get(1);
        Set<Encounter> secondVisitEncounters = secondVisit.getEncounters();
        Set<Diagnosis> secondVisitDiagnoses = secondVisit.getDiagnoses();

        assert secondVisit.getId() == 1015;
        assert secondVisit.getPatient().getPatientId() == 109;
        assert secondVisitEncounters.size() == 1;
        assert secondVisitDiagnoses.size() == 2;

        for (Encounter encounter : secondVisitEncounters) {
            assert encounter.getEncounterType().getUuid().equals(visitNoteEncounterTypeUuid);
        }
        
        VisitWithDiagnoses thirdVisit = visits.get(0);
        Set<Encounter> thirdVisitEncounters = thirdVisit.getEncounters();
        Set<Diagnosis> thirdVisitDiagnoses = thirdVisit.getDiagnoses();
        
        assert thirdVisit.getId() == 1017;
        assert thirdVisit.getPatient().getPatientId() == 109;
        assert thirdVisitEncounters.isEmpty();
        assert thirdVisitDiagnoses.isEmpty();
        
    }
    
    @Test
    public void shouldFetchVisitsByPatientIdWithPagination() {
        Patient patient = new Patient();
        patient.setPatientId(109);

        List<VisitWithDiagnoses> visits = visitDAO.getVisitsWithNotesAndDiagnosesByPatient(patient,0,1);
        assertNotNull(visits);
        assert visits.size() == 1;

        VisitWithDiagnoses mostRecentVisit = visits.get(0);
        assert mostRecentVisit.getId() == 1015;
    }

}
