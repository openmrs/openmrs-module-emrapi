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

import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.OrderFrequency;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DosingInstructionsMapper {
    private ConceptService conceptService;
    private OrderService orderService;

    @Autowired
    public DosingInstructionsMapper(ConceptService conceptService, OrderService orderService) {
        this.conceptService = conceptService;
        this.orderService = orderService;
    }

    public DrugOrder map(EncounterTransaction.DosingInstructions dosingInstructions, DrugOrder drugOrder) {
        drugOrder.setDose(dosingInstructions.getDose());
        drugOrder.setDoseUnits(conceptByName(dosingInstructions.getDoseUnits()));
        drugOrder.setDosingInstructions(dosingInstructions.getAdministrationInstructions());
        drugOrder.setRoute(conceptByName(dosingInstructions.getRoute()));
        drugOrder.setAsNeeded(dosingInstructions.getAsNeeded());
        drugOrder.setFrequency(orderFrequency(conceptByName(dosingInstructions.getFrequency())));
        drugOrder.setQuantity(Double.valueOf(dosingInstructions.getQuantity()));
        drugOrder.setQuantityUnits(conceptByName(dosingInstructions.getQuantityUnits()));
        Integer numberOfRefills = dosingInstructions.getNumberOfRefills();
        drugOrder.setNumRefills(numberOfRefills == null? 0: numberOfRefills);
        return drugOrder;
    }

    private OrderFrequency orderFrequency(Concept frequencyConcept) {
        return orderService.getOrderFrequencyByConcept(frequencyConcept);
    }

    private Concept conceptByName(String name) {
        return conceptService.getConceptByName(name);
    }
}
