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
import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.CareSettingType;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

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
            openMRSTestOrder.setDateCreated(testOrder.getDateCreated());
        }
        else{
            openMRSTestOrder = getOrderByUuid(testOrder);
            openMRSTestOrder.setDateChanged(new Date());
        }

        openMRSTestOrder.setVoided(testOrder.isVoided());
        openMRSTestOrder.setVoidReason(testOrder.getVoidReason());

        return  openMRSTestOrder;
    }

    private TestOrder getOrderByUuid(EncounterTransaction.TestOrder testOrder){
        TestOrder openMRSTestOrder = (TestOrder) orderService.getOrderByUuid(testOrder.getUuid());
        if(openMRSTestOrder == null) {
            throw new APIException("No test order with uuid : " + testOrder.getUuid());
        }
        return openMRSTestOrder;
    }

    private Concept getConceptFrom(EncounterTransaction.TestOrder testOrder, TestOrder openMRSTestOrder) {
        if (!isNewTestOrder(testOrder)) {
            return openMRSTestOrder.getConcept();
        }

        EncounterTransaction.Concept concept = testOrder.getConcept();
        Concept conceptByUuid = conceptService.getConceptByUuid(concept.getUuid());
        if(conceptByUuid == null){
            throw new APIException("No such Concept : " + testOrder.getConcept().getName());
        }
        return conceptByUuid;
    }


    private boolean isNewTestOrder(EncounterTransaction.TestOrder testOrder) {
        return StringUtils.isBlank(testOrder.getUuid());
    }

    private Provider getProviderForTestOrders(Encounter encounter){
        Iterator<EncounterProvider> providers = encounter.getEncounterProviders().iterator();

        if(providers.hasNext()){
            return providers.next().getProvider();
        }

        throw new APIException("Encounter doesn't have atleast a single provider.");
    }


}
