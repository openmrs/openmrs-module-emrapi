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
import org.openmrs.SimpleDosingInstructions;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Date;

public class DrugOrderMapper {

    public EncounterTransaction.DrugOrder map(DrugOrder openMRSDrugOrder) {
        EncounterTransaction.DrugOrder drugOrder = new EncounterTransaction.DrugOrder();
        drugOrder.setCareSetting(openMRSDrugOrder.getCareSetting().getName());
        drugOrder.setAction(openMRSDrugOrder.getAction().name());

        EncounterTransaction.Drug encounterTransactionDrug = new EncounterTransaction.Drug();
        encounterTransactionDrug.setName(openMRSDrugOrder.getDrug().getDisplayName());
        encounterTransactionDrug.setForm(openMRSDrugOrder.getDrug().getDosageForm().getName().getName());
        encounterTransactionDrug.setStrength(openMRSDrugOrder.getDrug().getStrength());
        encounterTransactionDrug.setUuid(openMRSDrugOrder.getDrug().getUuid());
        drugOrder.setDrug(encounterTransactionDrug);

        drugOrder.setDosingInstructionType(openMRSDrugOrder.getDosingType().getName());
        drugOrder.setDuration(openMRSDrugOrder.getDuration());
        drugOrder.setDurationUnits(openMRSDrugOrder.getDurationUnits().getName().getName());

        drugOrder.setScheduledDate(openMRSDrugOrder.getScheduledDate());
        drugOrder.setDateActivated(openMRSDrugOrder.getDateActivated());
        drugOrder.setEffectiveStartDate(openMRSDrugOrder.getEffectiveStartDate());
        if(drugOrder.getDosingInstructionType().equals(SimpleDosingInstructions.class.getName())) {
            Date autoExpireDate = openMRSDrugOrder.getDosingInstructionsInstance().getAutoExpireDate(openMRSDrugOrder);
            drugOrder.setAutoExpireDate(autoExpireDate);
            drugOrder.setEffectiveStopDate(openMRSDrugOrder.getDateStopped() != null ? openMRSDrugOrder.getDateStopped() : autoExpireDate);
        } else {
            drugOrder.setAutoExpireDate(openMRSDrugOrder.getAutoExpireDate());
            drugOrder.setEffectiveStopDate(openMRSDrugOrder.getEffectiveStopDate());
        }

        drugOrder.setDateStopped(openMRSDrugOrder.getDateStopped());

        EncounterTransaction.DosingInstructions dosingInstructions = new EncounterTransaction.DosingInstructions();
        dosingInstructions.setDose(openMRSDrugOrder.getDose());
        dosingInstructions.setDoseUnits(openMRSDrugOrder.getDoseUnits().getName().getName());
        dosingInstructions.setRoute(openMRSDrugOrder.getRoute().getName().getName());
        dosingInstructions.setAsNeeded(openMRSDrugOrder.getAsNeeded());
        dosingInstructions.setFrequency(openMRSDrugOrder.getFrequency().getName());
        dosingInstructions.setQuantity(openMRSDrugOrder.getQuantity().intValue());
        dosingInstructions.setQuantityUnits(openMRSDrugOrder.getQuantityUnits().getName().getName());
        dosingInstructions.setAdministrationInstructions(openMRSDrugOrder.getDosingInstructions());
        drugOrder.setDosingInstructions(dosingInstructions);

        drugOrder.setInstructions(openMRSDrugOrder.getInstructions());
        drugOrder.setCommentToFulfiller(openMRSDrugOrder.getCommentToFulfiller());

        return drugOrder;
    }
}