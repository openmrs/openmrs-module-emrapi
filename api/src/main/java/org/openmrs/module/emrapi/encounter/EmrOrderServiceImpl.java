/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
	
	private OrderGroup mapToOpenMRSOrderGroup(Set<OrderGroup> orderGroups, EncounterTransaction.OrderGroup newOrderGroup,
	        Encounter encounter) {
		
		if (newOrderGroup == null) {
			return null;
		}
		for (OrderGroup orderGroup : orderGroups) {
			if (orderGroup.getOrderSet().getUuid().equals(newOrderGroup.getOrderSet().getUuid())) {
				return orderGroup;
			}
		}
		
		if (StringUtils.isNotEmpty(newOrderGroup.getOrderSet().getUuid())) {
			OrderGroup orderGroup = openMRSOrderGroupMapper.map(newOrderGroup, encounter);
			orderGroups.add(orderGroup);
			return orderGroup;
		}
		return null;
	}
}
