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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.openmrs.module.emrapi.procedure.Procedure;
import org.openmrs.module.emrapi.procedure.ProcedureService;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProcedureControllerTest extends BaseModuleWebContextSensitiveTest {

    private static final String TEST_DATASET = "ProcedureControllerTestDataset.xml";

    private static final String PATIENT_UUID = "procedure-test-patient-uuid";
    private static final String PROCEDURE_CONCEPT_UUID = "procedure-concept-uuid";
    private static final String BODY_SITE_UUID = "body-site-concept-uuid";
    private static final String STATUS_UUID = "status-completed-concept-uuid";
    private static final String EXISTING_PROCEDURE_UUID = "existing-procedure-uuid";

    private static final String BASE_URL = "/rest/v1/emrapi/procedure";

    @Autowired
    private ObjectFactory<ProcedureController> controllerFactory;

    @Autowired
    private ProcedureService procedureService;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        executeDataSet(TEST_DATASET);
        mockMvc = MockMvcBuilders.standaloneSetup(controllerFactory.getObject()).build();
        objectMapper = new ObjectMapper();
    }

    private Map<String, Object> parseResponse(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
    }

    private List<Map<String, Object>> parseListResponse(MvcResult result) throws Exception {
        return objectMapper.readValue(result.getResponse().getContentAsString(), List.class);
    }

    @Nested
    @DisplayName("GET /rest/v1/emrapi/procedure?patient={uuid}")
    class GetProceduresByPatient {

        @Test
        void shouldReturnProceduresForPatient() throws Exception {
            MvcResult result = mockMvc.perform(get(BASE_URL).param("patient", PATIENT_UUID))
                    .andExpect(status().isOk())
                    .andReturn();

            List<Map<String, Object>> response = parseListResponse(result);
            assertEquals(1, response.size());
            assertEquals(EXISTING_PROCEDURE_UUID, response.get(0).get("uuid"));
            assertEquals(PATIENT_UUID, response.get(0).get("patientUuid"));
            assertEquals("Existing procedure for GET test", response.get(0).get("notes"));
        }
    }

    @Nested
    @DisplayName("GET /rest/v1/emrapi/procedure/{uuid}")
    class GetProcedureByUuid {

        @Test
        void shouldReturnProcedureByUuid() throws Exception {
            MvcResult result = mockMvc.perform(get(BASE_URL + "/" + EXISTING_PROCEDURE_UUID))
                    .andExpect(status().isOk())
                    .andReturn();

            Map<String, Object> response = parseResponse(result);
            assertEquals(EXISTING_PROCEDURE_UUID, response.get("uuid"));
            assertEquals(PATIENT_UUID, response.get("patientUuid"));
            assertEquals(PROCEDURE_CONCEPT_UUID, response.get("codedProcedureUuid"));
            assertEquals(BODY_SITE_UUID, response.get("bodySiteUuid"));
            assertEquals(STATUS_UUID, response.get("statusUuid"));
            assertEquals(45, response.get("duration"));
            assertEquals("MINUTES", response.get("durationUnit"));
            assertEquals("Existing procedure for GET test", response.get("notes"));
        }
    }

    @Nested
    @DisplayName("POST /rest/v1/emrapi/procedure/current")
    class CreateCurrentProcedure {

        @Test
        void shouldCreateCurrentProcedure() throws Exception {
            String requestBody = "{"
                    + "\"patientUuid\": \"" + PATIENT_UUID + "\","
                    + "\"codedProcedureUuid\": \"" + PROCEDURE_CONCEPT_UUID + "\","
                    + "\"bodySiteUuid\": \"" + BODY_SITE_UUID + "\","
                    + "\"statusUuid\": \"" + STATUS_UUID + "\","
                    + "\"startDateTime\": \"2025-06-15T14:30:00\","
                    + "\"duration\": 45,"
                    + "\"durationUnit\": \"MINUTES\","
                    + "\"notes\": \"Current procedure notes\""
                    + "}";

            MvcResult result = mockMvc.perform(post(BASE_URL + "/current")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            Map<String, Object> response = parseResponse(result);
            assertNotNull(response.get("uuid"));
            assertEquals(PATIENT_UUID, response.get("patientUuid"));
            assertEquals(PROCEDURE_CONCEPT_UUID, response.get("codedProcedureUuid"));
            assertEquals(BODY_SITE_UUID, response.get("bodySiteUuid"));
            assertEquals(45, response.get("duration"));
            assertEquals("MINUTES", response.get("durationUnit"));
            assertEquals("Current procedure notes", response.get("notes"));

            // Verify persisted to DB
            String uuid = (String) response.get("uuid");
            Procedure saved = procedureService.getProcedureByUuid(uuid);
            assertNotNull(saved);
            assertEquals("Current procedure notes", saved.getNotes());
        }
    }

    @Nested
    @DisplayName("POST /rest/v1/emrapi/procedure/historical")
    class CreateHistoricalProcedure {

        @Test
        void shouldCreateHistoricalProcedure() throws Exception {
            String requestBody = "{"
                    + "\"patientUuid\": \"" + PATIENT_UUID + "\","
                    + "\"codedProcedureUuid\": \"" + PROCEDURE_CONCEPT_UUID + "\","
                    + "\"bodySiteUuid\": \"" + BODY_SITE_UUID + "\","
                    + "\"statusUuid\": \"" + STATUS_UUID + "\","
                    + "\"estimatedStartDate\": \"2020-06\","
                    + "\"notes\": \"Historical procedure notes\""
                    + "}";

            MvcResult result = mockMvc.perform(post(BASE_URL + "/historical")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isCreated())
                    .andReturn();

            Map<String, Object> response = parseResponse(result);
            assertNotNull(response.get("uuid"));
            assertEquals("2020-06", response.get("estimatedStartDate"));
            assertEquals("Historical procedure notes", response.get("notes"));

            // Verify persisted to DB
            String uuid = (String) response.get("uuid");
            Procedure saved = procedureService.getProcedureByUuid(uuid);
            assertNotNull(saved);
            assertEquals("2020-06", saved.getEstimatedStartDate());
        }
    }
}