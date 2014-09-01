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
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.api.EncounterService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.mapper.DrugOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service(value = "emrOrderService")
@Transactional
public class EmrOrderServiceImpl implements EmrOrderService{
    private final DrugOrderMapper drugOrderMapper;
    private final EncounterService encounterService;

    @Autowired
    public EmrOrderServiceImpl(DrugOrderMapper drugOrderMapper, EncounterService encounterService) {
        this.drugOrderMapper = drugOrderMapper;
        this.encounterService = encounterService;
    }

    @Override
    public void save(List<EncounterTransaction.DrugOrder> drugOrders, Encounter encounter) {
        Set<Order> orders = new HashSet<Order>();
        for (EncounterTransaction.DrugOrder drugOrder : drugOrders) {
            DrugOrder omrsDrugOrder= drugOrderMapper.map(drugOrder, encounter);
            orders.add(omrsDrugOrder);
        }
        for (Order order : orders) {
            encounter.addOrder(order);
        }
        encounterService.saveEncounter(encounter);
    }

}
