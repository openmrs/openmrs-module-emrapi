/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.emrapi.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.procedure.Procedure;
import org.openmrs.module.emrapi.procedure.ProcedureService;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for {@link ProcedureController} using mocks.
 */
public class ProcedureControllerUnitTest {

    private static final String PATIENT_UUID = "patient-uuid";
    private static final String PROCEDURE_CONCEPT_UUID = "procedure-concept-uuid";
    private static final String BODY_SITE_CONCEPT_UUID = "body-site-concept-uuid";

    private MockMvc mockMvc;

    private ProcedureService procedureService;
    private PatientService patientService;
    private ConceptService conceptService;
    private EncounterService encounterService;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        procedureService = mock(ProcedureService.class);
        patientService = mock(PatientService.class);
        conceptService = mock(ConceptService.class);
        encounterService = mock(EncounterService.class);

        ProcedureController controller = new ProcedureController();
        ReflectionTestUtils.setField(controller, "procedureService", procedureService);
        ReflectionTestUtils.setField(controller, "patientService", patientService);
        ReflectionTestUtils.setField(controller, "conceptService", conceptService);
        ReflectionTestUtils.setField(controller, "encounterService", encounterService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void shouldCreateCurrentProcedure() throws Exception {
        // Setup mock data
        Patient patient = new Patient();
        patient.setUuid(PATIENT_UUID);

        Concept procedureConcept = new Concept();
        procedureConcept.setUuid(PROCEDURE_CONCEPT_UUID);

        Concept bodySiteConcept = new Concept();
        bodySiteConcept.setUuid(BODY_SITE_CONCEPT_UUID);

        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
        when(conceptService.getConceptByUuid(PROCEDURE_CONCEPT_UUID)).thenReturn(procedureConcept);
        when(conceptService.getConceptByUuid(BODY_SITE_CONCEPT_UUID)).thenReturn(bodySiteConcept);

        // Mock the save operation to return a procedure with UUID
        when(procedureService.saveProcedure(any(Procedure.class))).thenAnswer(invocation -> {
            Procedure p = invocation.getArgument(0);
            p.setUuid("new-procedure-uuid");
            return p;
        });

        String requestBody = "{"
                + "\"patientUuid\": \"" + PATIENT_UUID + "\","
                + "\"codedProcedureUuid\": \"" + PROCEDURE_CONCEPT_UUID + "\","
                + "\"bodySiteUuid\": \"" + BODY_SITE_CONCEPT_UUID + "\","
                + "\"startDateTime\": \"2023-06-15\","
                + "\"duration\": 45,"
                + "\"durationUnit\": \"MINUTES\","
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

        // Verify the service was called
        verify(procedureService).saveProcedure(any(Procedure.class));
    }
}
