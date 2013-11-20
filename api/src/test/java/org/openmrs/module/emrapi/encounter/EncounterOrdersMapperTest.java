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

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.module.emrapi.encounter.builder.DrugOrderBuilder;
import org.openmrs.module.emrapi.encounter.builder.TestOrderBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class EncounterOrdersMapperTest {
    private EncounterOrdersMapper encounterOrdersMapper;

    @Before
    public void setUp() {
        encounterOrdersMapper = new EncounterOrdersMapper(new TestOrderMapper(), new DrugOrderMapper());
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

        Assert.assertThat(Arrays.asList(testOrder1.getUuid(), testOrder2.getUuid()),
                IsIterableContainingInAnyOrder.containsInAnyOrder(getUuidsForTestOrders(encounterTransaction.getTestOrders()).toArray()));
        Assert.assertThat(Arrays.asList(drugOrder1.getUuid(), drugOrder2.getUuid()),
                IsIterableContainingInAnyOrder.containsInAnyOrder(getUuidsForDrugOrders(encounterTransaction.getDrugOrders()).toArray()));
    }

    private List<String> getUuidsForDrugOrders(List<EncounterTransaction.DrugOrder> drugOrders) {
        ArrayList<String> uuids = new ArrayList<String>();
        for(EncounterTransaction.DrugOrder order: drugOrders) {
            uuids.add(order.getUuid());
        }
        return uuids;
    }

    private List<String> getUuidsForTestOrders(List<EncounterTransaction.TestOrder> orders) {
        ArrayList<String> uuids = new ArrayList<String>();
        for(EncounterTransaction.TestOrder order: orders) {
            uuids.add(order.getUuid());
        }
        return uuids;
    }
}
