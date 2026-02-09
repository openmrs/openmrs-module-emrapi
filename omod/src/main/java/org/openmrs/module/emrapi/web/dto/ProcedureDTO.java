/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.web.dto;

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
