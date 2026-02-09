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

import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
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
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "procedure_coded")
    private Concept procedureCoded;
   

    @Column(name = "procedure_non_coded")
    private String procedureNonCoded;

    // Body site (required, coded only)
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "body_site_coded", nullable = false)
    private Concept bodySite;

    // Timing
    @Getter
    @Setter
    @Column(name = "start_date_time", nullable = false)
    private String startDateTime;
   

    @Column(name = "end_date_time")
    private Date endDateTime;
   

    @Column(name = "duration")
    private Integer duration;
   

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_unit", length = 20)
    private DurationUnit durationUnit;
    
    // Status
    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "status_coded", nullable = false)
    private Concept status;

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
   
   
}
