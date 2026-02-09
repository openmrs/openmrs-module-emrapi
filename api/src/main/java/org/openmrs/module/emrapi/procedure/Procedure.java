/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
 * Supports partial estimatedDate for historical procedures (e.g., "2019", "2019-10",).
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
      SECONDS,
      MINUTES,
      HOURS,
      DAYS
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
   
   @Column(name = "procedure_non_coded")
   private String procedureNonCoded;
   
   // Body site (required, coded only)
   @ManyToOne
   @JoinColumn(name = "body_site_coded", nullable = false)
   private Concept bodySite;
   
   // Timing
   @Column(name = "start_date_time", nullable = false)
   private Date startDateTime;
   
   @Column(name = "estimated_start_date")
   private String estimatedStartDate;
   
   @Column(name = "end_date_time")
   private Date endDateTime;
   
   @Column(name = "duration")
   private Integer duration;
   
   @Enumerated(EnumType.STRING)
   @Column(name = "duration_unit", length = 20)
   private DurationUnit durationUnit;
   
   // Status
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
