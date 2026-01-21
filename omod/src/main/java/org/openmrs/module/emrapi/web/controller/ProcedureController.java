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

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.procedure.Procedure;
import org.openmrs.module.emrapi.procedure.ProcedureService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for managing Procedure records.
 * Provides endpoints for creating, retrieving, updating, and voiding procedures.
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/emrapi/procedure")
public class ProcedureController extends BaseRestController {

    @Autowired
    private ProcedureService procedureService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EncounterService encounterService;

    /**
     * Gets all procedures for a patient.
     *
     * GET /rest/v1/emrapi/procedure?patient={uuid}
     * GET /rest/v1/emrapi/procedure?patient={uuid}&historical=true
     *
     * @param patientUuid the patient UUID
     * @return list of procedure DTOs
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<ProcedureDTO> getProcedures(
            @RequestParam("patient") String patientUuid) {

        Patient patient = patientService.getPatientByUuid(patientUuid);
        if (patient == null) {
            throw new IllegalArgumentException("Patient not found with UUID: " + patientUuid);
        }

        List<Procedure> procedures= procedureService.getProceduresByPatient(patient);

        List<ProcedureDTO> dtos = new ArrayList<>();
        for (Procedure procedure : procedures) {
            dtos.add(toDTO(procedure));
        }
        return dtos;
    }

    /**
     * Gets a procedure by UUID.
     *
     * GET /rest/v1/emrapi/procedure/{uuid}
     *
     * @param uuid the procedure UUID
     * @return the procedure DTO
     */
    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<ProcedureDTO> getProcedure(@PathVariable("uuid") String uuid) {
        Procedure procedure = procedureService.getProcedureByUuid(uuid);
        if (procedure == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(toDTO(procedure), HttpStatus.OK);
    }

    /**
     * Creates a historical procedure.
     * Historical procedures require an originalDateText value.
     *
     * POST /rest/v1/emrapi/procedure/historical
     *
     * @param dto the procedure data
     * @return the created procedure DTO
     */
    @RequestMapping(value = "/historical", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<ProcedureDTO> createHistoricalProcedure(@RequestBody ProcedureDTO dto) {
        Procedure procedure = fromDTO(dto);
        procedure = procedureService.saveProcedure(procedure);
        return new ResponseEntity<>(toDTO(procedure), HttpStatus.CREATED);
    }

    /**
     * Creates a current (real-time) procedure.
     * Any originalDateText value is ignored.
     *
     * POST /rest/v1/emrapi/procedure/current
     *
     * @param dto the procedure data
     * @return the created procedure DTO
     */
    @RequestMapping(value = "/current", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<ProcedureDTO> createCurrentProcedure(@RequestBody ProcedureDTO dto) {
        Procedure procedure = fromDTO(dto);
        procedure = procedureService.saveProcedure(procedure);
        return new ResponseEntity<>(toDTO(procedure), HttpStatus.CREATED);
    }

    /**
     * Updates an existing procedure.
     *
     * PUT /rest/v1/emrapi/procedure/{uuid}
     *
     * @param uuid the procedure UUID
     * @param dto the updated procedure data
     * @return the updated procedure DTO
     */
    @RequestMapping(value = "/{uuid}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<ProcedureDTO> updateProcedure(
            @PathVariable("uuid") String uuid,
            @RequestBody ProcedureDTO dto) {

        Procedure existing = procedureService.getProcedureByUuid(uuid);
        if (existing == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        updateFromDTO(existing, dto);
        existing = procedureService.saveProcedure(existing);
        return new ResponseEntity<>(toDTO(existing), HttpStatus.OK);
    }

    /**
     * Voids (soft-deletes) a procedure.
     *
     * DELETE /rest/v1/emrapi/procedure/{uuid}?reason={reason}
     *
     * @param uuid the procedure UUID
     * @param reason the reason for voiding
     * @return no content on success
     */
    @RequestMapping(value = "/{uuid}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Void> voidProcedure(
            @PathVariable("uuid") String uuid,
            @RequestParam("reason") String reason) {

        Procedure procedure = procedureService.getProcedureByUuid(uuid);
        if (procedure == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        procedureService.voidProcedure(procedure, reason);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Converts a Procedure entity to a ProcedureDTO.
     */
    private ProcedureDTO toDTO(Procedure procedure) {
        ProcedureDTO dto = new ProcedureDTO();
        dto.setUuid(procedure.getUuid());

        if (procedure.getPatient() != null) {
            dto.setPatientUuid(procedure.getPatient().getUuid());
        }
        if (procedure.getEncounter() != null) {
            dto.setEncounterUuid(procedure.getEncounter().getUuid());
        }
        if (procedure.getProcedureCoded() != null) {
            dto.setCodedProcedureUuid(procedure.getProcedureCoded().getUuid());
        }
        dto.setFreeTextProcedure(procedure.getProcedureNonCoded());

        if (procedure.getBodySite() != null) {
            dto.setBodySiteUuid(procedure.getBodySite().getUuid());
        }

        dto.setStartDateTime(procedure.getStartDateTime());
        dto.setEndDateTime(procedure.getEndDateTime());
        dto.setDuration(procedure.getDuration());

        if (procedure.getDurationUnit() != null) {
            dto.setDurationUnit(procedure.getDurationUnit().name());
        }

        if (procedure.getOutcomeCoded() != null) {
            dto.setCodedOutcomeUuid(procedure.getOutcomeCoded().getUuid());
        }
        dto.setFreeTextOutcome(procedure.getOutcomeNonCoded());

        dto.setNotes(procedure.getNotes());
        dto.setFormNamespace(procedure.getFormNamespace());
        dto.setFormFieldPath(procedure.getFormFieldPath());
        dto.setDateCreated(procedure.getDateCreated());
        dto.setVoided(procedure.getVoided());

        return dto;
    }

    /**
     * Creates a new Procedure entity from a ProcedureDTO.
     */
    private Procedure fromDTO(ProcedureDTO dto) {
        Procedure procedure = new Procedure();
        updateFromDTO(procedure, dto);
        return procedure;
    }

    /**
     * Updates an existing Procedure entity from a ProcedureDTO.
     */
    private void updateFromDTO(Procedure procedure, ProcedureDTO dto) {
        if (StringUtils.isNotBlank(dto.getPatientUuid())) {
            Patient patient = patientService.getPatientByUuid(dto.getPatientUuid());
            if (patient == null) {
                throw new IllegalArgumentException("Patient not found with UUID: " + dto.getPatientUuid());
            }
            procedure.setPatient(patient);
        }

        if (StringUtils.isNotBlank(dto.getEncounterUuid())) {
            Encounter encounter = encounterService.getEncounterByUuid(dto.getEncounterUuid());
            if (encounter == null) {
                throw new IllegalArgumentException("Encounter not found with UUID: " + dto.getEncounterUuid());
            }
            procedure.setEncounter(encounter);
        }

        if (StringUtils.isNotBlank(dto.getCodedProcedureUuid())) {
            Concept procedureCoded = conceptService.getConceptByUuid(dto.getCodedProcedureUuid());
            if (procedureCoded == null) {
                throw new IllegalArgumentException("Procedure concept not found with UUID: " + dto.getCodedProcedureUuid());
            }
            procedure.setProcedureCoded(procedureCoded);
        }
        procedure.setProcedureNonCoded(dto.getFreeTextProcedure());

        if (StringUtils.isNotBlank(dto.getBodySiteUuid())) {
            Concept bodySite = conceptService.getConceptByUuid(dto.getBodySiteUuid());
            if (bodySite == null) {
                throw new IllegalArgumentException("Body site concept not found with UUID: " + dto.getBodySiteUuid());
            }
            procedure.setBodySite(bodySite);
        }

        procedure.setStartDateTime(dto.getStartDateTime());
        procedure.setEndDateTime(dto.getEndDateTime());
        procedure.setDuration(dto.getDuration());

        if (StringUtils.isNotBlank(dto.getDurationUnit())) {
            try {
                procedure.setDurationUnit(Procedure.DurationUnit.valueOf(dto.getDurationUnit()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid duration unit: " + dto.getDurationUnit() +
                        ". Valid values are: SECONDS, MINUTES, HOURS, DAYS");
            }
        } else {
            procedure.setDurationUnit(null);
        }

        if (StringUtils.isNotBlank(dto.getCodedOutcomeUuid())) {
            Concept outcomeCoded = conceptService.getConceptByUuid(dto.getCodedOutcomeUuid());
            if (outcomeCoded == null) {
                throw new IllegalArgumentException("Outcome concept not found with UUID: " + dto.getCodedOutcomeUuid());
            }
            procedure.setOutcomeCoded(outcomeCoded);
        }
        procedure.setOutcomeNonCoded(dto.getFreeTextOutcome());

        procedure.setNotes(dto.getNotes());

        if (StringUtils.isNotBlank(dto.getFormNamespace()) && StringUtils.isNotBlank(dto.getFormFieldPath())) {
            procedure.setFormField(dto.getFormNamespace(), dto.getFormFieldPath());
        }
    }
}
