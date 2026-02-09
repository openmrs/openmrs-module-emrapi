/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.emrapi.procedure.Procedure;
import org.openmrs.module.emrapi.procedure.ProcedureService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProcedureControllerTest extends BaseModuleWebContextSensitiveTest {

    private static final String TEST_DATASET = "ProcedureControllerTestDataset.xml";

    private static final String PATIENT_UUID = "procedure-test-patient-uuid";
    private static final String PROCEDURE_CONCEPT_UUID = "procedure-concept-uuid";
   private static final String BODY_SITE_CONCEPT_UUID = "body-site-concept-uuid";
   private static final String STATUS_CONCEPT_UUID = "status-completed-concept-uuid";

    @Autowired
    private ObjectFactory<ProcedureController> controllerFactory;

    @Autowired
    private ProcedureService procedureService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        executeDataSet(TEST_DATASET);
        mockMvc = MockMvcBuilders.standaloneSetup(controllerFactory.getObject()).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void shouldCreateCurrentProcedure() throws Exception {
        String requestBody = "{"
                + "\"patientUuid\": \"" + PATIENT_UUID + "\","
                + "\"codedProcedureUuid\": \"" + PROCEDURE_CONCEPT_UUID + "\","
                + "\"bodySiteUuid\": \"" + BODY_SITE_CONCEPT_UUID + "\","
                + "\"startDateTime\": \"2025-06-15T14:30:00\","
                + "\"duration\": 45,"
                + "\"durationUnit\": \"MINUTES\","
                + "\"statusUuid\": \"" + STATUS_CONCEPT_UUID + "\","
                + "\"notes\": \"Test procedure notes\""
                + "}";

        MvcResult result = mockMvc.perform(post("/rest/v1/emrapi/procedure/current")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        Map<String, Object> response = objectMapper.readValue(jsonResponse, Map.class);

        assertNotNull(response.get("uuid"));
        assertEquals(PATIENT_UUID, response.get("patientUuid"));
        assertEquals(PROCEDURE_CONCEPT_UUID, response.get("codedProcedureUuid"));
        assertEquals(BODY_SITE_CONCEPT_UUID, response.get("bodySiteUuid"));
        assertEquals(45, response.get("duration"));
        assertEquals("MINUTES", response.get("durationUnit"));
        assertEquals("Test procedure notes", response.get("notes"));

        // Verify the procedure was persisted
        String uuid = (String) response.get("uuid");
        Procedure saved = procedureService.getProcedureByUuid(uuid);
        assertNotNull(saved);
        assertEquals("Test procedure notes", saved.getNotes());
    }
}
