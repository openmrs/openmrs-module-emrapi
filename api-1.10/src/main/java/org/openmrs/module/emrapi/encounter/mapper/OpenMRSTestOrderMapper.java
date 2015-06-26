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
import org.openmrs.EncounterProvider;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.TestOrder;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.CareSettingType;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.utils.HibernateLazyLoader;

import java.util.Date;
import java.util.Iterator;

/**
 * OpenMRSTestOrderMapper.
 * Maps EncounterTransaction TestOrder to OpenMRS TestOrders.
 * <p/>
 * Version 1.0
 */
public class OpenMRSTestOrderMapper {
    private OrderService orderService;
    private ConceptService conceptService;

    public OpenMRSTestOrderMapper(OrderService orderService, ConceptService conceptService) {
        this.orderService = orderService;
        this.conceptService = conceptService;
    }

    public TestOrder map(EncounterTransaction.TestOrder testOrder, Encounter encounter) {

        TestOrder openMRSTestOrder = null;

        if (isNewTestOrder(testOrder)) {
            openMRSTestOrder = new TestOrder();
            openMRSTestOrder.setConcept(getConceptFrom(testOrder, openMRSTestOrder));
            openMRSTestOrder.setEncounter(encounter);
            openMRSTestOrder.setOrderer(getProviderForTestOrders(encounter));
            openMRSTestOrder.setCareSetting(orderService.getCareSettingByName(CareSettingType.OUTPATIENT.toString()));
            openMRSTestOrder.setAutoExpireDate(testOrder.getAutoExpireDate());
            openMRSTestOrder.setCommentToFulfiller(testOrder.getCommentToFulfiller());
        } else if (isRevisedTestOrder(testOrder)) {
            openMRSTestOrder = getOrderByUuid(testOrder.getPreviousOrderUuid()).cloneForRevision();
            openMRSTestOrder.setEncounter(encounter);
            openMRSTestOrder.setOrderer(getProviderForTestOrders(encounter));
            openMRSTestOrder.setAutoExpireDate(testOrder.getAutoExpireDate());
            openMRSTestOrder.setCommentToFulfiller(testOrder.getCommentToFulfiller());
        } else {
            openMRSTestOrder = getOrderByUuid(testOrder.getUuid());
            openMRSTestOrder.setDateChanged(new Date());
        }

        openMRSTestOrder.setVoided(testOrder.isVoided());
        openMRSTestOrder.setVoidReason(testOrder.getVoidReason());

        return openMRSTestOrder;
    }

    private boolean isRevisedTestOrder(EncounterTransaction.TestOrder testOrder) {
        return StringUtils.isBlank(testOrder.getUuid()) && StringUtils.isNotBlank(testOrder.getPreviousOrderUuid());
    }

    private TestOrder getOrderByUuid(String testOrderUuid) {
        Order order = orderService.getOrderByUuid(testOrderUuid);
        order = new HibernateLazyLoader().load(order);
        if (order == null || !(order instanceof TestOrder)) {
            throw new APIException("No test order with uuid : " + testOrderUuid);
        }
        return (TestOrder) order;
    }

    private Concept getConceptFrom(EncounterTransaction.TestOrder testOrder, TestOrder openMRSTestOrder) {
        if (!isNewTestOrder(testOrder)) {
            return openMRSTestOrder.getConcept();
        }

        EncounterTransaction.Concept concept = testOrder.getConcept();
        Concept conceptByUuid = conceptService.getConceptByUuid(concept.getUuid());
        if (conceptByUuid == null) {
            throw new APIException("No such Concept : " + testOrder.getConcept().getName());
        }
        return conceptByUuid;
    }


    private boolean isNewTestOrder(EncounterTransaction.TestOrder testOrder) {
        return StringUtils.isBlank(testOrder.getUuid()) && StringUtils.isBlank(testOrder.getPreviousOrderUuid());
    }

    private Provider getProviderForTestOrders(Encounter encounter) {
        Iterator<EncounterProvider> providers = encounter.getEncounterProviders().iterator();

        if (providers.hasNext()) {
            return providers.next().getProvider();
        }

        throw new APIException("Encounter doesn't have a provider.");
    }


}
