/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.procedure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link HibernateProcedureDAO}.
 */
public class ProcedureDAOTest extends BaseModuleContextSensitiveTest {

    private static final String PROCEDURE_DATASET = "ProcedureDataset.xml";

    @Autowired
    private ProcedureDAO procedureDAO;

    @Autowired
    private PatientService patientService;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EncounterService encounterService;

    private Patient patient7;
    private Patient patient8;

    @BeforeEach
    public void setUp() throws Exception {
        executeDataSet(PROCEDURE_DATASET);
        patient7 = patientService.getPatient(7);
        patient8 = patientService.getPatient(8);
    }

    @Test
    public void getById_shouldReturnProcedureWhenExists() {
        Procedure procedure = procedureDAO.getById(1);

        assertNotNull(procedure);
        assertEquals(Integer.valueOf(1), procedure.getProcedureId());
        assertEquals("procedure-uuid-001", procedure.getUuid());
        assertEquals(patient7, procedure.getPatient());
    }

    @Test
    public void getById_shouldReturnNullWhenNotExists() {
        Procedure procedure = procedureDAO.getById(999);

        assertNull(procedure);
    }

    @Test
    public void getByUuid_shouldReturnProcedureWhenExists() {
        Procedure procedure = procedureDAO.getByUuid("procedure-uuid-002");

        assertNotNull(procedure);
        assertEquals(Integer.valueOf(2), procedure.getProcedureId());
        assertEquals("Appendectomy", procedure.getProcedureNonCoded());
    }

    @Test
    public void getByUuid_shouldReturnNullWhenNotExists() {
        Procedure procedure = procedureDAO.getByUuid("non-existent-uuid");

        assertNull(procedure);
    }

    @Test
    public void getProceduresByPatient_shouldReturnNonVoidedProcedures() {
        List<Procedure> procedures = procedureDAO.getProceduresByPatient(patient7, false);

        assertEquals(3, procedures.size());
        // Should be sorted by startDateTime descending
        assertEquals("procedure-uuid-003", procedures.get(0).getUuid()); // 2023-07-20
        assertEquals("procedure-uuid-001", procedures.get(1).getUuid()); // 2023-06-15
        assertEquals("procedure-uuid-002", procedures.get(2).getUuid()); // 2020-03-01

        // Verify none are voided
        for (Procedure p : procedures) {
            assertFalse(p.getVoided());
        }
    }

    @Test
    public void getProceduresByPatient_shouldIncludeVoidedWhenRequested() {
        List<Procedure> procedures = procedureDAO.getProceduresByPatient(patient7, true);

        assertEquals(4, procedures.size());
        // Should include the voided procedure
        boolean foundVoided = procedures.stream().anyMatch(Procedure::getVoided);
        assertTrue(foundVoided);
    }

    @Test
    public void getProceduresByPatient_shouldReturnEmptyListForPatientWithNoProcedures() {
        Patient patientWithNoProcedures = patientService.getPatient(2); // From standard test data

        List<Procedure> procedures = procedureDAO.getProceduresByPatient(patientWithNoProcedures, false);

        assertNotNull(procedures);
        assertTrue(procedures.isEmpty());
    }

    @Test
    public void saveOrUpdate_shouldSaveNewProcedure() {
        Procedure newProcedure = new Procedure();
        newProcedure.setPatient(patient7);
        newProcedure.setBodySite(conceptService.getConcept(4));
        newProcedure.setStartDateTime(new Date());
        newProcedure.setProcedureNonCoded("New Test Procedure");
        newProcedure.setStatus(conceptService.getConcept(5)); // Completed
        newProcedure.setEstimatedStartDate("2018");

        Procedure saved = procedureDAO.saveOrUpdate(newProcedure);

        assertNotNull(saved.getProcedureId());
        assertNotNull(saved.getUuid());

        // Verify it can be retrieved
        Procedure retrieved = procedureDAO.getById(saved.getProcedureId());
        assertNotNull(retrieved);
        assertEquals("New Test Procedure", retrieved.getProcedureNonCoded());
    }

    @Test
    public void saveOrUpdate_shouldUpdateExistingProcedure() {
        Procedure procedure = procedureDAO.getById(1);
        String originalNotes = procedure.getNotes();

        procedure.setNotes("Updated notes for testing");
        procedureDAO.saveOrUpdate(procedure);

        // Clear session and retrieve again
        Procedure updated = procedureDAO.getById(1);
        assertEquals("Updated notes for testing", updated.getNotes());
        assertNotEquals(originalNotes, updated.getNotes());
    }

//    @Test
//    public void delete_shouldRemoveProcedureFromDatabase() {
//        Procedure procedure = procedureDAO.getById(5);
//        assertNotNull(procedure);
//
//        procedureDAO.delete(procedure);
//
//        Procedure deleted = procedureDAO.getById(5);
//        assertNull(deleted);
//    }

    @Test
    public void procedure_shouldHaveCodedProcedure() {
        Procedure procedure = procedureDAO.getById(1);

        assertNotNull(procedure.getProcedureCoded());
        assertEquals(Integer.valueOf(3), procedure.getProcedureCoded().getConceptId());
    }

    @Test
    public void procedure_shouldHaveNonCodedProcedure() {
        Procedure procedure = procedureDAO.getById(2);

        assertNull(procedure.getProcedureCoded());
        assertEquals("Appendectomy", procedure.getProcedureNonCoded());
    }

}
