/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p/>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p/>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.emrapi.encounter.mapper;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.DosingInstructions;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.service.OrderMetadataService;

/**
 * OpenMRSDrugOrderMapper.
 * Maps EncounterTransaction DrugOrder to OpenMRS DrugOrders.
 * <p/>
 * Version 1.0
 */
public class OpenMRSDrugOrderMapper {

    private OrderService orderService;
    private ConceptService conceptService;
    private DosingInstructionsMapper dosingInstructionsMapper;
    private OrderMetadataService orderMetadataService;
    public static final Order.Urgency DEFAULT_URGENCY = Order.Urgency.ROUTINE;

    public OpenMRSDrugOrderMapper(OrderService orderService, ConceptService conceptService,
                                  DosingInstructionsMapper dosingInstructionsMapper, OrderMetadataService orderMetadataService) {
        this.orderService = orderService;
        this.conceptService = conceptService;
        this.dosingInstructionsMapper = dosingInstructionsMapper;
        this.orderMetadataService = orderMetadataService;
    }

    public DrugOrder map(EncounterTransaction.DrugOrder drugOrder, Encounter encounter) {
        Concept conceptByUuid;
        DrugOrder openMRSDrugOrder = createDrugOrder(drugOrder);
        openMRSDrugOrder.setCareSetting(getCareSettingFrom(drugOrder, openMRSDrugOrder));

        Drug drug = getDrugFrom(drugOrder, openMRSDrugOrder);
        openMRSDrugOrder.setDrug(drug);
        openMRSDrugOrder.setDrugNonCoded(drugOrder.getDrugNonCoded());
        Concept concept = getConceptFrom(drugOrder, openMRSDrugOrder);
        if (concept != null) {
            openMRSDrugOrder.setConcept(concept);
        }
        openMRSDrugOrder.setEncounter(encounter);

        openMRSDrugOrder.setDateActivated(drugOrder.getDateActivated());
        openMRSDrugOrder.setScheduledDate(drugOrder.getScheduledDate());
        openMRSDrugOrder.setUrgency(drugOrder.getScheduledDate() != null ? Order.Urgency.ON_SCHEDULED_DATE : DEFAULT_URGENCY);
        openMRSDrugOrder.setDuration(drugOrder.getDuration());
        openMRSDrugOrder.setSortWeight(drugOrder.getSortWeight());
        openMRSDrugOrder.setDurationUnits(orderMetadataService.getDurationUnitsConceptByName(drugOrder.getDurationUnits()));
        openMRSDrugOrder.setAutoExpireDate(drugOrder.getAutoExpireDate());
        if(drugOrder.getOrderReasonConcept() != null) {
            conceptByUuid = conceptService.getConceptByUuid(drugOrder.getOrderReasonConcept().getUuid());
            openMRSDrugOrder.setOrderReason(conceptByUuid);
        }
        openMRSDrugOrder.setOrderReasonNonCoded(drugOrder.getOrderReasonText());

        try {
            if (drugOrder.getDosingInstructionType() != null) {
                openMRSDrugOrder.setDosingType((Class<? extends DosingInstructions>) Context.loadClass(drugOrder.getDosingInstructionType()));
            }
        } catch (ClassNotFoundException e) {
            throw new APIException("Class not found for : DosingInstructionType " + drugOrder.getDosingInstructionType(), e);
        }

        dosingInstructionsMapper.map(drugOrder.getDosingInstructions(), openMRSDrugOrder);
        openMRSDrugOrder.setInstructions(drugOrder.getInstructions());
        Provider provider = encounter.getEncounterProviders().iterator().next().getProvider();
        openMRSDrugOrder.setOrderer(provider);
        return openMRSDrugOrder;
    }

    private boolean isNewDrugOrder(EncounterTransaction.DrugOrder drugOrder) {
        return StringUtils.isBlank(drugOrder.getPreviousOrderUuid());
    }

    private boolean isDiscontinuationDrugOrder(EncounterTransaction.DrugOrder drugOrder) {
        return drugOrder.getAction() != null && Order.Action.valueOf(drugOrder.getAction()) == Order.Action.DISCONTINUE;
    }

    private DrugOrder createDrugOrder(EncounterTransaction.DrugOrder drugOrder) {
        if (isNewDrugOrder(drugOrder)) {
            return new DrugOrder();
        } else if (isDiscontinuationDrugOrder(drugOrder)) {
            return (DrugOrder) orderService.getOrderByUuid(drugOrder.getPreviousOrderUuid()).cloneForDiscontinuing();
        } else {
            return (DrugOrder) orderService.getOrderByUuid(drugOrder.getPreviousOrderUuid()).cloneForRevision();
        }
    }

    private CareSetting getCareSettingFrom(EncounterTransaction.DrugOrder drugOrder, DrugOrder openMRSDrugOrder) {
        if (!isNewDrugOrder(drugOrder)) {
            return openMRSDrugOrder.getCareSetting();
        }
        return orderService.getCareSettingByName(drugOrder.getCareSetting().toString());
    }

    private Drug getDrugFrom(EncounterTransaction.DrugOrder drugOrder, DrugOrder openMRSDrugOrder) {
        if (!isNewDrugOrder(drugOrder)) {
            return openMRSDrugOrder.getDrug();
        }
        EncounterTransaction.Drug drug = drugOrder.getDrug();
        if (drug == null) {
            return null;
        }
        if (drug.getUuid() == null || drug.getUuid().isEmpty()) {
            return conceptService.getDrug(drug.getName());
        }
        return conceptService.getDrugByUuid(drug.getUuid());
    }

    private Concept getConceptFrom(EncounterTransaction.DrugOrder drugOrder, DrugOrder openMRSDrugOrder) {
        if (!isNewDrugOrder(drugOrder)) {
            return openMRSDrugOrder.getConcept();
        }

        Concept conceptByUuid;
        EncounterTransaction.Concept concept = drugOrder.getConcept();
        if (concept == null) {
            conceptByUuid = null;
        }
        else {
            conceptByUuid = conceptService.getConceptByUuid(concept.getUuid());
            if (conceptByUuid == null) {
                throw new APIException("No such Concept : " + drugOrder.getConcept().getName());
            }
        }
        return conceptByUuid;
    }
}