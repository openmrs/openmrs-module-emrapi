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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
	
	@Before
	public void setUp() {
		initMocks(this);
		encounterTransactionMapper = new EncounterTransactionMapper(encounterObservationsMapper, encounterProviderMapper,
		        orderMapper);
		mockedContext = mockStatic(Context.class);
	}
	
	@After
	public void tearDown() {
		mockedContext.close();
	}
	
	@Test
	public void shouldMap() throws Exception {
		Encounter encounter = new EncounterBuilder().build();
		boolean includeAll = false;
		
		mockedContext.when(() -> Context.getRegisteredComponents(EncounterTransactionHandler.class)).thenReturn(null);
		EncounterTransaction encounterTransaction = encounterTransactionMapper.map(encounter, includeAll);
		
		Assert.assertEquals(encounter.getUuid(), encounterTransaction.getEncounterUuid());
		Assert.assertEquals(encounter.getVisit().getUuid(), encounterTransaction.getVisitUuid());
		Assert.assertEquals(encounter.getPatient().getUuid(), encounterTransaction.getPatientUuid());
		Assert.assertEquals(encounter.getEncounterType().getUuid(), encounterTransaction.getEncounterTypeUuid());
		Assert.assertEquals(encounter.getLocation().getUuid(), encounterTransaction.getLocationUuid());
		Assert.assertEquals(encounter.getLocation().getName(), encounterTransaction.getLocationName());
		Assert.assertEquals(encounter.getVisit().getLocation().getUuid(), encounterTransaction.getVisitLocationUuid());
		Assert.assertEquals(encounter.getVisit().getVisitType().getUuid(), encounterTransaction.getVisitTypeUuid());
	}
	
	@Test
	public void shouldMapEncounterWithoutEncounterType() throws Exception {
		Encounter encounter = new EncounterBuilder().withEncounterType(null).build();
		mockedContext.when(() -> Context.getRegisteredComponents(EncounterTransactionHandler.class)).thenReturn(null);
		
		EncounterTransaction encounterTransaction = encounterTransactionMapper.map(encounter, false);
		
		Assert.assertEquals(null, encounterTransaction.getEncounterTypeUuid());
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
