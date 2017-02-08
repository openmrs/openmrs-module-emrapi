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

package org.openmrs.module.emrapi.conditionslist.db;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Condition;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class ConditionDAOTest extends BaseModuleContextSensitiveTest {
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Autowired
	ConditionDAO conditionDao;
	
	@Autowired
	PatientService patientService;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet("conditionListDataSet.xml");
	}
	
	@Test
	public void shouldGetConditionByUuid() {
		Condition condition = conditionDao.getConditionByUuid("2cc6880e-2c46-11e4-9038-a6c5e4d22fb7");
		assertEquals(Condition.Status.INACTIVE, condition.getStatus());
		assertEquals("Tuberculosis", condition.getConcept().getName().getName());
		assertEquals("2015-01-12 00:00:00.0", condition.getDateCreated().toString());
	}
	
	@Test
	public void shouldGetConditionsReturnNonVoidedConditionsInReverseChronologicalOrder() {
		Patient patient = patientService.getPatient(3);
		List<Condition> conditionHistoryForPatient = conditionDao.getConditionHistory(patient);
		assertEquals(4, conditionHistoryForPatient.size());
		assertEquals("wq4i8o0e-2n46-1zx4-58f4-a6i5trd22fb7", conditionHistoryForPatient.get(0).getUuid());
		assertEquals("p8ri8o0s-2m46-11e4-5df4-a6p5e4dh2fb7", conditionHistoryForPatient.get(1).getUuid());
		assertEquals("c84i8o0e-2n46-11e4-58f4-a6i5e4d22fb7", conditionHistoryForPatient.get(2).getUuid());
		assertEquals("p84i8o0r-2n46-mse4-58f4-a6i5e4du2fb7", conditionHistoryForPatient.get(3).getUuid());
	}
	
	@Test
	public void shouldGetActiveConditionsForPatient() {
		Patient patient = patientService.getPatient(3);
		List<Condition> activeConditions = conditionDao.getActiveConditions(patient);
		assertEquals(3, activeConditions.size());
	}
	
	@Test
	public void shouldGetActiveConditionsInReverseChronologicalOrderPatient() {
		Patient patient = patientService.getPatient(3);
		List<Condition> activeConditions = conditionDao.getActiveConditions(patient);
		assertEquals(3, activeConditions.size());
		assertEquals("wq4i8o0e-2n46-1zx4-58f4-a6i5trd22fb7", activeConditions.get(0).getUuid());
		assertEquals("p8ri8o0s-2m46-11e4-5df4-a6p5e4dh2fb7", activeConditions.get(1).getUuid());
		assertEquals("c84i8o0e-2n46-11e4-58f4-a6i5e4d22fb7", activeConditions.get(2).getUuid());
	}
	
	@Test
	public void shouldThrowErrorWhenChangingConcept() {
		Condition condition = conditionDao.getConditionByUuid("2cc6880e-2c46-11e4-9038-a6c5e4d22fb7");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Concept cannot be changed");
		condition.setConcept(conditionDao.getConditionByUuid("wq4i8o0e-2n46-1zx4-58f4-a6i5trd22fb7").getConcept());
	}
	
	@Test
	public void shouldThrowErrorWhenChangingPatient() {
		Condition condition = conditionDao.getConditionByUuid("2cc6880e-2c46-11e4-9038-a6c5e4d22fb7");
		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Patient cannot be changed");
		condition.setPatient(conditionDao.getConditionByUuid("wq4i8o0e-2n46-1zx4-58f4-a6i5trd22fb7").getPatient());
	}
}