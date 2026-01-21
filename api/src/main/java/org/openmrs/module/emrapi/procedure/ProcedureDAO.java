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

import org.openmrs.Encounter;
import org.openmrs.Patient;

import java.util.List;

/**
 * Data Access Object interface for Procedure entity.
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
     * @return list of procedures sorted by startDateTime descending
     */
    List<Procedure> getProceduresByPatient(Patient patient, boolean includeVoided);

    /**
     * Gets all procedures associated with an encounter.
     *
     * @param encounter the encounter
     * @return list of procedures for the encounter
     */
    List<Procedure> getProceduresByEncounter(Encounter encounter);

    /**
     * Deletes a procedure from the database.
     * This is a hard delete - use voiding for soft delete.
     *
     * @param procedure the procedure to delete
     */
    void delete(Procedure procedure);
}
