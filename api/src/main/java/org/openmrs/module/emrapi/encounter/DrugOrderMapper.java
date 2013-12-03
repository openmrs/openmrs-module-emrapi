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

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public class DrugOrderMapper {
    private final ConceptMapper conceptMapper = new ConceptMapper();
    private ConceptService conceptService;

    public DrugOrderMapper(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public EncounterTransaction.DrugOrder map(DrugOrder drugOrder) {
        EncounterTransaction.DrugOrder emrDrugOrder = new EncounterTransaction.DrugOrder();
        emrDrugOrder.setUuid(drugOrder.getUuid());
        emrDrugOrder.setConcept(conceptMapper.map(drugOrder.getConcept()));
        emrDrugOrder.setDosageFrequency(conceptMapper.map(conceptService.getConceptByUuid(drugOrder.getFrequency())));
        emrDrugOrder.setDosageInstruction(conceptMapper.map(conceptService.getConceptByUuid(drugOrder.getUnits())));
        emrDrugOrder.setEndDate(drugOrder.getAutoExpireDate());
        emrDrugOrder.setNotes(drugOrder.getInstructions());
        emrDrugOrder.setPrn(drugOrder.getPrn());
        emrDrugOrder.setNumberPerDosage(drugOrder.getDose() == null ? 0 : drugOrder.getDose().intValue());
        emrDrugOrder.setStartDate(drugOrder.getStartDate());
        Drug drug = drugOrder.getDrug();
        emrDrugOrder.setDoseStrength(drug.getDoseStrength());
        Concept dosageForm = drug.getDosageForm();
        if (dosageForm != null) {
            emrDrugOrder.setDosageForm(dosageForm.getName().getName());
        }
        emrDrugOrder.setDrugName(drug.getName());
        emrDrugOrder.setDrugUnits(drug.getUnits());
        return emrDrugOrder;
    }
}