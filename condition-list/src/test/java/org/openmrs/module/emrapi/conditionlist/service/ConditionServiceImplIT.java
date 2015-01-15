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

package org.openmrs.module.emrapi.conditionlist.service;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.module.emrapi.conditionlist.domain.Condition;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class ConditionServiceImplIT extends BaseModuleContextSensitiveTest {

    @Autowired
    ConditionService conditionService;

    @Autowired
    ConceptService conceptService;

    @Autowired
    PatientService patientService;

    @Autowired
    UserService userService;

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
        List<Condition> allConditions = conditionService.getConditionsByPatient(condition.getPatient());
        assertEquals(allConditions.size(), 3);
        assertTrue(condition.getId() > 0);
        assertEquals("3584c584-c291-46c8-8584-96dc33d19584", condition.getUuid());
        assertEquals(Condition.Status.PRESUMED, condition.getStatus());
        assertEquals("Tuberculosis", condition.getConcept().getName().getName());
    }

    @Test
    public void shouldNotCreateDuplicateCondition() {
        Condition condition = conditionService.getConditionByUuid("2cc6880e-2c46-11e4-9038-a6c5e4d22fb7");
        conditionService.save(condition);
        List<Condition> conditionsList = conditionService.getConditionsByPatient(condition.getPatient());
        assertEquals(conditionsList.size(), 4);
    }

    @Test
    public void shouldUpdateExistingCondition() {
        Condition condition = conditionService.getConditionByUuid("2cc5840e-2c84-11e4-9038-a6c5e4d22fb7");
        condition.setStatus(Condition.Status.HISTORY_OF);
        Condition savedCondition = conditionService.save(condition);

        assertEquals("Angina", savedCondition.getConcept().getDisplayString());
        assertEquals(Condition.Status.HISTORY_OF, savedCondition.getStatus());

        List<Condition> conditionsList = conditionService.getConditionsByPatient(condition.getPatient());
        assertEquals(conditionsList.size(), 4);
    }

    @Test
    public void shouldVoidGivenCondition() {
        Condition condition = conditionService.getConditionByUuid("2ss6880e-2c46-11e4-5844-a6c5e4d22fb7");
        Condition voidedCondition = conditionService.voidCondition(condition, "voiding");
        assertTrue(voidedCondition.getVoided());
        assertEquals(voidedCondition.getVoidReason(), "voiding");
    }

    @Test
    public void shouldMandateVoidReason() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("voidReason cannot be empty or null");
        Condition condition = conditionService.getConditionByUuid("2ss6880e-2c46-11e4-5844-a6c5e4d22fb7");
        Condition voidedCondition = conditionService.voidCondition(condition, null);
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
