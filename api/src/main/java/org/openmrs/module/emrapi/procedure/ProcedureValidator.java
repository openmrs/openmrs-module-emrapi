/*
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
import org.apache.commons.lang.StringUtils;
import org.openmrs.annotation.Handler;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Date;

/**
 * Validator for {@link Procedure} objects.
 *
 * @since 3.4.0
 */
@Handler(supports = { Procedure.class }, order = 50)
@Slf4j
public class ProcedureValidator implements Validator {
	
	@Override
	public boolean supports(Class<?> clazz) {
		return Procedure.class.isAssignableFrom(clazz);
	}
	
	@Override
	public void validate(Object target, Errors errors) {
		log.debug("Validating procedure: {}", target);
		if (!(target instanceof Procedure)) {
			errors.reject("ProcedureValidator.onlySupportsProcedure");
		} else {
			Procedure procedure = (Procedure) target;
			if (procedure.getPatient() == null) {
				errors.reject("Procedure.error.patientRequired");
			}
			if (procedure.getProcedureCoded() == null && StringUtils.isBlank(procedure.getProcedureNonCoded())) {
				errors.reject("Procedure.error.procedureRequired");
			}
			if (procedure.getProcedureCoded() != null && StringUtils.isNotBlank(procedure.getProcedureNonCoded())) {
				errors.reject("Procedure.error.procedureCodedAndNonCodedMutuallyExclusive");
			}
			if (procedure.getEndDateTime() != null) {
				Date startDateTime = procedure.getStartDateTime();
				// Calculate startDateTime from estimatedStartDate if provided
				if (procedure.getEstimatedStartDate() != null) {
					startDateTime = ProcedureUtil.getDateTimeFromEstimatedDate(procedure.getEstimatedStartDate());
				}
				
				if (procedure.getEndDateTime().before(startDateTime)) {
					errors.reject("Procedure.error.endDateTimeBeforeStartDateTime");
				}
			}
			if (procedure.getBodySite() == null) {
				errors.reject("Procedure.error.bodySiteRequired");
			}
			if (procedure.getEstimatedStartDate() == null && procedure.getStartDateTime() == null) {
				errors.reject("Procedure.error.startDateTimeRequired");
			}
			if (procedure.getDuration() != null && procedure.getDurationUnit() == null) {
				errors.reject("Procedure.error.durationUnitRequired");
			}
			if (procedure.getStatus() == null) {
				errors.reject("Procedure.error.statusRequired");
			}
			if (procedure.getVoided() && StringUtils.isBlank(procedure.getVoidReason())) {
				errors.reject("Procedure.error.voidReasonRequiredWhenVoided");
			}
			if (procedure.getProcedureType() == null) {
				errors.reject("Procedure.error.procedureTypeRequired");
			}
			
			// Rules which applied only for new procedures
			if (procedure.getProcedureId() == null) {
				if (procedure.getProcedureType() != null && procedure.getProcedureType().getRetired()) {
					errors.reject("Procedure.error.procedureTypeRetired");
				}
				if (procedure.getEstimatedStartDate() != null && procedure.getStartDateTime() != null) {
					errors.reject("Procedure.error.startDateTimeAndEstimatedDateMutuallyExclusiveForNewProcedures");
				}
			}
			
			if (errors.hasErrors()) {
				log.warn("Validation failed for procedure {}: {}", procedure.getUuid(), errors.getAllErrors());
			}
		}
	}
}
