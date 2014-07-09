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
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public abstract class DrugOrderMapperBaseImpl implements DrugOrderMapper{

    protected ConceptMapper conceptMapper;

    public DrugOrderMapperBaseImpl(ConceptMapper conceptMapper) {
        this.conceptMapper = conceptMapper;
    }

    @Override
    public EncounterTransaction.DrugOrder map(DrugOrder drugOrder) {
        EncounterTransaction.DrugOrder emrDrugOrder = new EncounterTransaction.DrugOrder();
        emrDrugOrder.setUuid(drugOrder.getUuid());
        emrDrugOrder.setConcept(conceptMapper.map(drugOrder.getConcept()));
        setDosageFrequency(drugOrder, emrDrugOrder);
        setDosageInstruction(drugOrder, emrDrugOrder);
        emrDrugOrder.setEndDate(drugOrder.getAutoExpireDate());
        emrDrugOrder.setNotes(drugOrder.getInstructions());
        emrDrugOrder.setPrn(drugOrder.getPrn());
        emrDrugOrder.setNumberPerDosage(drugOrder.getDose() == null ? 0 : drugOrder.getDose());
        emrDrugOrder.setStartDate(drugOrder.getStartDate());
        emrDrugOrder.setDateCreated(drugOrder.getDateCreated());
        emrDrugOrder.setDateChanged(drugOrder.getDateChanged());
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

    protected abstract void setDosageInstruction(DrugOrder drugOrder, EncounterTransaction.DrugOrder emrDrugOrder);

    protected abstract void setDosageFrequency(DrugOrder drugOrder, EncounterTransaction.DrugOrder emrDrugOrder);
}
