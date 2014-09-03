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
import org.openmrs.CareSetting;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class OpenMRSDrugOrderMapper {

    private OrderService orderService;
    private ConceptService conceptService;
    private DosingInstructionsMapper dosingInstructionsMapper;

    @Autowired
    public OpenMRSDrugOrderMapper(OrderService orderService, ConceptService conceptService,
                                  DosingInstructionsMapper dosingInstructionsMapper) {
        this.orderService = orderService;
        this.conceptService = conceptService;
        this.dosingInstructionsMapper = dosingInstructionsMapper;
    }

    public DrugOrder map(EncounterTransaction.DrugOrder drugOrder, Encounter encounter) {
        DrugOrder openMRSDrugOrder = createDrugOrder(drugOrder);
        openMRSDrugOrder.setCareSetting(getCareSettingFrom(drugOrder, openMRSDrugOrder));
        openMRSDrugOrder.setDrug(getDrugFrom(drugOrder, openMRSDrugOrder));
        openMRSDrugOrder.setConcept(openMRSDrugOrder.getDrug().getConcept());
        openMRSDrugOrder.setEncounter(encounter);
        openMRSDrugOrder.setOrderType(orderService.getOrderTypeByName(drugOrder.getOrderType()));
        openMRSDrugOrder.setDateActivated(drugOrder.getDateActivated());
        if (drugOrder.getScheduledDate() != null && drugOrder.getScheduledDate().after(new Date())) {
            openMRSDrugOrder.setScheduledDate(drugOrder.getScheduledDate());
            openMRSDrugOrder.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
        }
        openMRSDrugOrder.setDuration(drugOrder.getDuration());
        openMRSDrugOrder.setDurationUnits(conceptService.getConceptByName(drugOrder.getDurationUnits()));

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

    private boolean isDrugOrderDiscontinued(EncounterTransaction.DrugOrder drugOrder) {
        return drugOrder.getAction() != null && Order.Action.valueOf(drugOrder.getAction()) == Order.Action.DISCONTINUE;
    }

    private DrugOrder createDrugOrder(EncounterTransaction.DrugOrder drugOrder) {
        if (isNewDrugOrder(drugOrder)) {
            return new DrugOrder();
        } else if (isDrugOrderDiscontinued(drugOrder)) {
            return (DrugOrder) orderService.getOrderByUuid(drugOrder.getPreviousOrderUuid()).cloneForDiscontinuing();
        } else {
            return (DrugOrder) orderService.getOrderByUuid(drugOrder.getPreviousOrderUuid()).cloneForRevision();
        }
    }

    private CareSetting getCareSettingFrom(EncounterTransaction.DrugOrder drugOrder, DrugOrder openMRSDrugOrder) {
        if (!isNewDrugOrder(drugOrder)) {
            return openMRSDrugOrder.getCareSetting();
        }
        return orderService.getCareSettingByName(drugOrder.getCareSetting());
    }

    private Drug getDrugFrom(EncounterTransaction.DrugOrder drugOrder, DrugOrder openMRSDrugOrder) {
        if (!isNewDrugOrder(drugOrder)) {
            return openMRSDrugOrder.getDrug();
        }
        EncounterTransaction.Drug drug = drugOrder.getDrug();
        if (drug.getUuid() == null || drug.getUuid().isEmpty()) {
            return conceptService.getDrugByNameOrId(drug.getName());
        }
        return conceptService.getDrugByNameOrId(drugOrder.getDrug().getUuid());
    }
}