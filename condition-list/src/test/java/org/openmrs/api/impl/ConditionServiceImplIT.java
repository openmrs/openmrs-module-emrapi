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

package org.openmrs.api.impl;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Concept;
import org.openmrs.Condition;
import org.openmrs.ConditionHistory;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ConditionService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.api.db.ConditionDAO;
import org.openmrs.api.validator.ConditionValidator;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ConditionServiceImplIT extends BaseModuleContextSensitiveTest {

    @Autowired
    ConditionService conditionService;

    @Autowired
    ConceptService conceptService;

    @Autowired
    PatientService patientService;

    @Autowired
    UserService userService;

    @Autowired
    ConditionValidator conditionValidator;

    @Autowired
    ConditionDAO conditionDAO;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        executeDataSet("conditionListDataSet.xml");
    }
    @Test
    public void shouldCreateNewCondition() {
        Condition condition = createCondition(Condition.Status.PRESUMED, "Tuberculosis", 2, "3584c584-c291-46c8-8584-96dc33d19584", null);
        conditionService.save(condition);
        List<Condition> conditionsList = conditionDAO.getConditionHistory(condition.getPatient());
        assertEquals(conditionsList.size(), 3);
        assertTrue(condition.getId() > 0);
        assertEquals("3584c584-c291-46c8-8584-96dc33d19584", condition.getUuid());
        assertEquals(Condition.Status.PRESUMED, condition.getStatus());
        assertEquals("Tuberculosis", condition.getConcept().getName().getName());
    }

    @Test
    public void shouldNotCreateDuplicateCondition() {
        Condition condition = conditionService.getConditionByUuid("2cc6880e-2c46-11e4-9038-a6c5e4d22fb7");
        conditionService.save(condition);
        List<Condition> conditionsList = conditionDAO.getConditionHistory(condition.getPatient());
        assertEquals(conditionsList.size(), 4);
    }

    @Test
    public void shouldUpdateExistingCondition() {
        Condition condition = conditionService.getConditionByUuid("2cc5840e-2c84-11e4-9038-a6c5e4d22fb7");
        condition.setStatus(Condition.Status.HISTORY_OF);
        Condition savedCondition = conditionService.save(condition);

        assertEquals("Angina", savedCondition.getConcept().getDisplayString());
        assertEquals(Condition.Status.HISTORY_OF, savedCondition.getStatus());

        List<Condition> conditionsList = conditionDAO.getConditionHistory(condition.getPatient());
        assertEquals(conditionsList.size(), 4);
    }

    @Test
    public void shouldVoidConditionSetVoidedAndVoidReason() {
        Condition condition = conditionService.getConditionByUuid("2ss6880e-2c46-11e4-5844-a6c5e4d22fb7");
        Condition voidedCondition = conditionService.voidCondition(condition, "voiding");
        assertTrue(voidedCondition.getVoided());
        assertEquals(voidedCondition.getVoidReason(), "voiding");
    }

    @Test
    public void shouldMandateVoidReasonToVoidCondition() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("voidReason cannot be empty or null");
        Condition condition = conditionService.getConditionByUuid("2ss6880e-2c46-11e4-5844-a6c5e4d22fb7");
        conditionService.voidCondition(condition, null);
    }

    @Test
    public void shouldEndConditionSetEndDateAsTodayIfNotSpecified() {
        Condition condition = conditionService.getConditionByUuid("2ss6880e-2c46-11e4-5844-a6c5e4d22fb7");
        Date endDate = new Date();
        Concept endReason = conceptService.getConceptByUuid("cured84f-1yz9-4da3-bb88-8122ce8868ss");
        condition.setEndReason(endReason);
        Condition savedCondition = conditionService.save(condition);
        assertEquals(savedCondition.getEndDate().getDate(), endDate.getDate());
    }

    @Test
    public void shouldGetConditionHistoryReturnListOfConditionHistoryGroupedByConceptForPatient() {
        Patient patient = patientService.getPatient(3);
        List<ConditionHistory> conditionHistoryForPatient = conditionService.getConditionHistory(patient);
        assertEquals(conditionHistoryForPatient.size(), 4);
        
        assertThat(conditionHistoryForPatient, contains(new ConditionHistoryMatcher("severe", 1),
        	new ConditionHistoryMatcher("pain", 1), new ConditionHistoryMatcher("Angina", 1), 
        	new ConditionHistoryMatcher("Tuberculosis", 1)));
    }
    
    public static class ConditionHistoryMatcher extends TypeSafeMatcher<ConditionHistory> {
    	private final String name;
    	private final int count;
    	
        public ConditionHistoryMatcher(String name, int count) {
        	this.name = name;
        	this.count = count;
        }
    	
		@Override
        public void describeTo(Description description) {
	        description.appendText("condition: ").appendValue(name).appendText("count: ").appendValue(count);
        }

		@Override
        protected boolean matchesSafely(ConditionHistory item) {
	        return (item.getCondition().getName().getName().equals(name) || item.getNonCodedCondition().equals(name))
	        		&& count == item.getConditions().size();
        }
    	
    }

    private Condition createCondition(Condition.Status status, String conceptName, int patientId, String uuid, String conditionNonCoded) {
        Condition condition = new Condition();
        condition.setStatus(status);
        Concept concept = conceptService.getConceptByName(conceptName);
        condition.setConcept(concept);
        Patient patient = patientService.getPatient(patientId);
        condition.setPatient(patient);
        Date dateCreated = new Date();
        condition.setDateCreated(dateCreated);
        condition.setUuid(uuid);
        condition.setConditionNonCoded(conditionNonCoded);
        return condition;
    }
}
