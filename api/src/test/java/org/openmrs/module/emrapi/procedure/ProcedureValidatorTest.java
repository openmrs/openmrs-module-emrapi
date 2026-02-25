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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ProcedureValidator}.
 */
class ProcedureValidatorTest {

	private ProcedureValidator validator;

	private Procedure procedure;

	private Errors errors;

	@BeforeEach
	void setUp() {
		validator = new ProcedureValidator();
		procedure = buildValidProcedure();
		errors = new BindException(procedure, "procedure");
	}

	@Test
	void supports_shouldSupportProcedureClass() {
		assertTrue(validator.supports(Procedure.class));
	}

	@Test
	void supports_shouldNotSupportOtherClasses() {
		assertFalse(validator.supports(Object.class));
		assertFalse(validator.supports(String.class));
	}

	@Test
	void validate_shouldPassForValidProcedure() {
		validator.validate(procedure, errors);
		assertFalse(errors.hasErrors());
	}

	@Test
	void validate_shouldRejectNonProcedureTarget() {
		Object notAProcedure = new Object();
      validator.validate(notAProcedure, errors);
      assertTrue(hasErrorCode("ProcedureValidator.onlySupportsProcedure"));
	}

	@Test
	void validate_shouldRejectWhenPatientIsNull() {
		procedure.setPatient(null);
		validator.validate(procedure, errors);
		assertTrue(hasErrorCode("Procedure.error.patientRequired"));
	}

	@Test
	void validate_shouldRejectWhenBothProcedureCodedAndNonCodedAreNull() {
		procedure.setProcedureCoded(null);
		procedure.setProcedureNonCoded(null);
		validator.validate(procedure, errors);
		assertTrue(hasErrorCode("Procedure.error.procedureRequired"));
	}

	@Test
	void validate_shouldRejectWhenBothProcedureCodedAndNonCodedAreProvided() {
		procedure.setProcedureCoded(new Concept());
		procedure.setProcedureNonCoded("Some procedure");
		validator.validate(procedure, errors);
		assertTrue(hasErrorCode("Procedure.error.procedureCodedAndNonCodedMutuallyExclusive"));
	}

	@Test
	void validate_shouldPassWhenOnlyProcedureCodedIsProvided() {
		procedure.setProcedureCoded(new Concept());
		procedure.setProcedureNonCoded(null);
		validator.validate(procedure, errors);
		assertFalse(hasErrorCode("Procedure.error.procedureRequired"));
		assertFalse(hasErrorCode("Procedure.error.procedureCodedAndNonCodedMutuallyExclusive"));
	}

	@Test
	void validate_shouldPassWhenOnlyProcedureNonCodedIsProvided() {
		procedure.setProcedureCoded(null);
		procedure.setProcedureNonCoded("Some procedure");
		validator.validate(procedure, errors);
		assertFalse(hasErrorCode("Procedure.error.procedureRequired"));
		assertFalse(hasErrorCode("Procedure.error.procedureCodedAndNonCodedMutuallyExclusive"));
	}

	@Test
	void validate_shouldRejectWhenBodySiteIsNull() {
		procedure.setBodySite(null);
		validator.validate(procedure, errors);
		assertTrue(hasErrorCode("Procedure.error.bodySiteRequired"));
	}

	@Test
	void validate_shouldRejectWhenBothStartDateTimeAndEstimatedStartDateAreNull() {
		procedure.setStartDateTime(null);
		procedure.setEstimatedStartDate(null);
		validator.validate(procedure, errors);
		assertTrue(hasErrorCode("Procedure.error.startDateTimeRequired"));
	}

	@Test
	void validate_shouldPassWhenOnlyStartDateTimeIsProvided() {
		procedure.setStartDateTime(new Date());
		procedure.setEstimatedStartDate(null);
		validator.validate(procedure, errors);
		assertFalse(hasErrorCode("Procedure.error.startDateTimeRequired"));
		assertFalse(hasErrorCode("Procedure.error.startDateTimeAndEstimatedDateMutuallyExclusive"));
	}

