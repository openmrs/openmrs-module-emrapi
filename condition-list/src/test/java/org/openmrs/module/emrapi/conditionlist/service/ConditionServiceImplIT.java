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
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.conditionlist.domain.Condition;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class ConditionServiceImplIT extends BaseModuleContextSensitiveTest {

    @Autowired
    ConditionService conditionService;

    @Autowired
    ConceptService conceptService;

    @Autowired
    PatientService patientService;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        executeDataSet("conditionListDataSet.xml");
    }

    @Test
    public void shouldCreateNewCondition() {
        Condition condition = new Condition();
        assertNull(condition.getId());

        condition.setStatus(Condition.Status.PRESUMED);

        Concept tuberculosisConcept = conceptService.getConceptByName("Tuberculosis");
        condition.setConcept(tuberculosisConcept);

        Patient patient = patientService.getPatient(2);
        condition.setPatient(patient);

        Date dateCreated = new Date();
        condition.setDateCreated(dateCreated);

        condition.setUuid("3584c584-c291-46c8-8584-96dc33d19584");
        conditionService.save(condition);

        List<Condition> allConditions = conditionService.getConditions(patient);
        assertEquals(allConditions.size(), 7);

        assertTrue(condition.getId() > 0);
        assertEquals(condition.getUuid(), "3584c584-c291-46c8-8584-96dc33d19584");
        assertEquals(condition.getStatus(), Condition.Status.PRESUMED);
        assertEquals(condition.getConcept(), tuberculosisConcept);
        assertEquals(condition.getPatient(), patient);
    }

    @Test
    public void shouldNotCreateDuplicateCondition() {
        Condition condition = conditionService.getConditionByUuid("2cc6880e-2c46-11e4-9038-a6c5e4d22fb7");
        conditionService.save(condition);
        List<Condition> conditionsList = conditionService.getConditions(condition.getPatient());
        assertEquals(conditionsList.size(), 6);
    }

    @Test
    public void shouldUpdateExistingCondition() {
        Condition condition = conditionService.getConditionByUuid("2cc5840e-2c84-11e4-9038-a6c5e4d22fb7");
        condition.setStatus(Condition.Status.HISTORY_OF);
        Condition savedCondition = conditionService.save(condition);

        assertEquals("Angina", savedCondition.getConcept().getDisplayString());
        assertEquals(Condition.Status.HISTORY_OF, savedCondition.getStatus());

        List<Condition> conditionsList = conditionService.getConditions(condition.getPatient());
        assertEquals(conditionsList.size(), 6);
    }

    @Test
    public void shouldNotAllowNonCodedConceptConditionWithoutNonCodedConditionValue() {
        thrown.expect(APIException.class);
        thrown.expectMessage("Non-coded value needed for non-coded condition");
        Condition condition = conditionService.getConditionByUuid("2ss6880e-2c46-11e4-9038-a6c5e4d22fb7");
        conditionService.save(condition);
    }

    @Test
    public void shouldNotAllowNonCodedValueForCodedConceptCondition() {
        thrown.expect(APIException.class);
        thrown.expectMessage("Non-coded value not supported for coded condition");
        Condition condition = conditionService.getConditionByUuid("2ss6880e-2c46-11e4-5844-a6c5e4d22fb7");
        conditionService.save(condition);
    }

    @Test
    public void shouldNotAllowDuplicateConditionsWhiteSpacesAndCaseChangesInConditionNonCodedValue() {
        thrown.expect(APIException.class);
        thrown.expectMessage("Duplicates are not allowed");
        Condition condition = new Condition();
        assertNull(condition.getId());

        condition.setStatus(Condition.Status.PRESUMED);

        Concept nonCodedConcept = conceptService.getConceptByName("OTHER, NON-CODED");
        condition.setConcept(nonCodedConcept);

        Patient patient = patientService.getPatient(2);
        condition.setPatient(patient);

        Date dateCreated = new Date();
        condition.setDateCreated(dateCreated);

        condition.setUuid("3584c584-c291-46c8-8584-96dc33d19584");
        condition.setConditionNonCoded("P a I n ");
        conditionService.save(condition);
    }

    @Test
    public void shouldNotAllowUpdatingConceptInCondition() {
        thrown.expect(APIException.class);
        thrown.expectMessage("Concept cannot be updated");

        Condition condition = new Condition();
        condition.setStatus(Condition.Status.CONFIRMED);

        Concept anginaConcept = conceptService.getConceptByName("Angina");
        condition.setConcept(anginaConcept);

        Patient patient = patientService.getPatient(1);
        condition.setPatient(patient);

        Date dateCreated = new Date();
        condition.setDateCreated(dateCreated);

        condition.setUuid("2cc6880e-2c46-11e4-9038-a6c5e4d22fb7");
        conditionService.save(condition);
    }
}
