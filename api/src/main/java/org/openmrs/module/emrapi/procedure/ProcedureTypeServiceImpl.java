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

import lombok.extern.slf4j.Slf4j;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of the {@link ProcedureTypeService}.
 * @since 3.3.0
 */
@Transactional
@Slf4j
public class ProcedureTypeServiceImpl extends BaseOpenmrsService implements ProcedureTypeService {

	private ProcedureTypeDAO procedureTypeDAO;

	public void setProcedureTypeDAO(ProcedureTypeDAO procedureTypeDAO) {
		this.procedureTypeDAO = procedureTypeDAO;
	}

   @Override
   public ProcedureType getProcedureTypeById(Integer id) {
      log.debug("Getting procedure type by id: {}", id);
      return procedureTypeDAO.getById(id);
   }
   
	@Override
	public ProcedureType saveProcedureType(ProcedureType procedureType) {
		log.info("Saving procedure type: {}", procedureType.getName());
		return procedureTypeDAO.saveOrUpdate(procedureType);
	}

	@Override
   @Transactional(readOnly = true)
	public ProcedureType getProcedureTypeByUuid(String uuid) {
		log.debug("Getting procedure type by uuid: {}", uuid);
		return procedureTypeDAO.getByUuid(uuid);
	}
   
   @Override
   @Transactional(readOnly = true)
   public ProcedureType getProcedureTypeByName(String name) {
      log.debug("Getting procedure type by name: {}", name);
      return procedureTypeDAO.getByName(name);
   }

	@Override
   @Transactional(readOnly = true)
	public List<ProcedureType> getAllProcedureTypes(boolean includeRetired) {
		log.debug("Getting all procedure types, includeRetired: {}", includeRetired);
		return procedureTypeDAO.getAll(includeRetired);
	}

	@Override
	public ProcedureType retireProcedureType(ProcedureType procedureType, String reason) {
		log.info("Retiring procedure type: {} with reason: {}", procedureType.getName(), reason);
		procedureType.setRetired(true);
		procedureType.setRetireReason(reason);
		return procedureTypeDAO.saveOrUpdate(procedureType);
	}

	@Override
	public ProcedureType unretireProcedureType(ProcedureType procedureType) {
		log.info("Unretiring procedure type: {}", procedureType.getName());
		procedureType.setRetired(false);
		procedureType.setRetireReason(null);
		procedureType.setDateRetired(null);
		procedureType.setRetiredBy(null);
		return procedureTypeDAO.saveOrUpdate(procedureType);
	}

	@Override
	public void purgeProcedureType(ProcedureType procedureType) {
		log.info("Purging procedure type: {}", procedureType.getName());
		procedureTypeDAO.delete(procedureType);
	}
}
