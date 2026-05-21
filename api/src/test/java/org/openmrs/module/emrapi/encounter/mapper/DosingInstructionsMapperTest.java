/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
import org.openmrs.module.emrapi.encounter.OrderMetadataService;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openmrs.module.emrapi.encounter.domain.EncounterTransaction.DosingInstructions;

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
		when(orderMetadataService.getDoseUnitsConceptByName(dosingInstructions.getDoseUnits())).thenReturn(capsuleConcept);
		Concept routeConcept = new Concept();
		when(orderMetadataService.getRouteConceptByName(dosingInstructions.getRoute())).thenReturn(routeConcept);
		Concept frequencyConcept = new Concept();
		when(conceptService.getConceptByName(dosingInstructions.getFrequency())).thenReturn(frequencyConcept);
		OrderFrequency orderFrequency = new OrderFrequency();
		when(orderMetadataService.getOrderFrequencyByName("QDS", false)).thenReturn(orderFrequency);
		Concept quantityUnits = new Concept();
		when(orderMetadataService.getDispenseUnitsConceptByName(dosingInstructions.getQuantityUnits()))
		        .thenReturn(quantityUnits);
		
		DrugOrder drugOrder = new DrugOrder();
		DosingInstructionsMapper dosingInstructionsMapper = new DosingInstructionsMapper(conceptService,
		        orderMetadataService);
		
		dosingInstructionsMapper.map(dosingInstructions, drugOrder);
		
		assertThat(drugOrder.getDosingInstructions(), is(equalTo("AC")));
		assertThat(drugOrder.getDose(), is(equalTo(2.0)));
		assertThat(drugOrder.getDoseUnits(), is(capsuleConcept));
		assertThat(drugOrder.getRoute(), is(equalTo(routeConcept)));
		assertThat(drugOrder.getFrequency(), is(equalTo(orderFrequency)));
		assertThat(drugOrder.getAsNeeded(), is(equalTo(false)));
		assertThat(drugOrder.getQuantity(), is(equalTo(dosingInstructions.getQuantity())));
		assertThat(drugOrder.getQuantityUnits(), is(equalTo(quantityUnits)));
		assertThat(drugOrder.getNumRefills(), is(equalTo(dosingInstructions.getNumberOfRefills())));
	}
	
	@Test
	public void shouldDefaultNumRefillsToZeroIfNotAvailable() {
		DosingInstructions dosingInstructions = DosingInstructionsBuilder.sample();
		dosingInstructions.setNumberOfRefills(null);
		DrugOrder drugOrder = new DrugOrder();
		
		DosingInstructionsMapper dosingInstructionsMapper = new DosingInstructionsMapper(conceptService,
		        orderMetadataService);
		
		dosingInstructionsMapper.map(dosingInstructions, drugOrder);
		assertThat(drugOrder.getNumRefills(), is(equalTo(0)));
	}
}
