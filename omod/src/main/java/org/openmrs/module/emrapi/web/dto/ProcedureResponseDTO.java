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
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Patient;

import java.util.Date;

/**
 * Data Transfer Object for Procedure entity.
 * Used for REST API communication.
 */
@Setter
@Getter
public class ProcedureResponseDTO {
   
   private String uuid;
   
   private Patient patient;
   
   private Encounter encounter;
   
   // Procedure - coded or free text
   private Concept codedProcedure;
   
   private String freeTextProcedure;
   
   // Body site (required, coded only)
   private Concept bodySite;
   
   // Timing
   private Date startDateTime;
   
   private String estimatedStartDate;
   
   private Date endDateTime;
   
   private Integer duration;
   
   private Concept durationUnitUuid;
   
   // Status
   private Concept status;
   
   // Outcome - coded or free text
   private Concept codedOutcome;
   
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
   public ProcedureResponseDTO() {
   }
   
}
