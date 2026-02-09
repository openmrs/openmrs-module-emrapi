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

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;

import java.util.List;

/**
 * Service interface for managing Procedure records.
 * Provides methods for creating, retrieving, updating, and voiding procedures.
 */
public interface ProcedureService extends OpenmrsService {
   
   /**
    * Saves a procedure record.
    * Validates required fields before saving.
    *
    * @param procedure the procedure to save
    * @return the saved procedure
    * @throws APIException if validation fails
    */
   Procedure saveProcedure(Procedure procedure) throws APIException;
   
   /**
    * Gets a procedure by its UUID.
    *
    * @param uuid the procedure UUID
    * @return the procedure, or null if not found
    */
   Procedure getProcedureByUuid(String uuid);
   
   /**
    * Gets a procedure by its internal ID.
    *
    * @param id the procedure ID
    * @return the procedure, or null if not found
    */
   Procedure getProcedure(Integer id);
   
   /**
    * Gets all non-voided procedures for a patient, sorted by startDateTime descending.
    *
    * @param patient the patient
    * @return list of procedures
    */
   List<Procedure> getProceduresByPatient(Patient patient);
   
   /**
    * Gets all procedures for a patient.
    *
    * @param patient the patient
    * @param includeVoided whether to include voided procedures
    * @return list of procedures sorted by startDateTime descending
    */
   List<Procedure> getProceduresByPatient(Patient patient, boolean includeVoided);
   
   /**
    * Gets all non-voided procedures associated with an encounter.
    *
    * @param encounter the encounter
    * @return list of procedures
    */
   List<Procedure> getProceduresByEncounter(Encounter encounter);
   
   /**
    * Voids (soft-deletes) a procedure.
    *
    * @param procedure the procedure to void
    * @param voidReason the reason for voiding
    * @return the voided procedure
    * @throws APIException if the void reason is null or empty
    */
   Procedure voidProcedure(Procedure procedure, String voidReason) throws APIException;
}
