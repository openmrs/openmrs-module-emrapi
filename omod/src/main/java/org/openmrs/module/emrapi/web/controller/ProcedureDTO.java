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

import java.util.Date;

/**
 * Data Transfer Object for Procedure entity.
 * Used for REST API communication.
 */
public class ProcedureDTO {

    private String uuid;
    private String patientUuid;
    private String encounterUuid;

    // Procedure - coded or free text
    private String codedProcedureUuid;
    private String freeTextProcedure;

    // Body site (required, coded only)
    private String bodySiteUuid;

    // Timing
    private String startDateTime;
    private Date endDateTime;
    private Integer duration;
    private String durationUnit;  // SECONDS, MINUTES, HOURS, DAYS

    // Outcome - coded or free text
    private String codedOutcomeUuid;
    private String freeTextOutcome;

    // Notes
    private String notes;

    // FormRecordable support
    private String formNamespace;
    private String formFieldPath;

    // Audit fields (read-only in responses)
    private Date dateCreated;
    private boolean voided;

    // Default constructor
    public ProcedureDTO() {
    }

    // Getters and setters

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getEncounterUuid() {
        return encounterUuid;
    }

    public void setEncounterUuid(String encounterUuid) {
        this.encounterUuid = encounterUuid;
    }

    public String getCodedProcedureUuid() {
        return codedProcedureUuid;
    }

    public void setCodedProcedureUuid(String codedProcedureUuid) {
        this.codedProcedureUuid = codedProcedureUuid;
    }

    public String getFreeTextProcedure() {
        return freeTextProcedure;
    }

    public void setFreeTextProcedure(String freeTextProcedure) {
        this.freeTextProcedure = freeTextProcedure;
    }

    public String getBodySiteUuid() {
        return bodySiteUuid;
    }

    public void setBodySiteUuid(String bodySiteUuid) {
        this.bodySiteUuid = bodySiteUuid;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(String durationUnit) {
        this.durationUnit = durationUnit;
    }

    public String getCodedOutcomeUuid() {
        return codedOutcomeUuid;
    }

    public void setCodedOutcomeUuid(String codedOutcomeUuid) {
        this.codedOutcomeUuid = codedOutcomeUuid;
    }

    public String getFreeTextOutcome() {
        return freeTextOutcome;
    }

    public void setFreeTextOutcome(String freeTextOutcome) {
        this.freeTextOutcome = freeTextOutcome;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getFormNamespace() {
        return formNamespace;
    }

    public void setFormNamespace(String formNamespace) {
        this.formNamespace = formNamespace;
    }

    public String getFormFieldPath() {
        return formFieldPath;
    }

    public void setFormFieldPath(String formFieldPath) {
        this.formFieldPath = formFieldPath;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public boolean isVoided() {
        return voided;
    }

    public void setVoided(boolean voided) {
        this.voided = voided;
    }

}
