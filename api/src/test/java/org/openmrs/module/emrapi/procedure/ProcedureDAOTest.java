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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
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
    private ProcedureTypeDAO procedureTypeDAO;

    private Patient patient;

    @BeforeEach
    public void setUp() throws Exception {
        executeDataSet(PROCEDURE_DATASET);
        patient = patientService.getPatient(7);
    }

    @Nested
    class GetById {

        @Test
        void shouldReturnProcedureWhenExists() {
            Procedure procedure = procedureDAO.getById(1);

            assertNotNull(procedure);
            assertEquals(Integer.valueOf(1), procedure.getProcedureId());
            assertEquals("procedure-uuid-001", procedure.getUuid());
            assertEquals(patient, procedure.getPatient());
        }

        @Test
        void shouldReturnNullWhenNotExists() {
            assertNull(procedureDAO.getById(999));
        }
       
       @Test
       void shouldHaveCodedProcedure() {
          Procedure procedure = procedureDAO.getById(1);
          
          assertNotNull(procedure.getProcedureCoded());
          assertEquals(Integer.valueOf(3), procedure.getProcedureCoded().getConceptId());
       }
       
       @Test
       void shouldHaveNonCodedProcedure() {
          Procedure procedure = procedureDAO.getById(2);
          
          assertNull(procedure.getProcedureCoded());
          assertEquals("Appendectomy", procedure.getProcedureNonCoded());
       }
    }

    @Nested
    class GetByUuid {

        @Test
        void shouldReturnProcedureWhenExists() {
            Procedure procedure = procedureDAO.getByUuid("procedure-uuid-002");

            assertNotNull(procedure);
            assertEquals(Integer.valueOf(2), procedure.getProcedureId());
            assertEquals("Appendectomy", procedure.getProcedureNonCoded());
        }

        @Test
        void shouldReturnNullWhenNotExists() {
            assertNull(procedureDAO.getByUuid("non-existent-uuid"));
        }
    }

    @Nested
    class GetProceduresByPatient {

        @Test
        void shouldReturnNonVoidedProcedures() {
            List<Procedure> procedures = procedureDAO.getProceduresByPatient(patient, false, null, null);

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
        void shouldIncludeVoidedWhenRequested() {
            List<Procedure> procedures = procedureDAO.getProceduresByPatient(patient, true, null, null);

            assertEquals(4, procedures.size());
            // Should include the voided procedure
            boolean foundVoided = procedures.stream().anyMatch(Procedure::getVoided);
            assertTrue(foundVoided);
        }

        @Test
        void shouldReturnEmptyListForPatientWithNoProcedures() {
            Patient patientWithNoProcedures = patientService.getPatient(2); // From standard test data

            List<Procedure> procedures = procedureDAO.getProceduresByPatient(patientWithNoProcedures, false, null, null);

            assertNotNull(procedures);
            assertTrue(procedures.isEmpty());
        }

        @Test
        void shouldLimitResultsWhenMaxResultsIsSet() {
            List<Procedure> procedures = procedureDAO.getProceduresByPatient(patient, false, null, 2);
            assertEquals(2, procedures.size());
            assertEquals("procedure-uuid-003", procedures.get(0).getUuid());
            assertEquals("procedure-uuid-001", procedures.get(1).getUuid());
        }

        @Test
        void shouldOffsetResultsWhenFirstResultIsSet() {
            List<Procedure> procedures = procedureDAO.getProceduresByPatient(patient, false, 1, null);
            assertEquals(2, procedures.size());
            assertEquals("procedure-uuid-001", procedures.get(0).getUuid());
            assertEquals("procedure-uuid-002", procedures.get(1).getUuid());
        }

        @Test
        void shouldApplyBothPaginationParams() {
            List<Procedure> procedures = procedureDAO.getProceduresByPatient(patient, false, 1, 1);
            assertEquals(1, procedures.size());
            assertEquals("procedure-uuid-001", procedures.get(0).getUuid());
        }

        @Test
        void shouldReturnEmptyListWhenFirstResultExceedsTotal() {
            List<Procedure> procedures = procedureDAO.getProceduresByPatient(patient, false, 100, null);
            assertTrue(procedures.isEmpty());
        }
    }

    @Nested
    class GetProcedureCountByPatient {

        @Test
        void shouldReturnCountOfNonVoidedProcedures() {
            Long count = procedureDAO.getProcedureCountByPatient(patient, false);
            assertEquals(3L, count);
        }

        @Test
        void shouldIncludeVoidedWhenRequested() {
            Long count = procedureDAO.getProcedureCountByPatient(patient, true);
            assertEquals(4L, count);
        }
    }

    @Nested
    class SaveOrUpdate {

        @Test
        void shouldSaveNewProcedure() {
            Procedure newProcedure = new Procedure();
            newProcedure.setPatient(patient);
            newProcedure.setProcedureType(procedureTypeDAO.getByUuid("procedure-type-uuid-001"));
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
        void shouldUpdateExistingProcedure() {
            Procedure procedure = procedureDAO.getById(1);
            String originalNotes = procedure.getNotes();

            procedure.setNotes("Updated notes for testing");
            procedureDAO.saveOrUpdate(procedure);

            // Clear session and retrieve again
            Procedure updated = procedureDAO.getById(1);
            assertEquals("Updated notes for testing", updated.getNotes());
            assertNotEquals(originalNotes, updated.getNotes());
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldRemoveProcedureFromDatabase() {
            Procedure procedure = procedureDAO.getById(5);
            assertNotNull(procedure);

            procedureDAO.delete(procedure);

            assertNull(procedureDAO.getById(5));
        }
    }
}
