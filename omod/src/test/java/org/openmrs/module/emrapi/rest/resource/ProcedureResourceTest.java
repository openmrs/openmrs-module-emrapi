/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under the terms
 * of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.rest.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.procedure.Procedure;
import org.openmrs.module.emrapi.procedure.ProcedureService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcedureResourceTest extends BaseModuleWebContextSensitiveTest {
   
   private static final String TEST_DATASET = "ProcedureDataset.xml";
   
   private static final String PATIENT_UUID = "procedure-test-patient-uuid";
   
   private static final String PROCEDURE_CONCEPT_UUID = "procedure-concept-uuid";
   
   private static final String BODY_SITE_UUID = "body-site-concept-uuid";
   
   private static final String STATUS_UUID = "status-completed-concept-uuid";
   
   private static final String DURATION_UNIT_MINUTES_UUID = "duration-unit-minutes-concept-uuid";
   
   private static final String EXISTING_PROCEDURE_UUID = "existing-procedure-uuid";
   
   private static final String HISTORICAL_PROCEDURE_TYPE_UUID = "historical-procedure-type-uuid";
   
   private static final String CURRENT_PROCEDURE_TYPE_UUID = "cce8ea25-ba2c-4dfe-a386-fba606bc2ef2";
   
   private static final String ENCOUNTER_UUID = "procedure-test-encounter-uuid";
   
   private ProcedureResource resource;
   
   @BeforeEach
   void setUp() {
      executeDataSet(TEST_DATASET);
      resource = (ProcedureResource) Context.getService(RestService.class)
              .getResourceBySupportedClass(Procedure.class);
   }
   
   @Nested
   @DisplayName("getByUniqueId")
   class GetByUniqueId {
      
      @Test
      void shouldReturnProcedureByUuid() {
         Procedure procedure = resource.getByUniqueId(EXISTING_PROCEDURE_UUID);
         
         assertNotNull(procedure);
         assertEquals(EXISTING_PROCEDURE_UUID, procedure.getUuid());
         assertEquals(PROCEDURE_CONCEPT_UUID, procedure.getProcedureCoded().getUuid());
         assertEquals(BODY_SITE_UUID, procedure.getBodySite().getUuid());
         assertEquals(STATUS_UUID, procedure.getStatus().getUuid());
         assertEquals(Integer.valueOf(45), procedure.getDuration());
         assertEquals(DURATION_UNIT_MINUTES_UUID, procedure.getDurationUnit().getUuid());
         assertEquals("Existing procedure for GET test", procedure.getNotes());
      }
      
      @Test
      void shouldReturnNullForUnknownUuid() {
         assertNull(resource.getByUniqueId("non-existent-uuid"));
      }
   }
   
   @Nested
   @DisplayName("retrieve")
   class Retrieve {
      
      @Test
      void shouldReturnRepresentationWithExpectedProperties() throws Exception {
         SimpleObject result = (SimpleObject) resource.retrieve(EXISTING_PROCEDURE_UUID, new RequestContext());
         
         assertNotNull(result);
         assertEquals(EXISTING_PROCEDURE_UUID, result.get("uuid"));
         assertEquals("Existing procedure for GET test", result.get("notes"));
         assertEquals(Integer.valueOf(45), result.get("duration"));
         assertEquals(Boolean.FALSE, result.get("voided"));
         
         Map<String, Object> procedureCoded = result.get("procedureCoded");
         assertNotNull(procedureCoded);
         assertEquals(PROCEDURE_CONCEPT_UUID, procedureCoded.get("uuid"));
         
         Map<String, Object> bodySite = result.get("bodySite");
         assertNotNull(bodySite);
         assertEquals(BODY_SITE_UUID, bodySite.get("uuid"));
         
         Map<String, Object> status = result.get("status");
         assertNotNull(status);
         assertEquals(STATUS_UUID, status.get("uuid"));
         
         Map<String, Object> durationUnit = result.get("durationUnit");
         assertNotNull(durationUnit);
         assertEquals(DURATION_UNIT_MINUTES_UUID, durationUnit.get("uuid"));
         
         Map<String, Object> patient = result.get("patient");
         assertNotNull(patient);
         assertEquals(PATIENT_UUID, patient.get("uuid"));
      }
   }
   
   @Nested
   @DisplayName("GetAll")
   class GetAll {
      
      @Test
      void shouldReturnProceduresForPatient() throws Exception {
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.setParameter("patient", PATIENT_UUID);
         RequestContext context = new RequestContext();
         context.setRequest(request);
         
         AlreadyPaged<Procedure> result = (AlreadyPaged<Procedure>) resource.doGetAll(context);
         
         assertNotNull(result);
         List<Procedure> procedures = result.getPageOfResults();
         assertEquals(3, procedures.size());
         assertEquals(EXISTING_PROCEDURE_UUID, procedures.get(0).getUuid());
         assertEquals("Existing procedure for GET test", procedures.get(0).getNotes());
      }
      
      @Test
      void shouldThrowWhenNoPatientParameter() {
         assertThrows(ResourceDoesNotSupportOperationException.class,
                 () -> resource.doGetAll(new RequestContext()));
      }
      
      @Test
      void shouldReturnFirstPageWhenLimitIsSet() throws Exception {
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.setParameter("patient", PATIENT_UUID);
         RequestContext context = new RequestContext();
         context.setRequest(request);
         context.setLimit(2);
         
         AlreadyPaged<Procedure> result = (AlreadyPaged<Procedure>) resource.doGetAll(context);
         List<Procedure> procedures = result.getPageOfResults();

         assertEquals(2, procedures.size());
         assertEquals(EXISTING_PROCEDURE_UUID, procedures.get(0).getUuid());
         assertEquals("procedure-uuid-page-2", procedures.get(1).getUuid());
      }

      @Test
      void shouldReturnOffsetPageWhenStartIndexIsSet() throws Exception {
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.setParameter("patient", PATIENT_UUID);
         RequestContext context = new RequestContext();
         context.setStartIndex(1);
         context.setLimit(1);
         context.setRequest(request);

         AlreadyPaged<Procedure> result = (AlreadyPaged<Procedure>) resource.doGetAll(context);
         List<Procedure> procedures = result.getPageOfResults();

         assertEquals(1, procedures.size());
         assertEquals("procedure-uuid-page-2", procedures.get(0).getUuid());
      }

      @Test
      void shouldSetHasMoreTrueWhenMoreResultsExist() throws Exception {
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.setParameter("patient", PATIENT_UUID);
         RequestContext context = new RequestContext();
         context.setRequest(request);
         context.setStartIndex(0);
         context.setLimit(2);
         AlreadyPaged<Procedure> result = (AlreadyPaged<Procedure>) resource.doGetAll(context);

         assertTrue(result.hasMoreResults());
      }

      @Test
      void shouldSetHasMoreFalseOnLastPage() throws Exception {
         MockHttpServletRequest request = new MockHttpServletRequest();
         request.setParameter("patient", PATIENT_UUID);
         RequestContext context = new RequestContext();
         context.setRequest(request);
         context.setStartIndex(2);
         context.setLimit(2);

         AlreadyPaged<Procedure> result = (AlreadyPaged<Procedure>) resource.doGetAll(context);

         assertFalse(result.hasMoreResults());
      }
   }
   
   @Nested
   @DisplayName("create")
   class Create {
      
      @Test
      void shouldCreateHistoricalProcedure() throws Exception {
         SimpleObject properties = new SimpleObject();
         properties.add("patient", PATIENT_UUID);
         properties.add("procedureType", HISTORICAL_PROCEDURE_TYPE_UUID);
         properties.add("procedureCoded", PROCEDURE_CONCEPT_UUID);
         properties.add("bodySite", BODY_SITE_UUID);
         properties.add("status", STATUS_UUID);
         properties.add("duration", 60);
         properties.add("durationUnit", DURATION_UNIT_MINUTES_UUID);
         properties.add("notes", "New procedure via resource");
         properties.add("estimatedStartDate", "2020-06");
         
         SimpleObject created = (SimpleObject) resource.create(properties, new RequestContext());
         
         assertNotNull(created);
         String uuid = (String) created.get("uuid");
         assertNotNull(uuid);
         assertEquals("New procedure via resource", created.get("notes"));
         assertEquals(Integer.valueOf(60), created.get("duration"));
         assertEquals("2020-06", created.get("estimatedStartDate"));
         
         Procedure saved = Context.getService(ProcedureService.class).getProcedureByUuid(uuid);
         assertNotNull(saved);
         assertEquals("New procedure via resource", saved.getNotes());
         assertEquals(Integer.valueOf(60), saved.getDuration());
         assertEquals(PATIENT_UUID, saved.getPatient().getUuid());
         assertEquals(PROCEDURE_CONCEPT_UUID, saved.getProcedureCoded().getUuid());
         assertEquals(BODY_SITE_UUID, saved.getBodySite().getUuid());
         assertEquals(STATUS_UUID, saved.getStatus().getUuid());
         assertEquals("2020-06", saved.getEstimatedStartDate());
      }
      
      @Test
      void shouldCreateValidCurrentProcedure() throws Exception {
         SimpleObject properties = new SimpleObject();
         properties.add("patient", PATIENT_UUID);
         properties.add("procedureType", CURRENT_PROCEDURE_TYPE_UUID);
         properties.add("encounter", ENCOUNTER_UUID);
         properties.add("procedureCoded", PROCEDURE_CONCEPT_UUID);
         properties.add("bodySite", BODY_SITE_UUID);
         properties.add("status", STATUS_UUID);
         properties.add("startDateTime", "2023-06-15T10:00:00.000+0000");
         properties.add("notes", "Current procedure test");
         
         SimpleObject created = (SimpleObject) resource.create(properties, new RequestContext());
         
         assertNotNull(created);
         String uuid = (String) created.get("uuid");
         assertNotNull(uuid);
         assertEquals("Current procedure test", created.get("notes"));
         
         Procedure saved = Context.getService(ProcedureService.class).getProcedureByUuid(uuid);
         assertNotNull(saved);
         assertEquals(CURRENT_PROCEDURE_TYPE_UUID, saved.getProcedureType().getUuid());
         assertEquals(ENCOUNTER_UUID, saved.getEncounter().getUuid());
      }
      
      @Test
      void shouldRequireStartDateTime() {
         SimpleObject properties = new SimpleObject();
         properties.add("patient", PATIENT_UUID);
         properties.add("procedureType", HISTORICAL_PROCEDURE_TYPE_UUID);
         properties.add("procedureCoded", PROCEDURE_CONCEPT_UUID);
         properties.add("bodySite", BODY_SITE_UUID);
         properties.add("status", STATUS_UUID);
         
         assertThrows(APIException.class, () -> resource.create(properties, new RequestContext()));
      }
      
      @Test
      void shouldCreateProcedureWithFreeTextFields() throws Exception {
         SimpleObject properties = new SimpleObject();
         properties.add("patient", PATIENT_UUID);
         properties.add("procedureType", HISTORICAL_PROCEDURE_TYPE_UUID);
         properties.add("procedureNonCoded", "Some unlisted procedure");
         properties.add("startDateTime", "2020-06-15T10:00:00.000+0000");
         properties.add("bodySite", BODY_SITE_UUID);
         properties.add("status", STATUS_UUID);
         properties.add("outcomeNonCoded", "Patient recovered well");
         properties.add("notes", "Free text procedure test");
         
         SimpleObject created = (SimpleObject) resource.create(properties, new RequestContext());
         
         assertNotNull(created);
         String uuid = (String) created.get("uuid");
         assertEquals("Some unlisted procedure", created.get("procedureNonCoded"));
         assertEquals("Patient recovered well", created.get("outcomeNonCoded"));
         
         Procedure saved = Context.getService(ProcedureService.class).getProcedureByUuid(uuid);
         assertNotNull(saved);
         assertEquals("Some unlisted procedure", saved.getProcedureNonCoded());
         assertEquals("Patient recovered well", saved.getOutcomeNonCoded());
      }
   }
   
   @Nested
   @DisplayName("delete")
   class Delete {
      
      @Test
      void shouldVoidProcedure() throws Exception {
         resource.delete(EXISTING_PROCEDURE_UUID, "testing void", new RequestContext());
         
         Procedure voided = Context.getService(ProcedureService.class).getProcedureByUuid(EXISTING_PROCEDURE_UUID);
         assertNotNull(voided);
         assertTrue(voided.getVoided());
         assertEquals("testing void", voided.getVoidReason());
      }
      
      @Test
      void shouldNotAllowDeleteWithoutReason() {
         assertThrows(APIException.class,
                 () -> resource.delete(EXISTING_PROCEDURE_UUID, null, new RequestContext()));
      }
   }
   
   @Nested
   @DisplayName("purge")
   class Purge {
      
      @Test
      void shouldPermanentlyDeleteProcedure() throws Exception {
         resource.purge(EXISTING_PROCEDURE_UUID, new RequestContext());
         
         Procedure purged = Context.getService(ProcedureService.class).getProcedureByUuid(EXISTING_PROCEDURE_UUID);
         assertNull(purged);
      }
   }
}
