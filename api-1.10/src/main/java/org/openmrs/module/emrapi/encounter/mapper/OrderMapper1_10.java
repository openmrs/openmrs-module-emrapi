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

import org.apache.commons.lang3.*;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.TestOrder;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.db.hibernate.HibernateUtil;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.OrderMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component(value = "orderMapper")
@OpenmrsProfile(openmrsVersion = "1.10")
public class OrderMapper1_10 implements OrderMapper {

    private final ConceptMapper conceptMapper = new ConceptMapper();

    @Override
    public List<EncounterTransaction.DrugOrder> mapDrugOrders(Encounter encounter) {

        List<EncounterTransaction.DrugOrder> orders = new ArrayList<EncounterTransaction.DrugOrder>();
        for (Order order : encounter.getOrders()) {
            order = HibernateUtil.getRealObjectFromProxy(order);
            if (DrugOrder.class.equals(order.getClass())) {
                orders.add(mapDrugOrder((DrugOrder) order));
            }
        }
        return orders;
    }

    @Override
    public List<EncounterTransaction.TestOrder> mapTestOrders(Encounter encounter) {
        List<EncounterTransaction.TestOrder> testOrders = new ArrayList<EncounterTransaction.TestOrder>();
        for (Order order : encounter.getOrders()) {
            order = HibernateUtil.getRealObjectFromProxy(order);
            if (TestOrder.class.equals(order.getClass())) {
                testOrders.add(mapTestOrder((TestOrder) order));
            }
        }
        return testOrders;
    }

    @Override
    public EncounterTransaction.DrugOrder mapDrugOrder(DrugOrder openMRSDrugOrder) {
        EncounterTransaction.DrugOrder drugOrder = new EncounterTransaction.DrugOrder();
        drugOrder.setUuid(openMRSDrugOrder.getUuid());
        if (openMRSDrugOrder.getCareSetting() != null) {
            drugOrder.setCareSetting(openMRSDrugOrder.getCareSetting().getName());
        }
        drugOrder.setAction(openMRSDrugOrder.getAction().name());
        drugOrder.setOrderType(openMRSDrugOrder.getOrderType().getName());

        Order previousOrder = openMRSDrugOrder.getPreviousOrder();
        if (previousOrder != null && StringUtils.isNotBlank(previousOrder.getUuid())){
            drugOrder.setPreviousOrderUuid(previousOrder.getUuid());
        }

        EncounterTransaction.Drug encounterTransactionDrug = new DrugMapper().map(openMRSDrugOrder.getDrug());
        drugOrder.setDrug(encounterTransactionDrug);

        drugOrder.setDosingInstructionType(openMRSDrugOrder.getDosingType().getName());
        drugOrder.setDuration(openMRSDrugOrder.getDuration());
        drugOrder.setDurationUnits(getConceptName(openMRSDrugOrder.getDurationUnits()));

        drugOrder.setScheduledDate(openMRSDrugOrder.getScheduledDate());
        drugOrder.setDateActivated(openMRSDrugOrder.getDateActivated());
        drugOrder.setEffectiveStartDate(openMRSDrugOrder.getEffectiveStartDate());
        drugOrder.setAutoExpireDate(openMRSDrugOrder.getAutoExpireDate());
        drugOrder.setEffectiveStopDate(openMRSDrugOrder.getEffectiveStopDate());

        drugOrder.setDateStopped(openMRSDrugOrder.getDateStopped());

        EncounterTransaction.DosingInstructions dosingInstructions = new EncounterTransaction.DosingInstructions();
        dosingInstructions.setDose(openMRSDrugOrder.getDose());
        dosingInstructions.setDoseUnits(getConceptName(openMRSDrugOrder.getDoseUnits()));
        dosingInstructions.setRoute(getConceptName(openMRSDrugOrder.getRoute()));
        dosingInstructions.setAsNeeded(openMRSDrugOrder.getAsNeeded());
        if(openMRSDrugOrder.getFrequency() != null) {
            dosingInstructions.setFrequency(openMRSDrugOrder.getFrequency().getName());
        }
        if (openMRSDrugOrder.getQuantity() != null) {
            dosingInstructions.setQuantity(openMRSDrugOrder.getQuantity());
        }
        dosingInstructions.setQuantityUnits(getConceptName(openMRSDrugOrder.getQuantityUnits()));
        dosingInstructions.setAdministrationInstructions(openMRSDrugOrder.getDosingInstructions());
        drugOrder.setDosingInstructions(dosingInstructions);

        drugOrder.setInstructions(openMRSDrugOrder.getInstructions());
        drugOrder.setCommentToFulfiller(openMRSDrugOrder.getCommentToFulfiller());

        drugOrder.setVoided(openMRSDrugOrder.getVoided());
        drugOrder.setVoidReason(openMRSDrugOrder.getVoidReason());

        return drugOrder;
    }

    @Override
    public EncounterTransaction.TestOrder mapTestOrder(TestOrder order) {
        EncounterTransaction.TestOrder emrTestOrder = new EncounterTransaction.TestOrder();
        emrTestOrder.setUuid(order.getUuid());
        emrTestOrder.setConcept(conceptMapper.map(order.getConcept()));
        emrTestOrder.setInstructions(order.getInstructions());
        emrTestOrder.setOrderTypeUuid(order.getOrderType() != null ? order.getOrderType().getUuid() : null);
        emrTestOrder.setVoided(order.getVoided());
        emrTestOrder.setVoidReason(order.getVoidReason());
        emrTestOrder.setDateCreated(order.getDateCreated());
        emrTestOrder.setDateChanged(order.getDateChanged());
        return emrTestOrder;
    }

    private String getConceptName(Concept concept) {
        if (concept != null) {
            return concept.getName().getName();
        }
        return null;
    }
}