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
package org.openmrs.module.emrapi.encounter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.builder.EncounterBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.postprocessor.EncounterTransactionHandler;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class EncounterTransactionMapperTest {
    @Mock
    private EncounterObservationsMapper encounterObservationsMapper;
    @Mock
    private EncounterProviderMapper encounterProviderMapper;
    @Mock
    private EmrOrderService emrOrderService;
    @Mock
    private OrderMapper orderMapper;

    private EncounterTransactionMapper encounterTransactionMapper;

    @Before
    public void setUp() {
        initMocks(this);
        PowerMockito.mockStatic(Context.class);
        encounterTransactionMapper = new EncounterTransactionMapper(encounterObservationsMapper, encounterProviderMapper, orderMapper);
    }

    @Test
    public void shouldMap() throws Exception {
        Encounter encounter = new EncounterBuilder().build();
        boolean includeAll = false;

        when(Context.getRegisteredComponents(EncounterTransactionHandler.class)).thenReturn(null);
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
        when(Context.getRegisteredComponents(EncounterTransactionHandler.class)).thenReturn(null);

        EncounterTransaction encounterTransaction = encounterTransactionMapper.map(encounter, false);

        Assert.assertEquals(null, encounterTransaction.getEncounterTypeUuid());
    }

    @Test
    public void shouldMapEncounterTransactionsWithExtensions(){
        Encounter encounter = new EncounterBuilder().build();
        boolean includeAll = false;

        EncounterTransactionHandler encounterTransactionHandler = mock(EncounterTransactionHandler.class);
        when(Context.getRegisteredComponents(EncounterTransactionHandler.class)).thenReturn(
                Arrays.asList(encounterTransactionHandler));

        encounterTransactionMapper = new EncounterTransactionMapper(encounterObservationsMapper, encounterProviderMapper, orderMapper);

        EncounterTransaction encounterTransaction = encounterTransactionMapper.map(encounter, includeAll);
        verify(encounterTransactionHandler).forRead(eq(encounter), any(EncounterTransaction.class));

    }
}
