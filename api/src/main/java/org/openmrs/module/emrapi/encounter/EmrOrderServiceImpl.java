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

import org.apache.commons.lang3.StringUtils;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderSetService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.mapper.OpenMRSDrugOrderMapper;
import org.openmrs.module.emrapi.encounter.mapper.OpenMRSOrderGroupMapper;
import org.openmrs.module.emrapi.encounter.mapper.OpenMRSOrderMapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Transactional
public class EmrOrderServiceImpl implements EmrOrderService {

    private EncounterService encounterService;
    private OpenMRSDrugOrderMapper openMRSDrugOrderMapper;
    private OpenMRSOrderMapper openMRSOrderMapper;
    private OrderSetService orderSetService;
    private OpenMRSOrderGroupMapper openMRSOrderGroupMapper;

    public EmrOrderServiceImpl(OpenMRSDrugOrderMapper openMRSDrugOrderMapper, EncounterService encounterService,
                               OpenMRSOrderMapper openMRSOrderMapper, OrderSetService orderSetService,
                               OpenMRSOrderGroupMapper openMRSOrderGroupMapper) {
        this.openMRSDrugOrderMapper = openMRSDrugOrderMapper;
        this.encounterService = encounterService;
        this.openMRSOrderMapper = openMRSOrderMapper;
        this.openMRSOrderGroupMapper = openMRSOrderGroupMapper;
        this.orderSetService = orderSetService;
    }

    @Override
    public void save(List<EncounterTransaction.DrugOrder> drugOrders, Encounter encounter) {
        Set<OrderGroup> orderGroups = new LinkedHashSet<OrderGroup>();

        for (EncounterTransaction.DrugOrder drugOrder : drugOrders) {
            OrderGroup orderGroup = mapToOpenMRSOrderGroup(orderGroups, drugOrder.getOrderGroup(), encounter);
            DrugOrder omrsDrugOrder = openMRSDrugOrderMapper.map(drugOrder, encounter);
            omrsDrugOrder.setOrderGroup(orderGroup);
            encounter.addOrder(omrsDrugOrder);
        }
        encounterService.saveEncounter(encounter);
    }

    @Override
    public void saveOrders(List<EncounterTransaction.Order> orders, Encounter encounter) {
        Set<OrderGroup> orderGroups = new LinkedHashSet<OrderGroup>();

        for (EncounterTransaction.Order order : orders) {
            OrderGroup orderGroup = mapToOpenMRSOrderGroup(orderGroups, order.getOrderGroup(), encounter);

            Order omrsOrder = openMRSOrderMapper.map(order, encounter);
            omrsOrder.setOrderGroup(orderGroup);

            encounter.addOrder(omrsOrder);
        }
        encounterService.saveEncounter(encounter);
    }

    private OrderGroup mapToOpenMRSOrderGroup(Set<OrderGroup> orderGroups, EncounterTransaction.OrderGroup newOrderGroup, Encounter encounter) {

        if(newOrderGroup == null){
            return  null;
        }
        for (OrderGroup orderGroup : orderGroups) {
            if (orderGroup.getOrderSet().getUuid().equals(newOrderGroup.getOrderSet().getUuid())) {
                return orderGroup;
            }
        }

        if(StringUtils.isNotEmpty(newOrderGroup.getOrderSet().getUuid())){
            OrderGroup orderGroup = openMRSOrderGroupMapper.map(newOrderGroup, encounter);
            orderGroups.add(orderGroup);
            return orderGroup;
        }
        return null;
    }
}
