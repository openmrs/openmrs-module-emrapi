/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p/>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p/>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.emrapi.encounter.mapper;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.OrderGroup;
import org.openmrs.api.OrderService;
import org.openmrs.api.OrderSetService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public class OpenMRSOrderGroupMapper {
    private OrderSetService orderSetService;
    private OrderService orderService;

    public OpenMRSOrderGroupMapper(OrderSetService orderSetService, OrderService orderService) {
        this.orderSetService = orderSetService;
        this.orderService = orderService;
    }


    public OrderGroup map(EncounterTransaction.OrderGroup encounterOrderGroup, Encounter encounter) {
        if(StringUtils.isNotEmpty(encounterOrderGroup.getUuid())) {
            OrderGroup orderGroup = orderService.getOrderGroupByUuid(encounterOrderGroup.getUuid());
            if(orderGroup.getEncounter().getUuid().equals(encounter.getUuid())){
                return orderGroup;
            }
        }
        OrderGroup omrsOrderGroup = new OrderGroup();
        omrsOrderGroup.setPatient(encounter.getPatient());
        omrsOrderGroup.setOrderSet(orderSetService.getOrderSetByUuid(encounterOrderGroup.getOrderSet().getUuid()));
        omrsOrderGroup.setEncounter(encounter);
        return omrsOrderGroup;
    }
}
