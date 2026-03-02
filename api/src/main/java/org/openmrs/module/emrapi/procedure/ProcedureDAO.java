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
   Procedure getProcedure(Integer id);
   
   /**
    * Gets a procedure by its UUID.
    *
    * @param uuid the procedure UUID
    * @return the procedure, or null if not found
    */
   Procedure getProcedureByUuid(String uuid);
   
   /**
    * Saves or updates a procedure.
    *
    * @param procedure the procedure to save
    * @return the saved procedure
    */
   Procedure saveOrUpdateProcedure(Procedure procedure);
   
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
   void deleteProcedure(Procedure procedure);
   
   /**
    * Retrieves a {@link ProcedureType} by its internal ID.
    *
    * @param id the internal ID of the procedure type
    * @return the matching {@link ProcedureType}, or {@code null} if not found
    */
   ProcedureType getProcedureType(Integer id);
   
   /**
    * Retrieves a {@link ProcedureType} by its UUID.
    *
    * @param uuid the UUID of the procedure type
    * @return the matching {@link ProcedureType}, or {@code null} if not found
    */
   ProcedureType getProcedureTypeByUuid(String uuid);
   
   /**
    * Retrieves a {@link ProcedureType} by its name.
    *
    * @param name the name of the procedure type
    * @return the matching {@link ProcedureType}, or {@code null} if not found
    */
   ProcedureType getProcedureTypeByName(String name);
   
   /**
    * Retrieves all {@link ProcedureType} records.
    *
    * @param includeRetired if {@code true}, retired procedure types are included in the results
    * @return a list of {@link ProcedureType} records; never {@code null}
    */
   List<ProcedureType> getAllProcedureTypes(boolean includeRetired);
   
   /**
    * Persists a new {@link ProcedureType} or updates an existing one.
    *
    * @param procedureType the procedure type to save or update
    * @return the saved or updated {@link ProcedureType}
    */
   ProcedureType saveOrUpdateProcedure(ProcedureType procedureType);
   
   /**
    * Permanently deletes a {@link ProcedureType} from the database.
    *
    * @param procedureType the procedure type to delete
    */
   void deleteProcedureType(ProcedureType procedureType);
}
