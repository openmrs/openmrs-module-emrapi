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

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Data Transfer Object for Procedure entity.
 * Used for REST API communication.
 */
@Setter
@Getter
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
    private Date startDateTime;
    private String estimatedStartDate;
    private Date endDateTime;
    private Integer duration;
    private String durationUnit;  // SECONDS, MINUTES, HOURS, DAYS

   // Status
   private String statusUuid;
   
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

}
