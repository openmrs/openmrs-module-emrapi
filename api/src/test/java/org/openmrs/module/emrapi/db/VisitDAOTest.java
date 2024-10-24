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
//        executeDataSet("baseMetaData.xml");
        executeDataSet("pastVisitSetup.xml");
    }

    @Test
    public void shouldFetchVisitsByPatientId() {
        String visitNoteEncounterTypeUuid = "d7151f82-c1f3-4152-a605-2f9ea7414a79";

        Patient patient = new Patient();
        patient.setPatientId(109);

        List<VisitWithDiagnoses> visits = visitDAO.getVisitsByPatientId(patient);
        assertNotNull(visits);
        assert visits.size() == 2;

        VisitWithDiagnoses firstVisit = visits.get(1);
        Set<Encounter> firstVisitEncounters = firstVisit.getEncounters();
        Set<Diagnosis> firstVisitDiagnoses = firstVisit.getDiagnoses();

        assert firstVisit.getId() == 1014;
        assert firstVisit.getPatient().getPatientId() == 109;
        assert firstVisitEncounters.size() == 2;
        assert firstVisitDiagnoses.size() == 3;

        for (Encounter encounter : firstVisitEncounters) {
            assert encounter.getEncounterType().getUuid().equals(visitNoteEncounterTypeUuid);
        }

        VisitWithDiagnoses secondVisit = visits.get(0);
        Set<Encounter> secondVisitEncounters = secondVisit.getEncounters();
        Set<Diagnosis> secondVisitDiagnoses = secondVisit.getDiagnoses();

        assert secondVisit.getId() == 1015;
        assert secondVisit.getPatient().getPatientId() == 109;
        assert secondVisitEncounters.size() == 1;
        assert secondVisitDiagnoses.size() == 2;

        for (Encounter encounter : secondVisitEncounters) {
            assert encounter.getEncounterType().getUuid().equals(visitNoteEncounterTypeUuid);
        }
    }

}
