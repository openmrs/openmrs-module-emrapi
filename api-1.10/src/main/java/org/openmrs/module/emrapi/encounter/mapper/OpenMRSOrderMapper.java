/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.emrapi.encounter.mapper;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.CareSettingType;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Iterator;

/**
 * OpenMRSOrderMapper.
 * Maps EncounterTransaction Order to OpenMRS Orders.
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
        openMRSOrder.setCareSetting(orderService.getCareSettingByName(CareSettingType.OUTPATIENT.toString()));

        openMRSOrder.setEncounter(encounter);
        openMRSOrder.setAutoExpireDate(order.getAutoExpireDate());
        openMRSOrder.setCommentToFulfiller(order.getCommentToFulfiller());
        openMRSOrder.setConcept(getConceptFrom(order, openMRSOrder));
        openMRSOrder.setOrderer(getProviderForOrders(encounter));

        return openMRSOrder;
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
        return order.getAction() != null && org.openmrs.Order.Action.valueOf(order.getAction()) == org.openmrs.Order.Action.DISCONTINUE;
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
