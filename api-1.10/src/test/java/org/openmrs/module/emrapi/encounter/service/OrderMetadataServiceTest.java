/*
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
package org.openmrs.module.emrapi.encounter.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.OrderFrequency;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class OrderMetadataServiceTest extends BaseModuleContextSensitiveTest {

    public static final String DAYS_CONCEPT_NAME = "Days";
    public static final String WEEKS_CONCEPT_NAME = "Weeks";
    public static final String ONCE_A_DAY_CONCEPT_NAME = "Once A Day";
    public static final String TWICE_A_DAY_CONCEPT_NAME = "Twice A Day";

    @Mock
    private OrderService orderService;

    private OrderMetadataService orderMetadataService;

    @Before
    public void setUp() throws Exception {
        OrderService orderService = Context.getOrderService();
        orderMetadataService = new OrderMetadataService(orderService);
    }

    @Test
    public void shouldGetDurationConceptByName() throws Exception {
        Concept days = createConcept(DAYS_CONCEPT_NAME);
        Concept weeks = createConcept(WEEKS_CONCEPT_NAME);
        when(orderService.getDurationUnits()).thenReturn(Arrays.asList(days, weeks));

        Concept durationUnitsConcept = orderMetadataService.getDurationUnitsConceptByName(DAYS_CONCEPT_NAME);

        assertThat(durationUnitsConcept, is(days));
    }

    @Test
    public void shouldReturnNullIfDurationConceptDoesNotExist() throws Exception {
        when(orderService.getDurationUnits()).thenReturn(new ArrayList<Concept>());

        Concept durationUnitsConcept = orderMetadataService.getDurationUnitsConceptByName(DAYS_CONCEPT_NAME);

        assertNull(durationUnitsConcept);
    }

    @Test
    public void shouldReturnNullDurationUnitsForNullInput() {
        assertNull(orderMetadataService.getDurationUnitsConceptByName(null));
    }

    @Test
    public void shouldGetOrderFrequencyByName() throws Exception {
        OrderFrequency onceADayOrderFrequency = new OrderFrequency();
        onceADayOrderFrequency.setConcept(createConcept(ONCE_A_DAY_CONCEPT_NAME));

        OrderFrequency twiceADayOrderFrequency = new OrderFrequency();
        twiceADayOrderFrequency.setConcept(createConcept(TWICE_A_DAY_CONCEPT_NAME));

        when(orderService.getOrderFrequencies(false)).thenReturn(Arrays.asList(onceADayOrderFrequency, twiceADayOrderFrequency));

        OrderFrequency orderFrequency = orderMetadataService.getOrderFrequencyByName(ONCE_A_DAY_CONCEPT_NAME, false);

        assertThat(orderFrequency, is(onceADayOrderFrequency));
    }

    @Test
    public void shouldReturnNullIfOrderFrequencyNotPresent() throws Exception {
        when(orderService.getOrderFrequencies(false)).thenReturn(new ArrayList<OrderFrequency>());

        OrderFrequency orderFrequency = orderMetadataService.getOrderFrequencyByName(ONCE_A_DAY_CONCEPT_NAME, false);

        assertNull(orderFrequency);
    }

    @Test
    public void shouldReturnNullOrderFrequencyForNullInput() {
        assertNull(orderMetadataService.getOrderFrequencyByName(null, false));
    }


    private Concept createConcept(String conceptName) {
        Concept concept = new Concept();
        concept.setPreferredName(new ConceptName(conceptName, Locale.getDefault()));
        return concept;
    }
}