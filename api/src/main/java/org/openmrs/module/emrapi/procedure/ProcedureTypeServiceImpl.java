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

import org.openmrs.api.impl.BaseOpenmrsService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public class ProcedureTypeServiceImpl extends BaseOpenmrsService implements ProcedureTypeService {

	private ProcedureTypeDAO procedureTypeDAO;

	public void setProcedureTypeDAO(ProcedureTypeDAO procedureTypeDAO) {
		this.procedureTypeDAO = procedureTypeDAO;
	}

	@Override
	@Transactional
	public ProcedureType saveProcedureType(ProcedureType procedureType) {
		return procedureTypeDAO.saveOrUpdate(procedureType);
	}

	@Override
	@Transactional(readOnly = true)
	public ProcedureType getProcedureTypeByUuid(String uuid) {
		return procedureTypeDAO.getByUuid(uuid);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ProcedureType> getAllProcedureTypes(boolean includeRetired) {
		return procedureTypeDAO.getAll(includeRetired);
	}

	@Override
	@Transactional
	public ProcedureType retireProcedureType(ProcedureType procedureType, String reason) {
		procedureType.setRetired(true);
		procedureType.setRetireReason(reason);
		return procedureTypeDAO.saveOrUpdate(procedureType);
	}

	@Override
	@Transactional
	public ProcedureType unretireProcedureType(ProcedureType procedureType) {
		procedureType.setRetired(false);
		procedureType.setRetireReason(null);
		procedureType.setDateRetired(null);
		procedureType.setRetiredBy(null);
		return procedureTypeDAO.saveOrUpdate(procedureType);
	}

	@Override
	@Transactional
	public void purgeProcedureType(ProcedureType procedureType) {
		procedureTypeDAO.delete(procedureType);
	}
}
