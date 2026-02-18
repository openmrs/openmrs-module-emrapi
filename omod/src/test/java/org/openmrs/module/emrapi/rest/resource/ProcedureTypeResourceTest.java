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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.procedure.ProcedureType;
import org.openmrs.module.emrapi.procedure.ProcedureTypeService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProcedureTypeResourceTest extends BaseModuleWebContextSensitiveTest {

	private static final String TEST_DATASET = "ProcedureControllerTestDataset.xml";

	private static final String HISTORICAL_TYPE_UUID = "historical-procedure-type-uuid";
	private static final String CURRENT_TYPE_UUID = "cce8ea25-ba2c-4dfe-a386-fba606bc2ef2";

	private ProcedureTypeResource resource;

	@BeforeEach
	void setUp() {
		executeDataSet(TEST_DATASET);
		resource = (ProcedureTypeResource) Context.getService(RestService.class)
				.getResourceBySupportedClass(ProcedureType.class);
	}

	@Nested
	@DisplayName("getByUniqueId")
	class GetByUniqueId {

		@Test
		void shouldReturnProcedureTypeByUuid() {
			ProcedureType type = resource.getByUniqueId(HISTORICAL_TYPE_UUID);

			assertNotNull(type);
			assertEquals(HISTORICAL_TYPE_UUID, type.getUuid());
			assertEquals("Historical", type.getName());
			assertEquals("Historical procedures", type.getDescription());
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
		void shouldReturnDefaultRepresentationWithExpectedProperties() {
			SimpleObject result = (SimpleObject) resource.retrieve(HISTORICAL_TYPE_UUID, new RequestContext());

			assertNotNull(result);
			assertEquals(HISTORICAL_TYPE_UUID, result.get("uuid"));
			assertEquals("Historical", result.get("name"));
			assertEquals("Historical procedures", result.get("description"));
			assertEquals(Boolean.FALSE, result.get("retired"));
		}
	}

	@Nested
	@DisplayName("getAll")
	class GetAll {

		@Test
		@SuppressWarnings("unchecked")
		void shouldReturnAllNonRetiredProcedureTypes() {
			NeedsPaging<ProcedureType> result = (NeedsPaging<ProcedureType>) resource.doGetAll(new RequestContext());

			assertNotNull(result);
			List<ProcedureType> types = result.getPageOfResults();
			assertEquals(2, types.size());
		}
	}

	@Nested
	@DisplayName("create")
	class Create {

		@Test
		void shouldCreateProcedureType() {
			SimpleObject properties = new SimpleObject();
			properties.add("name", "Emergency");
			properties.add("description", "Emergency procedures");

			SimpleObject created = (SimpleObject) resource.create(properties, new RequestContext());

			assertNotNull(created);
			String uuid = (String) created.get("uuid");
			assertNotNull(uuid);
			assertEquals("Emergency", created.get("name"));
			assertEquals("Emergency procedures", created.get("description"));

			ProcedureType saved = Context.getService(ProcedureTypeService.class).getProcedureTypeByUuid(uuid);
			assertNotNull(saved);
			assertEquals("Emergency", saved.getName());
			assertEquals("Emergency procedures", saved.getDescription());
		}

		@Test
		void shouldCreateProcedureTypeWithNameOnly() {
			SimpleObject properties = new SimpleObject();
			properties.add("name", "Minimal Type");

			SimpleObject created = (SimpleObject) resource.create(properties, new RequestContext());

			assertNotNull(created);
			assertEquals("Minimal Type", created.get("name"));
		}
      
      // should not allow creation without name
      @Test
      void shouldNotCreateProcedureTypeWithoutName() {
         SimpleObject properties = new SimpleObject();
         properties.add("description", "Missing name");
         assertThrows(ConversionException.class,
                 () -> resource.create(properties, new RequestContext()));
      }
	}

	@Nested
	@DisplayName("update")
	class Update {

		@Test
		void shouldUpdateProcedureType() {
			SimpleObject properties = new SimpleObject();
			properties.add("name", "Updated Historical");
			properties.add("description", "Updated description");

			SimpleObject updated = (SimpleObject) resource.update(HISTORICAL_TYPE_UUID, properties, new RequestContext());

			assertNotNull(updated);
			assertEquals("Updated Historical", updated.get("name"));
			assertEquals("Updated description", updated.get("description"));

			ProcedureType saved = Context.getService(ProcedureTypeService.class).getProcedureTypeByUuid(HISTORICAL_TYPE_UUID);
			assertEquals("Updated Historical", saved.getName());
			assertEquals("Updated description", saved.getDescription());
		}
	}

	@Nested
	@DisplayName("delete")
	class Delete {

		@Test
		void shouldRetireProcedureType() {
			resource.delete(HISTORICAL_TYPE_UUID, "no longer needed", new RequestContext());

			ProcedureType retired = Context.getService(ProcedureTypeService.class).getProcedureTypeByUuid(HISTORICAL_TYPE_UUID);
			assertNotNull(retired);
			assertTrue(retired.getRetired());
			assertEquals("no longer needed", retired.getRetireReason());
		}
	}

	@Nested
	@DisplayName("purge")
	class Purge {

		@Test
		void shouldPermanentlyDeleteProcedureType() {
			resource.purge(HISTORICAL_TYPE_UUID, new RequestContext());

			ProcedureType purged = Context.getService(ProcedureTypeService.class).getProcedureTypeByUuid(HISTORICAL_TYPE_UUID);
			assertNull(purged);
		}
	}
}
