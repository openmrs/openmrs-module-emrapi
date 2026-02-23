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

import java.util.List;

/**
 * Data Access Object interface for {@link ProcedureType} entity.
 * @since 3.3.0
 */
public interface ProcedureTypeDAO {

	/**
	 * Retrieves a {@link ProcedureType} by its UUID.
	 *
	 * @param uuid the UUID of the procedure type
	 * @return the matching {@link ProcedureType}, or {@code null} if not found
	 */
	ProcedureType getByUuid(String uuid);

	/**
	 * Retrieves all {@link ProcedureType} records.
	 *
	 * @param includeRetired if {@code true}, retired procedure types are included in the results
	 * @return a list of {@link ProcedureType} records; never {@code null}
	 */
	List<ProcedureType> getAll(boolean includeRetired);

	/**
	 * Persists a new {@link ProcedureType} or updates an existing one.
	 *
	 * @param procedureType the procedure type to save or update
	 * @return the saved or updated {@link ProcedureType}
	 */
	ProcedureType saveOrUpdate(ProcedureType procedureType);

	/**
	 * Permanently deletes a {@link ProcedureType} from the database.
	 *
	 * @param procedureType the procedure type to delete
	 */
	void delete(ProcedureType procedureType);
}
