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
            Procedure procedure = procedureDAO.getProcedure(1);

            assertNotNull(procedure);
            assertEquals(Integer.valueOf(1), procedure.getProcedureId());
            assertEquals("procedure-uuid-001", procedure.getUuid());
            assertEquals(patient, procedure.getPatient());
        }

        @Test
        void shouldReturnNullWhenNotExists() {
            assertNull(procedureDAO.getProcedure(999));
        }
       
       @Test
       void shouldHaveCodedProcedure() {
          Procedure procedure = procedureDAO.getProcedure(1);
          
          assertNotNull(procedure.getProcedureCoded());
          assertEquals(Integer.valueOf(3), procedure.getProcedureCoded().getConceptId());
       }
       
       @Test
       void shouldHaveNonCodedProcedure() {
          Procedure procedure = procedureDAO.getProcedure(2);
          
          assertNull(procedure.getProcedureCoded());
          assertEquals("Appendectomy", procedure.getProcedureNonCoded());
       }
    }

    @Nested
    class GetByUuid {

        @Test
        void shouldReturnProcedureWhenExists() {
            Procedure procedure = procedureDAO.getProcedureByUuid("procedure-uuid-002");

            assertNotNull(procedure);
            assertEquals(Integer.valueOf(2), procedure.getProcedureId());
            assertEquals("Appendectomy", procedure.getProcedureNonCoded());
        }

        @Test
        void shouldReturnNullWhenNotExists() {
            assertNull(procedureDAO.getProcedureByUuid("non-existent-uuid"));
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
            newProcedure.setProcedureType(procedureDAO.getProcedureTypeByUuid("procedure-type-uuid-001"));
            newProcedure.setBodySite(conceptService.getConcept(4));
            newProcedure.setStartDateTime(new Date());
            newProcedure.setProcedureNonCoded("New Test Procedure");
            newProcedure.setStatus(conceptService.getConcept(5)); // Completed
            newProcedure.setEstimatedStartDate("2018");

            Procedure saved = procedureDAO.saveOrUpdateProcedure(newProcedure);

            assertNotNull(saved.getProcedureId());
            assertNotNull(saved.getUuid());

            // Verify it can be retrieved
            Procedure retrieved = procedureDAO.getProcedure(saved.getProcedureId());
            assertNotNull(retrieved);
            assertEquals("New Test Procedure", retrieved.getProcedureNonCoded());
        }

        @Test
        void shouldUpdateExistingProcedure() {
            Procedure procedure = procedureDAO.getProcedure(1);
            String originalNotes = procedure.getNotes();

            procedure.setNotes("Updated notes for testing");
            procedureDAO.saveOrUpdateProcedure(procedure);

            // Clear session and retrieve again
            Procedure updated = procedureDAO.getProcedure(1);
            assertEquals("Updated notes for testing", updated.getNotes());
            assertNotEquals(originalNotes, updated.getNotes());
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldRemoveProcedureFromDatabase() {
            Procedure procedure = procedureDAO.getProcedure(5);
            assertNotNull(procedure);

            procedureDAO.deleteProcedure(procedure);

            assertNull(procedureDAO.getProcedure(5));
        }
    }
    
    @Nested
    class ProcedureTypeTests {
       
       @Test
       public void getProcedureTypeByUuid_shouldReturnProcedureTypeWhenExists() {
          ProcedureType type = procedureDAO.getProcedureTypeByUuid("procedure-type-uuid-001");
          
          assertNotNull(type);
          assertEquals("Historical", type.getName());
          assertEquals("Historical procedures", type.getDescription());
       }
       
       @Test
       public void getProcedureTypeByUuid_shouldReturnNullWhenNotExists() {
          assertNull(procedureDAO.getProcedureTypeByUuid("non-existent-uuid"));
       }
       
       @Test
       public void getProcedureType_shouldReturnProcedureTypeWhenExists() {
          ProcedureType type = procedureDAO.getProcedureType(1);
          assertNotNull(type);
          assertEquals("procedure-type-uuid-001", type.getUuid());
          assertEquals("Historical", type.getName());
       }
       
       @Test
       public void getProcedureType_shouldReturnNullWhenNotExists() {
          assertNull(procedureDAO.getProcedureType(999));
       }
       
       @Test
       public void getProcedureTypeByName_shouldReturnProcedureTypeWhenExists() {
          ProcedureType type = procedureDAO.getProcedureTypeByName("Historical");
          assertNotNull(type);
          assertEquals("procedure-type-uuid-001", type.getUuid());
       }
       
       @Test
       public void getProcedureTypeByName_shouldReturnNullWhenNotExists() {
          assertNull(procedureDAO.getProcedureTypeByName("NonExistentType"));
       }
       
       @Test
       public void getAll_ProcedureTypes_shouldReturnOnlyNonRetiredTypes() {
          List<ProcedureType> types = procedureDAO.getAllProcedureTypes(false);
          
          assertNotNull(types);
          assertEquals(2, types.size());
          for (ProcedureType type : types) {
             assertFalse(type.getRetired());
          }
       }
       
       @Test
       public void getAll_ProcedureTypes_shouldReturnResultsSortedByName() {
          List<ProcedureType> types = procedureDAO.getAllProcedureTypes(false);
          
          assertEquals("Current", types.get(0).getName());
          assertEquals("Historical", types.get(1).getName());
       }
       
       @Test
       public void getAll_ProcedureTypes_shouldIncludeRetiredWhenRequested() {
          // Retire one type first
          ProcedureType type = procedureDAO.getProcedureTypeByUuid("procedure-type-uuid-001");
          type.setRetired(true);
          type.setRetireReason("testing");
          procedureDAO.saveOrUpdateProcedure(type);
          
          List<ProcedureType> nonRetired = procedureDAO.getAllProcedureTypes(false);
          List<ProcedureType> all = procedureDAO.getAllProcedureTypes(true);
          
          assertEquals(1, nonRetired.size());
          assertEquals(2, all.size());
       }
       
       @Test
       public void saveOrUpdate_shouldSaveNewProcedureTypeProcedure() {
          ProcedureType newType = new ProcedureType("Emergency", "Emergency procedures");
          
          ProcedureType saved = procedureDAO.saveOrUpdateProcedure(newType);
          
          assertNotNull(saved.getProcedureTypeId());
          assertNotNull(saved.getUuid());
          
          ProcedureType retrieved = procedureDAO.getProcedureTypeByUuid(saved.getUuid());
          assertNotNull(retrieved);
          assertEquals("Emergency", retrieved.getName());
          assertEquals("Emergency procedures", retrieved.getDescription());
       }
       
       @Test
       public void saveOrUpdate_shouldUpdateProcedureExistingProcedureType() {
          ProcedureType type = procedureDAO.getProcedureTypeByUuid("procedure-type-uuid-001");
          type.setName("Updated Historical");
          type.setDescription("Updated description");
          
          procedureDAO.saveOrUpdateProcedure(type);
          
          ProcedureType updated = procedureDAO.getProcedureTypeByUuid("procedure-type-uuid-001");
          assertEquals("Updated Historical", updated.getName());
          assertEquals("Updated description", updated.getDescription());
       }
       
       @Test
       public void delete_ProcedureType_shouldRemoveProcedureTypeFromDatabase() {
          // Create a standalone type not referenced by any procedure
          ProcedureType newType = new ProcedureType("Temporary", "To be deleted");
          ProcedureType saved = procedureDAO.saveOrUpdateProcedure(newType);
          String uuid = saved.getUuid();
          assertNotNull(procedureDAO.getProcedureTypeByUuid(uuid));
          
          procedureDAO.deleteProcedureType(saved);
          
          assertNull(procedureDAO.getProcedureTypeByUuid(uuid));
       }
       
       @Test
       public void getProcedureTypeByUuid_shouldReturnCurrentProcedureType() {
          ProcedureType type = procedureDAO.getProcedureTypeByUuid("cce8ea25-ba2c-4dfe-a386-fba606bc2ef2");
          
          assertNotNull(type);
          assertEquals("Current", type.getName());
          assertEquals("Current procedures", type.getDescription());
          assertFalse(type.getRetired());
       } 
    }
}
