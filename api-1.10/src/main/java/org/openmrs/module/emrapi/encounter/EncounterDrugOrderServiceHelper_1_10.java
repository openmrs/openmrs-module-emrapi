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
import org.openmrs.SimpleDosingInstructions;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderFrequency;
import org.openmrs.OrderType;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.InvalidDrugException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//TODO: This needs to be reworked to really use features of the new OrderEntry API
//TODO: Mihir: This class needs tests
@Component (value = "encounterDrugOrderServiceHelper")
@OpenmrsProfile(openmrsVersion = "1.10")
public class EncounterDrugOrderServiceHelper_1_10 implements EncounterDrugOrderServiceHelper{

    private ConceptService conceptService;
    private OrderService orderService;

    public EncounterDrugOrderServiceHelper_1_10() {
    }

    @Autowired
    public EncounterDrugOrderServiceHelper_1_10(ConceptService conceptService, OrderService orderService) {
        this.conceptService = conceptService;
        this.orderService = orderService;
    }

    @Override
    public void update(Encounter encounter, List<EncounterTransaction.DrugOrder> drugOrders) {
        OrderType drugOrderType = getDrugOrderType();
        List<Concept> cachedConcepts = new ArrayList<Concept>();


        for (final EncounterTransaction.DrugOrder drug : drugOrders) {
            if (drug.getUuid() == null) {
                throw new InvalidDrugException("Drug does not exist");
            }

            Order order = getMatchingOrder(encounter, drug.getUuid());
            if (order == null) {
                order = new DrugOrder();
                order.setEncounter(encounter);
                order.setPatient(encounter.getPatient());
                encounter.addOrder(order);
            }

            order.setInstructions(drug.getNotes());
            order.setDateActivated(drug.getStartDate());
            order.setAutoExpireDate(drug.getEndDate());
            order.setVoided(drug.isVoided());
            order.setVoidReason(drug.getVoidReason());

            Concept drugConcept = conceptService.getConceptByUuid(drug.getConceptUuid());
            order.setConcept(drugConcept);
            order.setOrderType(drugOrderType);

            if (order instanceof DrugOrder) {
                DrugOrder drugOrder = (DrugOrder) order;
                drugOrder.setDrug(findDrug(drug.getUuid()));
                drugOrder.setDose(Double.valueOf(drug.getNumberPerDosage()));
                drugOrder.setAsNeeded(drug.isPrn());

                if (!StringUtils.isBlank(drug.getDosageFrequencyUuid())) {
                    Concept frequencyConcept = findConcept(cachedConcepts, drug.getDosageFrequencyUuid());
                    if (drug.getDosageFrequencyUuid() == null && !drug.isPrn()) {
                        throw new InvalidDrugException("Dosage Frequency does not exist.");
                    }
                    if (frequencyConcept != null) {
                        drugOrder.setFrequency(getOrderFrequency(drug));
                    }
                }

                if (!StringUtils.isBlank(drug.getDosageInstructionUuid())) {
                    Concept instructionConcept = findConcept(cachedConcepts, drug.getDosageInstructionUuid());
                    if (instructionConcept != null) {
                        drugOrder.setDoseUnits(conceptService.getConceptByUuid(drug.getDosageInstructionUuid()));
                    }
                }
                drugOrder.setDosingType(SimpleDosingInstructions.class);
            }
        }
    }

    public OrderFrequency getOrderFrequency(EncounterTransaction.DrugOrder drug) {
        return orderService.getOrderFrequencyByUuid(drug.getDosageFrequencyUuid());
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
        List<OrderType> allOrderTypes = orderService.getOrderTypes(true);
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