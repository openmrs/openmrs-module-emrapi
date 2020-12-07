/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.fhircondition.api.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.fhir2.api.dao.FhirConditionDao;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FhirConditionServiceImplTest {

    private static final String CONDITION_UUID = "ca0dfd38-ee20-41a6-909e-7d84247ca192";

    private static final String WRONG_CONDITION_UUID = "tx0dfd38-ee20-41a6-909e-7d84247c8340";

    @Mock
    private FhirConditionDao<Condition> dao;

    @Mock
    private ConditionTranslator<Condition> translator;

    private FhirConditionServiceImpl conditionService;

    private Condition openmrsCondition;

    private org.hl7.fhir.r4.model.Condition fhirCondition;

    @Before
    public void setup() {
        conditionService = new FhirConditionServiceImpl();
        conditionService.setDao(dao);
        conditionService.setTranslator(translator);

        openmrsCondition = new Condition();
        openmrsCondition.setUuid(CONDITION_UUID);

        fhirCondition = new org.hl7.fhir.r4.model.Condition();
        fhirCondition.setId(CONDITION_UUID);
    }

    @Test
    public void getConditionByUuid_shouldReturnCondition() {
        when(dao.get(CONDITION_UUID)).thenReturn(openmrsCondition);
        when(translator.toFhirResource(openmrsCondition)).thenReturn(fhirCondition);
        org.hl7.fhir.r4.model.Condition result = conditionService.get(CONDITION_UUID);
        assertThat(result, notNullValue());
        assertThat(result.getId(), equalTo(CONDITION_UUID));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void getConditionByWrongUuid_shouldReturnCondition() {
        assertThat(conditionService.get(WRONG_CONDITION_UUID), nullValue());
    }
}
