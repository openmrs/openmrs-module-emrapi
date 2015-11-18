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
package org.openmrs.module.emrapi.encounter.service;

import org.openmrs.Concept;
import org.openmrs.OrderFrequency;
import org.openmrs.api.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMetadataService {
    private OrderService orderService;

    @Autowired
    public OrderMetadataService(OrderService orderService) {
        this.orderService = orderService;
    }

    public Concept getDurationUnitsConceptByName(String conceptName) {
        List<Concept> durationUnits = orderService.getDurationUnits();
        for (Concept durationUnit : durationUnits) {
            if(durationUnit.getName().getName().equals(conceptName)){
                return durationUnit;
            }
        }
        return null;
    }

    public OrderFrequency getOrderFrequencyByName(String conceptName, boolean includeRetired) {
        List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies(includeRetired);
        for (OrderFrequency orderFrequency : orderFrequencies) {
            if(orderFrequency.getName().equals(conceptName)){
                return orderFrequency;
            }
        }
        return null;
    }
}
