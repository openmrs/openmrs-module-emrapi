/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under the terms
 * of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.procedure;

import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.emrapi.PrivilegeConstants;

import java.util.List;

/**
 * Service interface for managing Procedure records.
 * Provides methods for creating, retrieving, updating, and voiding procedures.
 * @since 3.3.0
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
	@Authorized(PrivilegeConstants.MANAGE_PROCEDURES)
	Procedure saveProcedure(Procedure procedure) throws APIException;

	/**
	 * Gets a procedure by its ID.
	 *
	 * @param id the procedure ID
	 * @return the procedure, or null if not found
	 */
	@Authorized(PrivilegeConstants.GET_PROCEDURES)
	Procedure getProcedure(Integer id);

	/**
	 * Gets a procedure by its UUID.
	 *
	 * @param uuid the procedure UUID
	 * @return the procedure, or null if not found
	 */
	@Authorized(PrivilegeConstants.GET_PROCEDURES)
	Procedure getProcedureByUuid(String uuid);

	/**
	 * Gets all procedures for a patient, sorted by startDateTime descending.
	 *
	 * @param patient the patient
	 * @param includeVoided whether to include voided procedures
	 * @param firstResult the index of the first result to return (for pagination), or null to return all
	 * @param maxResults the maximum number of results to return (for pagination), or null to return all
	 * @return list of procedures
	 */
	@Authorized(PrivilegeConstants.GET_PROCEDURES)
	List<Procedure> getProceduresByPatient(Patient patient, boolean includeVoided, Integer firstResult, Integer maxResults);

	/**
	 * Gets count of procedures for a patient.
	 *
	 * @param patient the patient
	 * @param includeVoided whether to include voided procedures
	 * @return count of procedures for the patient
	 */
	@Authorized(PrivilegeConstants.GET_PROCEDURES)
	Long getProcedureCountByPatient(Patient patient, boolean includeVoided);

	/**
	 * Voids a procedure with a reason.
	 *
	 * @param procedure the procedure to void
	 * @param reason the reason for voiding
	 * @return the voided procedure
	 * @throws APIException if the procedure cannot be voided
	 */
	@Authorized(PrivilegeConstants.MANAGE_PROCEDURES)
	Procedure voidProcedure(Procedure procedure, String reason);

	/**
	 * Unvoids a previously voided procedure.
	 *
	 * @param procedure the procedure to unvoid
	 * @return the unvoided procedure
	 */
	@Authorized(PrivilegeConstants.MANAGE_PROCEDURES)
	Procedure unvoidProcedure(Procedure procedure);

	/**
	 * Permanently deletes a procedure from the database.
	 * Use with caution - prefer voiding for audit purposes.
	 *
	 * @param procedure the procedure to delete
	 * @throws APIException if the procedure cannot be deleted
	 */
	@Authorized(PrivilegeConstants.PURGE_PROCEDURES)
	void purgeProcedure(Procedure procedure);

	/**
	 * Saves a procedure type.
	 *
	 * @param procedureType the procedure type to save
	 * @return the saved procedure type
	 */
	@Authorized(PrivilegeConstants.MANAGE_PROCEDURE_TYPES)
	ProcedureType saveProcedureType(ProcedureType procedureType);

	/**
	 * Gets a procedure type by its internal ID.
	 *
	 * @param id the internal ID of the procedure type
	 * @return the procedure type, or null if not found
	 */
	@Authorized(PrivilegeConstants.GET_PROCEDURE_TYPES)
	ProcedureType getProcedureType(Integer id);

	/**
	 * Gets a procedure type by its UUID.
	 *
	 * @param uuid the procedure type UUID
	 * @return the procedure type, or null if not found
	 */
	@Authorized(PrivilegeConstants.GET_PROCEDURE_TYPES)
	ProcedureType getProcedureTypeByUuid(String uuid);

	/**
	 * Gets a procedure type by its name.
	 *
	 * @param name the name of the procedure type
	 * @return the procedure type, or null if not found
	 */
	@Authorized(PrivilegeConstants.GET_PROCEDURE_TYPES)
	List<ProcedureType> getProcedureTypesByName(String name);

	/**
	 * Gets all procedure types.
	 *
	 * @param includeRetired whether to include retired types
	 * @return list of procedure types
	 */
	@Authorized(PrivilegeConstants.GET_PROCEDURE_TYPES)
	List<ProcedureType> getAllProcedureTypes(boolean includeRetired);

	/**
	 * Retires a procedure type with a reason.
	 *
	 * @param procedureType the procedure type to retire
	 * @param reason the reason for retiring
	 * @return the retired procedure type
	 */
	@Authorized(PrivilegeConstants.MANAGE_PROCEDURE_TYPES)
	ProcedureType retireProcedureType(ProcedureType procedureType, String reason);

	/**
	 * Unretires a previously retired procedure type.
	 *
	 * @param procedureType the procedure type to unretire
	 * @return the unretired procedure type
	 */
	@Authorized(PrivilegeConstants.MANAGE_PROCEDURE_TYPES)
	ProcedureType unretireProcedureType(ProcedureType procedureType);

	/**
	 * Permanently deletes a procedure type from the database.
	 *
	 * @param procedureType the procedure type to delete
	 */
	@Authorized(PrivilegeConstants.PURGE_PROCEDURE_TYPES)
	void purgeProcedureType(ProcedureType procedureType);
}
