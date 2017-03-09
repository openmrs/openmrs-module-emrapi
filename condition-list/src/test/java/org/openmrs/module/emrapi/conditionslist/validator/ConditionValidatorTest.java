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
package org.openmrs.module.emrapi.conditionslist.validator;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Condition;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.module.emrapi.conditionslist.ConditionService;
import org.openmrs.module.emrapi.conditionslist.ConditionValidator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

public class ConditionValidatorTest extends BaseModuleContextSensitiveTest {

	@Autowired
	ConditionService conditionService;

	@Autowired
	ConceptService conceptService;

	@Autowired
	UserService userService;

	@Autowired
	PatientService patientService;

	@Autowired
	ConditionValidator conditionValidator;

	@Before
	public void setUp() throws Exception {
		executeDataSet("conditionListDataSet.xml");
	}

	@Test
	public void validate_shouldFailValidationIfConditionIsNull() throws Exception {
		Errors errors = new BindException(new Condition(), "condition");
		conditionValidator.validate(null, errors);
		Assert.assertTrue(errors.hasErrors());
		Assert.assertEquals("error.general", ((List<ObjectError>) errors.getAllErrors()).get(0).getCode());
	}

	@Test
	public void shouldNotAllowNonCodedConceptConditionWithoutNonCodedConditionValue() {
		Condition condition = conditionService.getConditionByUuid("2ss6880e-2c46-11e4-9038-a6c5e4d22fb7");
		Errors errors = new BindException(condition, "condition");
		conditionValidator.validate(condition, errors);
		Assert.assertTrue(errors.hasErrors());
		Assert.assertEquals("Condition.error.conditionNonCodedValueNeededForNonCodedCondition",
				((List<ObjectError>) errors.getAllErrors()).get(0).getCode());
	}

	@Test
	public void shouldNotAllowNonCodedValueForCodedConceptCondition() {
		Condition condition = conditionService.getConditionByUuid("2ss6880e-2c46-11e4-5844-a6c5e4d22fb7");
		Errors errors = new BindException(condition, "condition");
		conditionValidator.validate(condition, errors);
		Assert.assertTrue(errors.hasErrors());
		Assert.assertEquals("Condition.error.conditionNonCodedValueNotSupportedForCodedCondition",
				((List<ObjectError>) errors.getAllErrors()).get(0).getCode());
	}

	@Test
	public void shouldNotAllowDuplicateConditionsWhiteSpacesAndCaseChangesInConditionNonCodedValue() {
		Condition condition = createCondition(Condition.Status.ACTIVE, "OTHER, NON-CODED", 2,
				"3584c584-c291-46c8-8584-96dc33d19584", "P a I n");
		Errors errors = new BindException(condition, "condition");
		conditionValidator.validate(condition, errors);
		Assert.assertTrue(errors.hasErrors());
		Assert.assertEquals("Condition.error.duplicatesNotAllowed",
				((List<ObjectError>) errors.getAllErrors()).get(0).getCode());
	}

	@Test
	public void shouldNotAllowInValidEndReasonConcept() {
		Condition condition = createCondition(Condition.Status.INACTIVE, "Tuberculosis", 1,
				"2cc6880e-2c46-11e4-9038-a6c5e4d22fb7", null);
		Concept endReasonConcept = conceptService.getConceptByName("invalid end reason");
		condition.setEndReason(endReasonConcept);
		Errors errors = new BindException(condition, "condition");
		conditionValidator.validate(condition, errors);
		Assert.assertTrue(errors.hasErrors());
		Assert.assertEquals("Condition.error.notAmongAllowedConcepts",
				((List<ObjectError>) errors.getAllErrors()).get(0).getCode());
	}

	@Test
	public void shouldAllowValidEndReasonConcept() {
		Condition condition = createCondition(Condition.Status.INACTIVE, "Tuberculosis", 1,
				"2cc6880e-2c46-11e4-9038-a6c5e4d22fb7", null);
		Concept endReasonConcept = conceptService.getConceptByName("cured");
		condition.setEndReason(endReasonConcept);
		Errors errors = new BindException(condition, "condition");
		conditionValidator.validate(condition, errors);
		Assert.assertFalse(errors.hasErrors());
	}

	@Test
	public void shouldMandateEndReasonToEndCondition() {
		Condition condition = conditionService.getConditionByUuid("2cc6880e-2c46-11e4-9038-a6c5e4d22fb7");
		condition.setEndDate(new Date());
		Errors errors = new BindException(condition, "condition");
		conditionValidator.validate(condition, errors);
		Assert.assertEquals("Condition.error.endReasonIsMandatory",
				((List<ObjectError>) errors.getAllErrors()).get(0).getCode());
	}

	private Condition createCondition(Condition.Status status, String conceptName, int patientId, String uuid,
	                                  String conditionNonCoded) {
		Condition condition = new Condition();
		Concept concept = conceptService.getConceptByName(conceptName);
		condition.setConcept(concept);

		Patient patient = patientService.getPatient(patientId);
		condition.setPatient(patient);

		User user = userService.getUserByUsername("superman");
		condition.setCreator(user);
		condition.setStatus(status);
		condition.setDateCreated(new Date());
		condition.setVoided(false);
		condition.setConditionNonCoded(conditionNonCoded);
		condition.setUuid(uuid);

		return condition;
	}
}