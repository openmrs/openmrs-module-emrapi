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
import org.mockito.Mock;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.module.emrapi.encounter.builder.DrugOrderBuilder;
import org.openmrs.module.emrapi.encounter.builder.TestOrderBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.Arrays;
import java.util.HashSet;

import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterOrdersMapperTest extends BaseModuleContextSensitiveTest {
    private EncounterOrdersMapper encounterOrdersMapper;
    @Mock
    private TestOrderMapper testOrderMapper;
    @Mock
    private DrugOrderMapper drugOrderMapper;

    @Before
    public void setUp() {
        initMocks(this);
        encounterOrdersMapper = new EncounterOrdersMapper(testOrderMapper, drugOrderMapper);
    }

    @Test
    public void shouldMapTestOrdersAndDrugOrdersFromOrders() throws Exception {
        Order testOrder1 = new TestOrderBuilder().build();
        Order testOrder2 = new TestOrderBuilder().build();
        DrugOrder drugOrder1 = new DrugOrderBuilder().build();
        DrugOrder drugOrder2 = new DrugOrderBuilder().build();
        HashSet<Order> orders = new HashSet<Order>(Arrays.asList(testOrder1, drugOrder1, testOrder2, drugOrder2));
        EncounterTransaction encounterTransaction = new EncounterTransaction();

        encounterOrdersMapper.update(encounterTransaction, orders);

        Assert.assertEquals(2, encounterTransaction.getTestOrders().size());
        Assert.assertEquals(2, encounterTransaction.getDrugOrders().size());
        verify(testOrderMapper).map(testOrder1);
        verify(testOrderMapper).map(testOrder2);
        verify(drugOrderMapper).map(drugOrder1);
        verify(drugOrderMapper).map(drugOrder2);
    }
}
