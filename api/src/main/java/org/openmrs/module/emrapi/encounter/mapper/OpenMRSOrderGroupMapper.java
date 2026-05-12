/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
		if (StringUtils.isNotEmpty(encounterOrderGroup.getUuid())) {
			OrderGroup orderGroup = orderService.getOrderGroupByUuid(encounterOrderGroup.getUuid());
			if (orderGroup.getEncounter().getUuid().equals(encounter.getUuid())) {
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
