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

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.encounter.mapper.OpenMRSDrugOrderMapper;
import org.openmrs.module.emrapi.encounter.builder.DrugOrderBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.text.ParseException;
import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

public class EmrOrderServiceImplTest {

    @Mock
    private EncounterService encounterService;

    @Mock
    private OpenMRSDrugOrderMapper openMRSDrugOrderMapper;
    @Mock
    private OrderService orderService;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void shouldSaveANewDrugOrder() throws ParseException {
        EmrOrderServiceImpl emrOrderService = new EmrOrderServiceImpl(openMRSDrugOrderMapper, encounterService);

        EncounterTransaction.DrugOrder drugOrder = DrugOrderBuilder.sample("drug-uuid", "day");
        DrugOrder mappedDrugOrder = new DrugOrder();
        Encounter encounter = new Encounter();
        when(openMRSDrugOrderMapper.map(any(EncounterTransaction.DrugOrder.class),
                same(encounter)))
                .thenReturn(mappedDrugOrder);

        emrOrderService.save(Arrays.asList(drugOrder), encounter);

        ArgumentCaptor<Encounter> encounterArgumentCaptor = ArgumentCaptor.forClass(Encounter.class);
        verify(encounterService).saveEncounter(encounterArgumentCaptor.capture());
        DrugOrder savedDrugOrder = (DrugOrder) encounterArgumentCaptor.getValue().getOrders().iterator().next();
        assertThat(savedDrugOrder, is(sameInstance(mappedDrugOrder)));
    }
}