/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.encounter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.builder.EncounterBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.postprocessor.EncounterTransactionHandler;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterTransactionMapperTest {
	
	@Mock
	private EncounterObservationsMapper encounterObservationsMapper;
	
	@Mock
	private EncounterProviderMapper encounterProviderMapper;
	
	@Mock
	private EmrOrderService emrOrderService;
	
	@Mock
	private OrderMapper orderMapper;
	
	private MockedStatic<Context> mockedContext;
	
	private EncounterTransactionMapper encounterTransactionMapper;
	
	@BeforeEach
	public void setUp() {
		initMocks(this);
		encounterTransactionMapper = new EncounterTransactionMapper(encounterObservationsMapper, encounterProviderMapper,
		        orderMapper);
		mockedContext = mockStatic(Context.class);
	}
	
	@AfterEach
	public void tearDown() {
		mockedContext.close();
	}
	
	@Test
	public void shouldMap() throws Exception {
		Encounter encounter = new EncounterBuilder().build();
		boolean includeAll = false;
		
		mockedContext.when(() -> Context.getRegisteredComponents(EncounterTransactionHandler.class)).thenReturn(null);
		EncounterTransaction encounterTransaction = encounterTransactionMapper.map(encounter, includeAll);
		
		Assertions.assertEquals(encounter.getUuid(), encounterTransaction.getEncounterUuid());
		Assertions.assertEquals(encounter.getVisit().getUuid(), encounterTransaction.getVisitUuid());
		Assertions.assertEquals(encounter.getPatient().getUuid(), encounterTransaction.getPatientUuid());
		Assertions.assertEquals(encounter.getEncounterType().getUuid(), encounterTransaction.getEncounterTypeUuid());
		Assertions.assertEquals(encounter.getLocation().getUuid(), encounterTransaction.getLocationUuid());
		Assertions.assertEquals(encounter.getLocation().getName(), encounterTransaction.getLocationName());
		Assertions.assertEquals(encounter.getVisit().getLocation().getUuid(), encounterTransaction.getVisitLocationUuid());
		Assertions.assertEquals(encounter.getVisit().getVisitType().getUuid(), encounterTransaction.getVisitTypeUuid());
	}
	
	@Test
	public void shouldMapEncounterWithoutEncounterType() throws Exception {
		Encounter encounter = new EncounterBuilder().withEncounterType(null).build();
		mockedContext.when(() -> Context.getRegisteredComponents(EncounterTransactionHandler.class)).thenReturn(null);
		
		EncounterTransaction encounterTransaction = encounterTransactionMapper.map(encounter, false);
		
		Assertions.assertEquals(null, encounterTransaction.getEncounterTypeUuid());
	}
	
	@Test
	public void shouldMapEncounterTransactionsWithExtensions() {
		Encounter encounter = new EncounterBuilder().build();
		boolean includeAll = false;
		
		EncounterTransactionHandler encounterTransactionHandler = mock(EncounterTransactionHandler.class);
		mockedContext.when(() -> Context.getRegisteredComponents(EncounterTransactionHandler.class))
		        .thenReturn(Arrays.asList(encounterTransactionHandler));
		
		encounterTransactionMapper = new EncounterTransactionMapper(encounterObservationsMapper, encounterProviderMapper,
		        orderMapper);
		
		EncounterTransaction encounterTransaction = encounterTransactionMapper.map(encounter, includeAll);
		verify(encounterTransactionHandler).forRead(eq(encounter), any(EncounterTransaction.class));
		
	}
}
