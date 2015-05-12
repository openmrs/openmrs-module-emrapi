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
package org.openmrs.module.emrapi.encounter.mapper;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Provider;
import org.openmrs.Order;
import org.openmrs.EncounterProvider;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.CareSettingType;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Date;
import java.util.Iterator;

/**
 * OpenMRSOrderMapper.
 * Maps EncounterTransaction Order to OpenMRS Order.
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

        Order openMRSOrder = null;

        if (isNewOrder(order)) {
            openMRSOrder = new Order();
            openMRSOrder.setConcept(getConceptFrom(order, openMRSOrder));
            openMRSOrder.setEncounter(encounter);
            openMRSOrder.setOrderer(getProviderForOrders(encounter));
            openMRSOrder.setCareSetting(orderService.getCareSettingByName(CareSettingType.OUTPATIENT.toString()));
        }
        else{
            openMRSOrder = getOrderByUuid(order);
            openMRSOrder.setDateChanged(new Date());
        }

        openMRSOrder.setVoided(order.isVoided());
        openMRSOrder.setVoidReason(order.getVoidReason());

        return  openMRSOrder;
    }

    private Order getOrderByUuid(EncounterTransaction.Order order){
        Order openMRSOrder = (Order) orderService.getOrderByUuid(order.getUuid());
        if(openMRSOrder == null) {
            throw new APIException("No test order with uuid : " + order.getUuid());
        }
        return openMRSOrder;
    }

    private Concept getConceptFrom(EncounterTransaction.Order order, Order openMRSOrder) {
        if (!isNewOrder(order)) {
            return openMRSOrder.getConcept();
        }

        EncounterTransaction.Concept concept = order.getConcept();
        Concept conceptByUuid = conceptService.getConceptByUuid(concept.getUuid());
        if(conceptByUuid == null){
            throw new APIException("No such Concept : " + order.getConcept().getName());
        }
        return conceptByUuid;
    }


    private boolean isNewOrder(EncounterTransaction.Order order) {
        return StringUtils.isBlank(order.getUuid());
    }

    private Provider getProviderForOrders(Encounter encounter){
        Iterator<EncounterProvider> providers = encounter.getEncounterProviders().iterator();

        if(providers.hasNext()){
            return providers.next().getProvider();
        }

        throw new APIException("Encounter doesn't have a provider.");
    }

}
