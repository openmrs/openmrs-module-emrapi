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

import org.openmrs.DrugOrder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public class DrugOrderMapper {

    public EncounterTransaction.DrugOrder map(DrugOrder drugOrder) {
        EncounterTransaction.DrugOrder encounterTransactionDrugOrder = new EncounterTransaction.DrugOrder();
        encounterTransactionDrugOrder.setCareSetting(drugOrder.getCareSetting().getName());
        encounterTransactionDrugOrder.setAction(drugOrder.getAction().name());

        EncounterTransaction.Drug encounterTransactionDrug = new EncounterTransaction.Drug();
        encounterTransactionDrug.setName(drugOrder.getDrug().getDisplayName());
        encounterTransactionDrug.setForm(drugOrder.getDrug().getDosageForm().getName().getName());
        encounterTransactionDrug.setStrength(drugOrder.getDrug().getStrength());
        encounterTransactionDrug.setUuid(drugOrder.getDrug().getUuid());
        encounterTransactionDrugOrder.setDrug(encounterTransactionDrug);

        encounterTransactionDrugOrder.setDosingInstructionType(drugOrder.getDosingType().getName());
        encounterTransactionDrugOrder.setDuration(drugOrder.getDuration());
        encounterTransactionDrugOrder.setDurationUnits(drugOrder.getDurationUnits().getName().getName());
        encounterTransactionDrugOrder.setScheduledDate(drugOrder.getScheduledDate());
        encounterTransactionDrugOrder.setDateActivated(drugOrder.getDateActivated());

        EncounterTransaction.DosingInstructions dosingInstructions = new EncounterTransaction.DosingInstructions();
        dosingInstructions.setDose(drugOrder.getDose());
        dosingInstructions.setDoseUnits(drugOrder.getDoseUnits().getName().getName());
        dosingInstructions.setRoute(drugOrder.getRoute().getName().getName());
        dosingInstructions.setAsNeeded(drugOrder.getAsNeeded());
        dosingInstructions.setFrequency(drugOrder.getFrequency().getName());
        dosingInstructions.setQuantity(drugOrder.getQuantity().intValue());
        dosingInstructions.setQuantityUnits(drugOrder.getQuantityUnits().getName().getName());
        dosingInstructions.setAdministrationInstructions(drugOrder.getDosingInstructions());
        encounterTransactionDrugOrder.setDosingInstructions(dosingInstructions);

        encounterTransactionDrugOrder.setInstructions(drugOrder.getInstructions());
        encounterTransactionDrugOrder.setCommentToFulfiller(drugOrder.getCommentToFulfiller());

        return encounterTransactionDrugOrder;
    }
}