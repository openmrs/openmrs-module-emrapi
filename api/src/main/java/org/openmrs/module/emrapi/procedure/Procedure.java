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
package org.openmrs.module.emrapi.procedure;

import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.FormRecordable;
import org.openmrs.Patient;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

/**
 * Entity class representing a surgical or medical procedure performed on a patient.
 * Supports partial date/time for historical procedures (e.g., "2019", "2019-10", "2018-10-12T14:30:45.123+05:30").
 */
@Entity
@Table(name = "emrapi_procedure")
public class Procedure extends BaseChangeableOpenmrsData implements FormRecordable {

    private static final long serialVersionUID = 1L;

    /**
     * Enum representing duration units for procedure duration.
     */
    public enum DurationUnit {
        SECONDS, MINUTES, HOURS, DAYS
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "procedure_id")
    private Integer procedureId;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "encounter_id")
    private Encounter encounter;

    // Procedure name - coded or free text
    @ManyToOne
    @JoinColumn(name = "procedure_coded")
    private Concept procedureCoded;

    @Column(name = "procedure_non_coded", length = 255)
    private String procedureNonCoded;

    // Body site (required, coded only)
    @ManyToOne
    @JoinColumn(name = "body_site_id", nullable = false)
    private Concept bodySite;

    // Timing
    @Column(name = "start_date_time", nullable = false)
    private String startDateTime;

    @Column(name = "end_date_time")
    private Date endDateTime;

    @Column(name = "duration")
    private Integer duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_unit", length = 20)
    private DurationUnit durationUnit;

    // Outcome - coded or free text
    @ManyToOne
    @JoinColumn(name = "outcome_coded")
    private Concept outcomeCoded;

    @Column(name = "outcome_non_coded", length = 255)
    private String outcomeNonCoded;

    // Notes
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    // FormRecordable fields
    @Column(name = "form_namespace", length = 255)
    private String formNamespace;

    @Column(name = "form_field_path", length = 255)
    private String formFieldPath;

    // Default constructor required by Hibernate
    public Procedure() {
    }


    @Override
    public String getFormFieldNamespace() {
        return formNamespace;
    }

    @Override
    public String getFormFieldPath() {
        return formFieldPath;
    }

    @Override
    public void setFormField(String namespace, String path) {
        this.formNamespace = namespace;
        this.formFieldPath = path;
    }

    // Getters and setters

    @Override
    public Integer getId() {
        return procedureId;
    }

    @Override
    public void setId(Integer id) {
        this.procedureId = id;
    }

    public Integer getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(Integer procedureId) {
        this.procedureId = procedureId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Encounter getEncounter() {
        return encounter;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    public Concept getProcedureCoded() {
        return procedureCoded;
    }

    public void setProcedureCoded(Concept procedureCoded) {
        this.procedureCoded = procedureCoded;
    }

    public String getProcedureNonCoded() {
        return procedureNonCoded;
    }

    public void setProcedureNonCoded(String procedureNonCoded) {
        this.procedureNonCoded = procedureNonCoded;
    }

    public Concept getBodySite() {
        return bodySite;
    }

    public void setBodySite(Concept bodySite) {
        this.bodySite = bodySite;
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

    public DurationUnit getDurationUnit() {
        return durationUnit;
    }

    public void setDurationUnit(DurationUnit durationUnit) {
        this.durationUnit = durationUnit;
    }

    public Concept getOutcomeCoded() {
        return outcomeCoded;
    }

    public void setOutcomeCoded(Concept outcomeCoded) {
        this.outcomeCoded = outcomeCoded;
    }

    public String getOutcomeNonCoded() {
        return outcomeNonCoded;
    }

    public void setOutcomeNonCoded(String outcomeNonCoded) {
        this.outcomeNonCoded = outcomeNonCoded;
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

    public void setFormFieldPath(String formFieldPath) {
        this.formFieldPath = formFieldPath;
    }
}
