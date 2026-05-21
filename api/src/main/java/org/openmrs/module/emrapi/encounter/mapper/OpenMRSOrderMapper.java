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
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Iterator;

/**
 * OpenMRSOrderMapper. Maps EncounterTransaction Order to OpenMRS Orders.
 * <p/>
 * Version 1.0
 */
public class OpenMRSOrderMapper {
	
	private OrderService orderService;
	
	private ConceptService conceptService;
	
	public OpenMRSOrderMapper(OrderService orderService, ConceptService conceptService) {
		this.orderService = orderService;
		this.conceptService = conceptService;
	}
	
	public Order map(EncounterTransaction.Order order, Encounter encounter) {
		
		Order openMRSOrder = createOrder(order);
		openMRSOrder.setUrgency(getOrderUrgency(order));
		openMRSOrder.setCareSetting(orderService.getCareSettingByName(CareSetting.CareSettingType.OUTPATIENT.toString()));
		openMRSOrder.setEncounter(encounter);
		openMRSOrder.setAutoExpireDate(order.getAutoExpireDate());
		openMRSOrder.setCommentToFulfiller(order.getCommentToFulfiller());
		openMRSOrder.setConcept(getConceptFrom(order, openMRSOrder));
		openMRSOrder.setOrderer(getProviderForOrders(encounter));
		
		return openMRSOrder;
	}
	
	private Order.Urgency getOrderUrgency(EncounterTransaction.Order order) {
		try {
			if (order.getUrgency() != null) {
				return Order.Urgency.valueOf(order.getUrgency());
			}
		}
		catch (Exception e) {
			throw new APIException("Invalid urgency type " + order.getUrgency());
		}
		return Order.Urgency.ROUTINE;
	}
	
	private Order createOrder(EncounterTransaction.Order order) {
		if (isNewOrder(order)) {
			return new Order();
		} else if (isDiscontinuationOrder(order)) {
			return orderService.getOrderByUuid(order.getPreviousOrderUuid()).cloneForDiscontinuing();
		} else {
			return orderService.getOrderByUuid(order.getPreviousOrderUuid()).cloneForRevision();
		}
	}
	
	private boolean isDiscontinuationOrder(EncounterTransaction.Order order) {
		return order.getAction() != null && Order.Action.valueOf(order.getAction()) == Order.Action.DISCONTINUE;
	}
	
	private Concept getConceptFrom(EncounterTransaction.Order order, Order openMRSOrder) {
		if (!isNewOrder(order)) {
			return openMRSOrder.getConcept();
		}
		
		EncounterTransaction.Concept concept = order.getConcept();
		Concept conceptByUuid = conceptService.getConceptByUuid(concept.getUuid());
		if (conceptByUuid == null) {
			throw new APIException("No such Concept : " + order.getConcept().getName());
		}
		return conceptByUuid;
	}
	
	private boolean isNewOrder(EncounterTransaction.Order order) {
		return StringUtils.isBlank(order.getUuid()) && StringUtils.isBlank(order.getPreviousOrderUuid());
	}
	
	private Provider getProviderForOrders(Encounter encounter) {
		Iterator<EncounterProvider> providers = encounter.getEncounterProviders().iterator();
		
		if (providers.hasNext()) {
			return providers.next().getProvider();
		}
		
		throw new APIException("Encounter doesn't have a provider.");
	}
	
}
