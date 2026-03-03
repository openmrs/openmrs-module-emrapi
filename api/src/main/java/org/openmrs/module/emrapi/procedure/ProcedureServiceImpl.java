/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under the terms
 * of the Healthcare Disclaimer located at http://openmrs.org/license.
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.procedure;

import lombok.extern.slf4j.Slf4j;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
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
	@Transactional(readOnly = true)
	public Procedure getProcedure(Integer id) {
		log.debug("Getting procedure by id: {}", id);
		return procedureDAO.getProcedure(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Procedure getProcedureByUuid(String uuid) {
		log.debug("Getting procedure by uuid: {}", uuid);
		return procedureDAO.getProcedureByUuid(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Procedure> getProceduresByPatient(Patient patient, boolean includeVoided, Integer firstResult,
			Integer maxResults) {
		log.debug("Getting procedures for patient: {}, includeVoided: {}, firstResult: {}, maxResults: {}", patient,
				includeVoided, firstResult, maxResults);
		return procedureDAO.getProceduresByPatient(patient, includeVoided, firstResult, maxResults);
	}
	
	@Override
	public Long getProcedureCountByPatient(Patient patient, boolean includeVoided) {
		log.debug("Getting procedure count for patient: {}, includeVoided: {}", patient, includeVoided);
		return procedureDAO.getProcedureCountByPatient(patient, includeVoided);
	}
	
	@Override
	public Procedure saveProcedure(Procedure procedure) throws APIException {
		log.info("Saving procedure: {}", procedure.getUuid());
		
		if (procedure.getEstimatedStartDate() != null) {
			log.debug("Calculating startDateTime from estimatedStartDate: {}", procedure.getEstimatedStartDate());
			Date calculatedStartDateTime = getDateTimeFromEstimatedDate(procedure.getEstimatedStartDate());
			procedure.setStartDateTime(calculatedStartDateTime);
		}
		
		return procedureDAO.saveOrUpdateProcedure(procedure);
	}
	
	@Override
	public Procedure unvoidProcedure(Procedure procedure) {
		log.info("Unvoiding procedure: {}", procedure.getUuid());
		
		procedure.setVoided(false);
		procedure.setVoidReason(null);
		procedure.setDateVoided(null);
		procedure.setVoidedBy(null);
		
		return procedureDAO.saveOrUpdateProcedure(procedure);
	}
	
	@Override
	public void purgeProcedure(Procedure procedure) throws APIException {
		log.info("Purging procedure: {}", procedure.getUuid());
		procedureDAO.deleteProcedure(procedure);
	}
	
	@Override
	public Procedure voidProcedure(Procedure procedure, String reason) {
		log.info("Voiding procedure: {} with reason: {}", procedure.getUuid(), reason);
		return procedureDAO.saveOrUpdateProcedure(procedure);
	}
	
	@Override
	public ProcedureType getProcedureType(Integer id) {
		log.debug("Getting procedure type by id: {}", id);
		return procedureDAO.getProcedureType(id);
	}
	
	@Override
	public ProcedureType saveProcedureType(ProcedureType procedureType) {
		log.info("Saving procedure type: {}", procedureType.getName());
		
		ProcedureType existingTypeWithSameName = procedureDAO.getProcedureTypeByName(procedureType.getName());
		if (existingTypeWithSameName != null && !existingTypeWithSameName.getUuid().equals(procedureType.getUuid())) {
			log.warn("Cannot save procedure type with duplicate name: {}", procedureType.getName());
			throw new APIException("ProcedureType.error.duplicateName", new Object[] { procedureType.getName() });
		}
		return procedureDAO.saveOrUpdateProcedure(procedureType);
	}
	
	@Override
	@Transactional(readOnly = true)
	public ProcedureType getProcedureTypeByUuid(String uuid) {
		log.debug("Getting procedure type by uuid: {}", uuid);
		return procedureDAO.getProcedureTypeByUuid(uuid);
	}
	
	@Override
	@Transactional(readOnly = true)
	public ProcedureType getProcedureTypeByName(String name) {
		log.debug("Getting procedure type by name: {}", name);
		return procedureDAO.getProcedureTypeByName(name);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<ProcedureType> getAllProcedureTypes(boolean includeRetired) {
		log.debug("Getting all procedure types, includeRetired: {}", includeRetired);
		return procedureDAO.getAllProcedureTypes(includeRetired);
	}
	
	@Override
	public ProcedureType retireProcedureType(ProcedureType procedureType, String reason) {
		log.info("Retiring procedure type: {} with reason: {}", procedureType.getName(), reason);
		procedureType.setRetired(true);
		procedureType.setRetireReason(reason);
		return procedureDAO.saveOrUpdateProcedure(procedureType);
	}
	
	@Override
	public ProcedureType unretireProcedureType(ProcedureType procedureType) {
		log.info("Unretiring procedure type: {}", procedureType.getName());
		procedureType.setRetired(false);
		procedureType.setRetireReason(null);
		procedureType.setDateRetired(null);
		procedureType.setRetiredBy(null);
		return procedureDAO.saveOrUpdateProcedure(procedureType);
	}
	
	@Override
	public void purgeProcedureType(ProcedureType procedureType) {
		log.info("Purging procedure type: {}", procedureType.getName());
		procedureDAO.deleteProcedureType(procedureType);
	}
	
	Date getDateTimeFromEstimatedDate(String estimatedDate) {
		try {
			// Full datetime
			if (estimatedDate.length() > 10) {
				LocalDateTime dateTime = LocalDateTime.parse(estimatedDate);
				return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
			}
			
			// yyyy-MM-dd
			if (estimatedDate.length() == 10) {
				LocalDate date = LocalDate.parse(estimatedDate);
				return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
			}
			
			// yyyy-MM
			if (estimatedDate.length() == 7) {
				YearMonth ym = YearMonth.parse(estimatedDate);
				return Date.from(
						ym.atDay(1)
								.atStartOfDay(ZoneId.systemDefault())
								.toInstant()
				);
			}
			
			// yyyy
			if (estimatedDate.length() == 4) {
				Year year = Year.parse(estimatedDate);
				return Date.from(
						year.atMonth(1)
								.atDay(1)
								.atStartOfDay(ZoneId.systemDefault())
								.toInstant()
				);
			}
			
			throw new APIException("Procedure.error.invalidEstimateDate", new Object[] { estimatedDate });
			
		}
		catch (DateTimeParseException e) {
			log.warn("Failed to parse estimated date: {}, error: {}", estimatedDate, e.getMessage());
			throw new APIException("Procedure.error.invalidEstimateDate", new Object[] { estimatedDate }, e);
		}
	}
}
