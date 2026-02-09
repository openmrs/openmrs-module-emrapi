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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.procedure.Procedure;
import org.openmrs.module.emrapi.procedure.ProcedureService;
import org.openmrs.module.emrapi.web.dto.ProcedureDTO;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProcedureControllerTest {

    @Mock
    private ProcedureService procedureService;

    @Mock
    private PatientService patientService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private EncounterService encounterService;

    @InjectMocks
    private ProcedureController controller;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private static final String BASE_URL = "/rest/v1/emrapi/procedure";

    private static final String PATIENT_UUID = "patient-uuid-123";
    private static final String PROCEDURE_UUID = "procedure-uuid-456";
    private static final String CONCEPT_UUID = "concept-uuid-789";
    private static final String BODY_SITE_UUID = "body-site-uuid-012";
    private static final String STATUS_UUID = "status-uuid-345";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    private Procedure buildProcedure() {
        Patient patient = new Patient();
        patient.setUuid(PATIENT_UUID);

        Concept procedureConcept = new Concept();
        procedureConcept.setUuid(CONCEPT_UUID);

        Concept bodySite = new Concept();
        bodySite.setUuid(BODY_SITE_UUID);

        Concept status = new Concept();
        status.setUuid(STATUS_UUID);

        Procedure procedure = new Procedure();
        procedure.setUuid(PROCEDURE_UUID);
        procedure.setPatient(patient);
        procedure.setProcedureCoded(procedureConcept);
        procedure.setBodySite(bodySite);
        procedure.setStatus(status);
        procedure.setStartDateTime(new Date());
        procedure.setDuration(45);
        procedure.setDurationUnit(Procedure.DurationUnit.MINUTES);
        procedure.setNotes("Test notes");
        return procedure;
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
            Patient patient = new Patient();
            patient.setUuid(PATIENT_UUID);
            Procedure procedure = buildProcedure();

            when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
            when(procedureService.getProceduresByPatient(patient)).thenReturn(Arrays.asList(procedure));

            MvcResult result = mockMvc.perform(get(BASE_URL).param("patient", PATIENT_UUID))
                    .andExpect(status().isOk())
                    .andReturn();

            List<Map<String, Object>> response = parseListResponse(result);
            assertEquals(1, response.size());
            assertEquals(PROCEDURE_UUID, response.get(0).get("uuid"));
            assertEquals(PATIENT_UUID, response.get(0).get("patientUuid"));
            assertEquals("Test notes", response.get(0).get("notes"));
        }
    }

    @Nested
    @DisplayName("GET /rest/v1/emrapi/procedure/{uuid}")
    class GetProcedureByUuid {

        @Test
        void shouldReturnProcedureByUuid() throws Exception {
            Procedure procedure = buildProcedure();
            when(procedureService.getProcedureByUuid(PROCEDURE_UUID)).thenReturn(procedure);

            MvcResult result = mockMvc.perform(get(BASE_URL + "/" + PROCEDURE_UUID))
                    .andExpect(status().isOk())
                    .andReturn();

            Map<String, Object> response = parseResponse(result);
            assertEquals(PROCEDURE_UUID, response.get("uuid"));
            assertEquals(PATIENT_UUID, response.get("patientUuid"));
            assertEquals(CONCEPT_UUID, response.get("codedProcedureUuid"));
            assertEquals(BODY_SITE_UUID, response.get("bodySiteUuid"));
            assertEquals(STATUS_UUID, response.get("statusUuid"));
            assertEquals(45, response.get("duration"));
            assertEquals("MINUTES", response.get("durationUnit"));
            assertEquals("Test notes", response.get("notes"));
        }
    }

    @Nested
    @DisplayName("POST /rest/v1/emrapi/procedure/current")
    class CreateCurrentProcedure {

        @Test
        void shouldCreateCurrentProcedure() throws Exception {
            Patient patient = new Patient();
            patient.setUuid(PATIENT_UUID);

            Concept procedureConcept = new Concept();
            procedureConcept.setUuid(CONCEPT_UUID);

            Concept bodySite = new Concept();
            bodySite.setUuid(BODY_SITE_UUID);

            Concept status = new Concept();
            status.setUuid(STATUS_UUID);

            when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
            when(conceptService.getConceptByUuid(CONCEPT_UUID)).thenReturn(procedureConcept);
            when(conceptService.getConceptByUuid(BODY_SITE_UUID)).thenReturn(bodySite);
            when(conceptService.getConceptByUuid(STATUS_UUID)).thenReturn(status);
            when(procedureService.saveProcedure(any(Procedure.class))).thenAnswer(i -> {
                Procedure p = i.getArgument(0);
                p.setUuid(PROCEDURE_UUID);
                return p;
            });

            ProcedureDTO dto = new ProcedureDTO();
            dto.setPatientUuid(PATIENT_UUID);
            dto.setCodedProcedureUuid(CONCEPT_UUID);
            dto.setBodySiteUuid(BODY_SITE_UUID);
            dto.setStatusUuid(STATUS_UUID);
            dto.setStartDateTime(new Date());
            dto.setDuration(45);
            dto.setDurationUnit("MINUTES");
            dto.setNotes("Current procedure");

            MvcResult result = mockMvc.perform(post(BASE_URL + "/current")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andReturn();

            Map<String, Object> response = parseResponse(result);
            assertEquals(PROCEDURE_UUID, response.get("uuid"));
            assertEquals(PATIENT_UUID, response.get("patientUuid"));
            assertEquals("Current procedure", response.get("notes"));
        }
    }

    @Nested
    @DisplayName("POST /rest/v1/emrapi/procedure/historical")
    class CreateHistoricalProcedure {

        @Test
        void shouldCreateHistoricalProcedure() throws Exception {
            Patient patient = new Patient();
            patient.setUuid(PATIENT_UUID);

            Concept procedureConcept = new Concept();
            procedureConcept.setUuid(CONCEPT_UUID);

            Concept bodySite = new Concept();
            bodySite.setUuid(BODY_SITE_UUID);

            Concept status = new Concept();
            status.setUuid(STATUS_UUID);

            when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(patient);
            when(conceptService.getConceptByUuid(CONCEPT_UUID)).thenReturn(procedureConcept);
            when(conceptService.getConceptByUuid(BODY_SITE_UUID)).thenReturn(bodySite);
            when(conceptService.getConceptByUuid(STATUS_UUID)).thenReturn(status);
            when(procedureService.saveProcedure(any(Procedure.class))).thenAnswer(i -> {
                Procedure p = i.getArgument(0);
                p.setUuid(PROCEDURE_UUID);
                return p;
            });

            ProcedureDTO dto = new ProcedureDTO();
            dto.setPatientUuid(PATIENT_UUID);
            dto.setCodedProcedureUuid(CONCEPT_UUID);
            dto.setBodySiteUuid(BODY_SITE_UUID);
            dto.setStatusUuid(STATUS_UUID);
            dto.setEstimatedStartDate("2020-06");
            dto.setNotes("Historical procedure");

            MvcResult result = mockMvc.perform(post(BASE_URL + "/historical")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andReturn();

            Map<String, Object> response = parseResponse(result);
            assertEquals(PROCEDURE_UUID, response.get("uuid"));
            assertEquals("2020-06", response.get("estimatedStartDate"));
            assertEquals("Historical procedure", response.get("notes"));
        }
    }
}