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

import org.openmrs.Patient;

import java.util.List;

/**
 * Data Access Object interface for Procedure entity.
 * @since 3.3.0
 */
public interface ProcedureDAO {
   
   /**
    * Gets a procedure by its internal ID.
    *
    * @param id the procedure ID
    * @return the procedure, or null if not found
    */
   Procedure getById(Integer id);
   
   /**
    * Gets a procedure by its UUID.
    *
    * @param uuid the procedure UUID
    * @return the procedure, or null if not found
    */
   Procedure getByUuid(String uuid);
   
   /**
    * Saves or updates a procedure.
    *
    * @param procedure the procedure to save
    * @return the saved procedure
    */
   Procedure saveOrUpdate(Procedure procedure);
   
   /**
    * Gets all procedures for a patient.
    *
    * @param patient the patient
    * @param includeVoided whether to include voided procedures
    * @param firstResult the index of the first result to return (for pagination), or null to return all
    * @param maxResults the maximum number of results to return (for pagination), or null to return all
    * @return list of procedures sorted by startDateTime descending
    */
   List<Procedure> getProceduresByPatient(Patient patient, boolean includeVoided, Integer firstResult, Integer maxResults);
   
   /**
    * Gets count of procedures for a patient.
    *
    * @param patient the patient
    * @param includeVoided whether to include voided procedures
    * @return count of procedures for the patient
    */
   Long getProcedureCountByPatient(Patient patient, boolean includeVoided);
   
   /**
    * Deletes a procedure from the database.
    * This is a hard delete - use voiding for soft delete.
    *
    * @param procedure the procedure to delete
    */
   void delete(Procedure procedure);

}
