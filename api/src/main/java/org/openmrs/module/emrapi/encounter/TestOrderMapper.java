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

import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.TestOrder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.List;

public class TestOrderMapper {
    private final ConceptMapper conceptMapper = new ConceptMapper();

    public List<EncounterTransaction.TestOrder> map(Encounter encounter) {
        List<EncounterTransaction.TestOrder> testOrders = new ArrayList<EncounterTransaction.TestOrder>();
        for (Order order : encounter.getOrders()) {
            if (order instanceof TestOrder) {
                testOrders.add(map(order));
            }
        }
        return testOrders;
    }

    public EncounterTransaction.TestOrder map(Order order) {
        EncounterTransaction.TestOrder emrTestOrder = new EncounterTransaction.TestOrder();
        emrTestOrder.setUuid(order.getUuid());
        emrTestOrder.setConcept(conceptMapper.map(order.getConcept()));
        emrTestOrder.setInstructions(order.getInstructions());
        emrTestOrder.setOrderTypeUuid(order.getOrderType() != null ? order.getOrderType().getUuid() : null);
        emrTestOrder.setVoided(order.getVoided());
        emrTestOrder.setVoidReason(order.getVoidReason());
        emrTestOrder.setDateCreated(order.getDateCreated());
        emrTestOrder.setDateChanged(order.getDateChanged());
        return emrTestOrder;
    }
}