	@Test
	void validate_shouldPassWhenOnlyEstimatedStartDateIsProvided() {
		procedure.setStartDateTime(null);
		procedure.setEstimatedStartDate("2024-01");
		validator.validate(procedure, errors);
		assertFalse(hasErrorCode("Procedure.error.startDateTimeRequired"));
		assertFalse(hasErrorCode("Procedure.error.startDateTimeAndEstimatedDateMutuallyExclusive"));
	}

	@Test
	void validate_shouldRejectWhenDurationIsProvidedWithoutDurationUnit() {
		procedure.setDuration(30);
		procedure.setDurationUnit(null);
		validator.validate(procedure, errors);
		assertTrue(hasErrorCode("Procedure.error.durationUnitRequired"));
	}

	@Test
	void validate_shouldPassWhenDurationAndDurationUnitAreBothProvided() {
		procedure.setDuration(30);
		procedure.setDurationUnit(new Concept());
		validator.validate(procedure, errors);
		assertFalse(hasErrorCode("Procedure.error.durationUnitRequired"));
	}

	@Test
	void validate_shouldPassWhenDurationIsNull() {
		procedure.setDuration(null);
		procedure.setDurationUnit(null);
		validator.validate(procedure, errors);
		assertFalse(hasErrorCode("Procedure.error.durationUnitRequired"));
	}

	@Test
	void validate_shouldRejectWhenStatusIsNull() {
		procedure.setStatus(null);
		validator.validate(procedure, errors);
		assertTrue(hasErrorCode("Procedure.error.statusRequired"));
	}

	@Test
	void validate_shouldRejectWhenVoidedWithoutVoidReason() {
		procedure.setVoided(true);
		procedure.setVoidReason(null);
		validator.validate(procedure, errors);
		assertTrue(hasErrorCode("Procedure.error.voidReasonRequiredWhenVoided"));
	}

	@Test
	void validate_shouldRejectWhenVoidedWithBlankVoidReason() {
		procedure.setVoided(true);
		procedure.setVoidReason("   ");
		validator.validate(procedure, errors);
		assertTrue(hasErrorCode("Procedure.error.voidReasonRequiredWhenVoided"));
	}

	@Test
	void validate_shouldPassWhenVoidedWithVoidReason() {
		procedure.setVoided(true);
		procedure.setVoidReason("Entered in error");
		validator.validate(procedure, errors);
		assertFalse(hasErrorCode("Procedure.error.voidReasonRequiredWhenVoided"));
	}

	@Test
	void validate_shouldPassWhenNotVoidedWithoutVoidReason() {
		procedure.setVoided(false);
		procedure.setVoidReason(null);
		validator.validate(procedure, errors);
		assertFalse(hasErrorCode("Procedure.error.voidReasonRequiredWhenVoided"));
	}

	@Test
	void validate_shouldAllowRetiredTypesWhenUpdating() {
      procedure.setProcedureId(123); // Simulate existing procedure
		ProcedureType otherType = new ProcedureType();
		otherType.setUuid("some-other-uuid");
		otherType.setRetired(true);
		procedure.setProcedureType(otherType);
		procedure.setEncounter(null);
		validator.validate(procedure, errors);
		assertFalse(hasErrorCode("Procedure.error.procedureTypeRetired"));
	}

	private Procedure buildValidProcedure() {
		Procedure p = new Procedure();
		p.setPatient(new Patient());
		p.setProcedureCoded(new Concept());
		p.setProcedureNonCoded(null);
		p.setBodySite(new Concept());
		p.setStartDateTime(new Date());
		p.setEstimatedStartDate(null);
		p.setStatus(new Concept());
		p.setVoided(false);

		ProcedureType type = new ProcedureType();
		type.setUuid("some-non-current-uuid");
		type.setRetired(false);
		p.setProcedureType(type);

		return p;
	}

	private boolean hasErrorCode(String code) {
		return errors.getAllErrors().stream()
				.anyMatch(e -> e.getCode().equals(code));
	}
}
