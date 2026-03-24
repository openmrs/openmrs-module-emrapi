/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under the terms
 * of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.procedure;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Implementation of the {@link ProcedureService}.
 * @since 3.3.0
 */
@Transactional
@Slf4j
public class ProcedureServiceImpl extends BaseOpenmrsService implements ProcedureService {
	
	private ProcedureDAO procedureDAO;
	
	public void setProcedureDAO(ProcedureDAO procedureDAO) {
		this.procedureDAO = procedureDAO;
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_PROCEDURES)
	@Transactional(readOnly = true)
	public Procedure getProcedure(Integer id) {
		log.debug("Getting procedure by id: {}", id);
		return procedureDAO.getProcedure(id);
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_PROCEDURES)
	@Transactional(readOnly = true)
	public Procedure getProcedureByUuid(String uuid) {
		log.debug("Getting procedure by uuid: {}", uuid);
		return procedureDAO.getProcedureByUuid(uuid);
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_PROCEDURES)
	@Transactional(readOnly = true)
	public List<Procedure> getProceduresByPatient(Patient patient, boolean includeVoided, Integer firstResult,
			Integer maxResults) {
		log.debug("Getting procedures for patient: {}, includeVoided: {}, firstResult: {}, maxResults: {}", patient,
				includeVoided, firstResult, maxResults);
		return procedureDAO.getProceduresByPatient(patient, includeVoided, firstResult, maxResults);
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_PROCEDURES)
	@Transactional(readOnly = true)
	public Long getProcedureCountByPatient(Patient patient, boolean includeVoided) {
		log.debug("Getting procedure count for patient: {}, includeVoided: {}", patient, includeVoided);
		return procedureDAO.getProcedureCountByPatient(patient, includeVoided);
	}
	
	@Override
	@Authorized(PrivilegeConstants.MANAGE_PROCEDURES)
	public Procedure saveProcedure(Procedure procedure) throws APIException {
		log.info("Saving procedure: {}", procedure.getUuid());
		
		if (procedure.getEstimatedStartDate() != null) {
			log.debug("Calculating startDateTime from estimatedStartDate: {}", procedure.getEstimatedStartDate());
			Date calculatedStartDateTime = ProcedureUtil.getDateTimeFromEstimatedDate(procedure.getEstimatedStartDate());
			procedure.setStartDateTime(calculatedStartDateTime);
		}
		
		if (procedure.getEndDateTime() != null && procedure.getEndDateTime().before(procedure.getStartDateTime())) {
			log.warn("End date {} is before start date {}", procedure.getEndDateTime(), procedure.getStartDateTime());
			throw new APIException("Procedure.error.endDateBeforeStartDate");
		}
		
		return procedureDAO.saveOrUpdateProcedureType(procedure);
	}
	
	@Override
	@Authorized(PrivilegeConstants.MANAGE_PROCEDURES)
	public Procedure unvoidProcedure(Procedure procedure) {
		log.info("Unvoiding procedure: {}", procedure.getUuid());
		
		procedure.setVoided(false);
		procedure.setVoidReason(null);
		procedure.setDateVoided(null);
		procedure.setVoidedBy(null);
		
		return procedureDAO.saveOrUpdateProcedureType(procedure);
	}
	
	@Override
	@Authorized(PrivilegeConstants.PURGE_PROCEDURES)
	public void purgeProcedure(Procedure procedure) throws APIException {
		log.info("Purging procedure: {}", procedure.getUuid());
		procedureDAO.deleteProcedure(procedure);
	}
	
	@Override
	@Authorized(PrivilegeConstants.MANAGE_PROCEDURES)
	public Procedure voidProcedure(Procedure procedure, String reason) {
		log.info("Voiding procedure: {} with reason: {}", procedure.getUuid(), reason);
		return procedureDAO.saveOrUpdateProcedureType(procedure);
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_PROCEDURE_TYPES)
	@Transactional(readOnly = true)
	public ProcedureType getProcedureType(Integer id) {
		log.debug("Getting procedure type by id: {}", id);
		return procedureDAO.getProcedureType(id);
	}
	
	@Override
	@Authorized(PrivilegeConstants.MANAGE_PROCEDURE_TYPES)
	public ProcedureType saveProcedureType(ProcedureType procedureType) {
		log.info("Saving procedure type: {}", procedureType.getName());
		return procedureDAO.saveOrUpdateProcedureType(procedureType);
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_PROCEDURE_TYPES)
	@Transactional(readOnly = true)
	public ProcedureType getProcedureTypeByUuid(String uuid) {
		log.debug("Getting procedure type by uuid: {}", uuid);
		return procedureDAO.getProcedureTypeByUuid(uuid);
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_PROCEDURE_TYPES)
	@Transactional(readOnly = true)
	public List<ProcedureType> getProcedureTypesByName(String name) {
		log.debug("Getting procedure type by name: {}", name);
		return procedureDAO.getProcedureTypesByName(name);
	}
	
	@Override
	@Authorized(PrivilegeConstants.GET_PROCEDURE_TYPES)
	@Transactional(readOnly = true)
	public List<ProcedureType> getAllProcedureTypes(boolean includeRetired) {
		log.debug("Getting all procedure types, includeRetired: {}", includeRetired);
		return procedureDAO.getAllProcedureTypes(includeRetired);
	}
	
	@Override
	@Authorized(PrivilegeConstants.MANAGE_PROCEDURE_TYPES)
	public ProcedureType retireProcedureType(ProcedureType procedureType, String reason) {
		log.info("Retiring procedure type: {} with reason: {}", procedureType.getName(), reason);
		procedureType.setRetired(true);
		procedureType.setRetireReason(reason);
		return procedureDAO.saveOrUpdateProcedureType(procedureType);
	}
	
	@Override
	@Authorized(PrivilegeConstants.MANAGE_PROCEDURE_TYPES)
	public ProcedureType unretireProcedureType(ProcedureType procedureType) {
		log.info("Unretiring procedure type: {}", procedureType.getName());
		procedureType.setRetired(false);
		procedureType.setRetireReason(null);
		procedureType.setDateRetired(null);
		procedureType.setRetiredBy(null);
		return procedureDAO.saveOrUpdateProcedureType(procedureType);
	}
	
	@Override
	@Authorized(PrivilegeConstants.PURGE_PROCEDURE_TYPES)
	public void purgeProcedureType(ProcedureType procedureType) {
		log.info("Purging procedure type: {}", procedureType.getName());
		procedureDAO.deleteProcedureType(procedureType);
	}
}
