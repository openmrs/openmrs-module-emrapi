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
			if (durationUnit.getName().getName().equals(conceptName)) {
				return durationUnit;
			}
		}
		return null;
	}
	
	public OrderFrequency getOrderFrequencyByName(String conceptName, boolean includeAll) {
		List<OrderFrequency> orderFrequencies = orderService.getOrderFrequencies(includeAll);
		for (OrderFrequency orderFrequency : orderFrequencies) {
			if (orderFrequency.getName().equals(conceptName)) {
				return orderFrequency;
			}
		}
		return null;
	}
	
	public Concept getDoseUnitsConceptByName(String conceptName) {
		List<Concept> dosingUnits = orderService.getDrugDosingUnits();
		for (Concept doseUnit : dosingUnits) {
			if (doseUnit.getName().getName().equals(conceptName)) {
				return doseUnit;
			}
		}
		return null;
	}
	
	public Concept getDispenseUnitsConceptByName(String conceptName) {
		List<Concept> dispensingUnits = orderService.getDrugDispensingUnits();
		for (Concept dispensingUnit : dispensingUnits) {
			if (dispensingUnit.getName().getName().equals(conceptName)) {
				return dispensingUnit;
			}
		}
		return null;
	}
	
	public Concept getRouteConceptByName(String conceptName) {
		List<Concept> drugRoutes = orderService.getDrugRoutes();
		for (Concept route : drugRoutes) {
			if (route.getName().getName().equals(conceptName)) {
				return route;
			}
		}
		return null;
	}
	
}
