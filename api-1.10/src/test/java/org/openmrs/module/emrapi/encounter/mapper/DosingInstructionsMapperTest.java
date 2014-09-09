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
package org.openmrs.module.emrapi.encounter.mapper;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.OrderFrequency;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.builder.DosingInstructionsBuilder;
import org.openmrs.module.emrapi.encounter.service.OrderMetadataService;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openmrs.module.emrapi.encounter.domain.EncounterTransaction.DosingInstructions;
import static org.powermock.api.mockito.PowerMockito.when;

public class DosingInstructionsMapperTest {

    @Mock
    private OrderMetadataService orderMetadataService;
    @Mock
    private ConceptService conceptService;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void shouldMapDosingInstructionsToDrugOrder() {
        DosingInstructions dosingInstructions = DosingInstructionsBuilder.sample();
        Concept capsuleConcept = new Concept();
        when(conceptService.getConceptByName(dosingInstructions.getDoseUnits())).thenReturn(capsuleConcept);
        Concept routeConcept = new Concept();
        when(conceptService.getConceptByName(dosingInstructions.getRoute())).thenReturn(routeConcept);
        Concept frequencyConcept = new Concept();
        when(conceptService.getConceptByName(dosingInstructions.getFrequency())).thenReturn(frequencyConcept);
        OrderFrequency orderFrequency = new OrderFrequency();
        when(orderMetadataService.getOrderFrequencyByName("QDS", false)).thenReturn(orderFrequency);
        Concept quantityUnits = new Concept();
        when(conceptService.getConceptByName(dosingInstructions.getQuantityUnits())).thenReturn(quantityUnits);

        DrugOrder drugOrder = new DrugOrder();
        DosingInstructionsMapper dosingInstructionsMapper = new DosingInstructionsMapper(conceptService, orderMetadataService);

        dosingInstructionsMapper.map(dosingInstructions, drugOrder);

        assertThat(drugOrder.getDosingInstructions(), is(equalTo("AC")));
        assertThat(drugOrder.getDose(), is(equalTo(2.0)));
        assertThat(drugOrder.getDoseUnits(), is(capsuleConcept));
        assertThat(drugOrder.getRoute(), is(equalTo(routeConcept)));
        assertThat(drugOrder.getFrequency(), is(equalTo(orderFrequency)));
        assertThat(drugOrder.getAsNeeded(), is(equalTo(false)));
        assertThat(drugOrder.getQuantity(), is(equalTo(Double.valueOf(dosingInstructions.getQuantity()))));
        assertThat(drugOrder.getQuantityUnits(), is(equalTo(quantityUnits)));
        assertThat(drugOrder.getNumRefills(), is(equalTo(dosingInstructions.getNumberOfRefills())));
    }

    @Test
    public void shouldDefaultNumRefillsToZeroIfNotAvailable() {
        DosingInstructions dosingInstructions = DosingInstructionsBuilder.sample();
        dosingInstructions.setNumberOfRefills(null);
        DrugOrder drugOrder = new DrugOrder();

        DosingInstructionsMapper dosingInstructionsMapper = new DosingInstructionsMapper(conceptService, orderMetadataService);

        dosingInstructionsMapper.map(dosingInstructions, drugOrder);
        assertThat(drugOrder.getNumRefills(), is(equalTo(0)));
    }
}
