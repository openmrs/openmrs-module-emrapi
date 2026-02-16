/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.rest.resource;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.procedure.Procedure;
import org.openmrs.module.emrapi.procedure.ProcedureService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ProcedureResourceTest extends BaseModuleWebContextSensitiveTest {

	private static final String TEST_DATASET = "ProcedureControllerTestDataset.xml";

	private static final String PATIENT_UUID = "procedure-test-patient-uuid";
	private static final String PROCEDURE_CONCEPT_UUID = "procedure-concept-uuid";
	private static final String BODY_SITE_UUID = "body-site-concept-uuid";
	private static final String STATUS_UUID = "status-completed-concept-uuid";
	private static final String DURATION_UNIT_MINUTES_UUID = "duration-unit-minutes-concept-uuid";
	private static final String EXISTING_PROCEDURE_UUID = "existing-procedure-uuid";

	private ProcedureResource resource;

	@Before
	public void setUp() throws Exception {
		executeDataSet(TEST_DATASET);
		resource = (ProcedureResource) Context.getService(RestService.class)
				.getResourceBySupportedClass(Procedure.class);
	}

	@Test
	public void getByUniqueId_shouldReturnProcedureByUuid() {
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
	public void getByUniqueId_shouldReturnNullForUnknownUuid() {
		assertNull(resource.getByUniqueId("non-existent-uuid"));
	}

	@Test
	public void retrieve_shouldReturnRepresentationWithExpectedProperties() throws Exception {
		SimpleObject result = (SimpleObject) resource.retrieve(EXISTING_PROCEDURE_UUID, new RequestContext());

		assertNotNull(result);
		assertEquals(EXISTING_PROCEDURE_UUID, result.get("uuid"));
		assertEquals("Existing procedure for GET test", result.get("notes"));
		assertEquals(Integer.valueOf(45), result.get("duration"));
		assertEquals(Boolean.FALSE, result.get("voided"));

		// Concept fields are REF representations (nested objects with uuid + display)
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

	@Test
	public void doGetAll_shouldReturnProceduresForPatient() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("patient", PATIENT_UUID);
		RequestContext context = new RequestContext();
		context.setRequest(request);

		NeedsPaging<Procedure> result = (NeedsPaging<Procedure>) resource.doGetAll(context);

		assertNotNull(result);
		List<Procedure> procedures = result.getPageOfResults();
		assertEquals(1, procedures.size());
		assertEquals(EXISTING_PROCEDURE_UUID, procedures.get(0).getUuid());
		assertEquals("Existing procedure for GET test", procedures.get(0).getNotes());
	}

	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void doGetAll_shouldThrowWhenNoPatientParameter() throws Exception {
		resource.doGetAll(new RequestContext());
	}

	@Test
	public void create_shouldCreateProcedure() throws Exception {
		SimpleObject properties = new SimpleObject();
		properties.add("patient", PATIENT_UUID);
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

		// Verify persisted to DB
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
	public void create_shouldCreateProcedureWithFreeTextFields() throws Exception {
		SimpleObject properties = new SimpleObject();
		properties.add("patient", PATIENT_UUID);
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

	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void delete_shouldThrowUnsupportedOperation() throws Exception {
		resource.delete(EXISTING_PROCEDURE_UUID, "test reason", new RequestContext());
	}

	@Test(expected = ResourceDoesNotSupportOperationException.class)
	public void purge_shouldThrowUnsupportedOperation() throws Exception {
		resource.purge(EXISTING_PROCEDURE_UUID, new RequestContext());
	}
}
