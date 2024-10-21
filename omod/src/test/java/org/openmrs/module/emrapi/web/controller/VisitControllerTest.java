package org.openmrs.module.emrapi.web.controller;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.visit.VisitWithDiagnoses;
import org.openmrs.module.emrapi.visit.VisitWithDiagnosesService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

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

        ResponseEntity<List<VisitWithDiagnoses>> visits = visitController.getVisitsByPatientId(109);
        assertNotNull(visits);
        assert visits.getBody().size() == 1;
        Set<Encounter> encounters = visits.getBody().get(0).getEncounters();

        for (Encounter encounter : encounters) {
            System.out.println(encounter.getEncounterType().getUuid());
            assert encounter.getEncounterType().getUuid().equals(visitNoteEncounterTypeUuid);
        }
    }
}