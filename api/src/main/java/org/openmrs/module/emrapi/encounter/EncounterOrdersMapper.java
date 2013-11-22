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

import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.TestOrder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Set;

public class EncounterOrdersMapper {
    private final TestOrderMapper testOrderMapper;
    private final DrugOrderMapper drugOrderMapper;

    public EncounterOrdersMapper(TestOrderMapper testOrderMapper, DrugOrderMapper drugOrderMapper) {
        this.testOrderMapper = testOrderMapper;
        this.drugOrderMapper = drugOrderMapper;
    }

    void update(EncounterTransaction encounterTransactionResponse, Set<Order> orders) {
        for (Order order : orders) {
            if (order instanceof TestOrder) {
                encounterTransactionResponse.addTestOrder(testOrderMapper.map(order));
            } else if (order instanceof DrugOrder) {
                encounterTransactionResponse.addDrugOrder(drugOrderMapper.map((DrugOrder) order));
            }
        }
    }
}