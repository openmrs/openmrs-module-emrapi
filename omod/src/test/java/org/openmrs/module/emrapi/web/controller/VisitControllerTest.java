package org.openmrs.module.emrapi.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.visit.VisitWithDiagnoses;
import org.openmrs.module.emrapi.visit.VisitWithDiagnosesService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class VisitControllerTest extends BaseModuleWebContextSensitiveTest {

    @Autowired
    VisitController visitController;

    @Autowired
    VisitWithDiagnosesService customVisitService;

    @Autowired
    PatientService patientService;


    @Before
    public void setUp() throws Exception {
        executeDataSet("baseMetaData.xml");
        executeDataSet("pastVisitSetup.xml");
    }

    @Test
    public void shouldGetVisitsByPatientId() {

        String visitNoteEncounterTypeUuid = "d7151f82-c1f3-4152-a605-2f9ea7414a79";
        String patientUuid = "8604d42e-3ca8-11e3-bf2b-0d0c09861e97";

        ResponseEntity<List<VisitWithDiagnoses>> visitsResponse = (ResponseEntity<List<VisitWithDiagnoses>>) visitController.getVisitsByPatientId(patientUuid);
        assertNotNull(visitsResponse);

        List<VisitWithDiagnoses> visits = visitsResponse.getBody();

        assertNotNull(visits);
        assert visits.size() == 2;

        VisitWithDiagnoses firstVisit = visits.get(1);
        Set<Encounter> firstVisitEncounters = firstVisit.getEncounters();
        Set<Diagnosis> firstVisitDiagnoses = firstVisit.getDiagnoses();

        assert firstVisit.getId() == 1014;
        assertEquals(firstVisit.getPatient().getUuid(), patientUuid);
        assert firstVisitEncounters.size() == 2;
        assert firstVisitDiagnoses.size() == 3;

        for (Encounter encounter : firstVisitEncounters) {
            assert encounter.getEncounterType().getUuid().equals(visitNoteEncounterTypeUuid);
        }

        VisitWithDiagnoses secondVisit = visits.get(0);
        Set<Encounter> secondVisitEncounters = secondVisit.getEncounters();
        Set<Diagnosis> secondVisitDiagnoses = secondVisit.getDiagnoses();

        assert secondVisit.getId() == 1015;
        assertEquals(secondVisit.getPatient().getUuid(), patientUuid);
        assert secondVisitEncounters.size() == 1;
        assert secondVisitDiagnoses.size() == 2;

        for (Encounter encounter : secondVisitEncounters) {
            assert encounter.getEncounterType().getUuid().equals(visitNoteEncounterTypeUuid);
        }

    }

    @Test
    public void shouldThrowExceptionWhenPatientUuidIsInvalid() {
        String invalidPatientUuid = "invalid-uuid";
        ResponseEntity<?> visitsResponse = visitController.getVisitsByPatientId(invalidPatientUuid);
        assertNotNull(visitsResponse);
        assert visitsResponse.getStatusCode().is4xxClientError();
    }
}