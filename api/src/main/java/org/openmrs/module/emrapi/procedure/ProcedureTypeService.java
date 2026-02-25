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

import org.openmrs.api.OpenmrsService;

import java.util.List;

/**
 * Service interface for managing ProcedureType metadata.
 * @since 3.3.0
 */
public interface ProcedureTypeService extends OpenmrsService {
 
	/**
	 * Saves a procedure type.
	 *
	 * @param procedureType the procedure type to save
	 * @return the saved procedure type
	 */
	ProcedureType saveProcedureType(ProcedureType procedureType);
   
   /**
    * Gets a procedure type by its internal ID.
    *
    * @param id the internal ID of the procedure type
    * @return the procedure type, or null if not found
    */
   ProcedureType getProcedureTypeById(Integer id);
   
	/**
	 * Gets a procedure type by its UUID.
	 *
	 * @param uuid the procedure type UUID
	 * @return the procedure type, or null if not found
	 */
	ProcedureType getProcedureTypeByUuid(String uuid);

   /**
    * Gets a procedure type by its name.
    *
    * @param name the name of the procedure type
    * @return the procedure type, or null if not found
    */
   ProcedureType getProcedureTypeByName(String name);
   
	/**
	 * Gets all procedure types.
	 *
	 * @param includeRetired whether to include retired types
	 * @return list of procedure types
	 */
	List<ProcedureType> getAllProcedureTypes(boolean includeRetired);

	/**
	 * Retires a procedure type with a reason.
	 *
	 * @param procedureType the procedure type to retire
	 * @param reason the reason for retiring
	 * @return the retired procedure type
	 */
	ProcedureType retireProcedureType(ProcedureType procedureType, String reason);

	/**
	 * Unretires a previously retired procedure type.
	 *
	 * @param procedureType the procedure type to unretire
	 * @return the unretired procedure type
	 */
	ProcedureType unretireProcedureType(ProcedureType procedureType);

	/**
	 * Permanently deletes a procedure type from the database.
	 *
	 * @param procedureType the procedure type to delete
	 */
	void purgeProcedureType(ProcedureType procedureType);
}
