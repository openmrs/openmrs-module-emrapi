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
package org.openmrs.module.emrapi.encounter;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.List;

public class EncounterDrugOrderServiceHelper {

    private ConceptService conceptService;
    private OrderService orderService;

    public EncounterDrugOrderServiceHelper() {
    }

    public EncounterDrugOrderServiceHelper(ConceptService conceptService, OrderService orderService) {
        this.conceptService = conceptService;
        this.orderService = orderService;
    }

    public void update(Encounter encounter, List<EncounterTransaction.DrugOrder> drugOrders) {
        OrderType drugOrderType = getDrugOrderType();
        List<Concept> cachedConcepts = new ArrayList<Concept>();


        for (final EncounterTransaction.DrugOrder drug : drugOrders) {
            Order order = getMatchingOrder(encounter, drug.getUuid());
            if (order == null){
                order = new DrugOrder();
                order.setEncounter(encounter);
                order.setPatient(encounter.getPatient());
                encounter.addOrder(order);
            }

            order.setInstructions(drug.getNotes());
            order.setStartDate(drug.getStartDate());
            order.setAutoExpireDate(drug.getEndDate());

            Concept drugConcept = conceptService.getConceptByUuid(drug.getConceptUuid());
            order.setConcept(drugConcept);
            order.setOrderType(drugOrderType);

            if (order instanceof DrugOrder) {
                DrugOrder drugOrder =  (DrugOrder) order;
                drugOrder.setDrug(findDrug(drug.getUuid()));
                drugOrder.setDose(Double.valueOf(drug.getNumberPerDosage()));

                if (!StringUtils.isBlank(drug.getDosageFrequencyUuid())) {
                    Concept frequencyConcept = findConcept(cachedConcepts, drug.getDosageFrequencyUuid());
                    drugOrder.setFrequency(frequencyConcept.getConceptId().toString());
                }

                if (!StringUtils.isBlank(drug.getDosageInstructionUuid())) {
                    Concept instructionConcept = findConcept(cachedConcepts, drug.getDosageInstructionUuid());
                    drugOrder.setUnits(instructionConcept.getConceptId().toString());
                }

                drugOrder.setPrn(drug.isPrn());
                drugOrder.setComplex(false);
            }
        }
    }

    private Drug findDrug(String uuid) {
        return conceptService.getDrugByUuid(uuid);
    }

    private Concept findConcept(List<Concept> cachedConcepts, String conceptUuid) {
        Concept fetchedConcept = null;
        for (Concept concept : cachedConcepts) {
            if (concept.getUuid().equals(conceptUuid)) {
                fetchedConcept = concept;
                break;
            }
        }

        if (fetchedConcept != null) {
            return fetchedConcept;
        }

        fetchedConcept = conceptService.getConceptByUuid(conceptUuid);
        cachedConcepts.add(fetchedConcept);
        return fetchedConcept;
    }

    private OrderType getDrugOrderType() {
        List<OrderType> allOrderTypes = orderService.getAllOrderTypes();
        for (OrderType type : allOrderTypes) {
            if (type.getName().toLowerCase().equals("drug order")) {
                return type;
            }
        }
        return null;
    }

    private Order getMatchingOrder(Encounter encounter, String uuid) {
        for (Order o : encounter.getOrders()) {
            if (StringUtils.equals(o.getUuid(), uuid)) {
                return o;
            }
        }
        return null;
    }


}