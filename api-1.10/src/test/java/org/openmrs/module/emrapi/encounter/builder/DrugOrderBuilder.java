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
package org.openmrs.module.emrapi.encounter.builder;

import java.util.Calendar;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public class DrugOrderBuilder {

    public static EncounterTransaction.DrugOrder sample(String drugUuid, String durationUnits) {
        EncounterTransaction.DrugOrder drugOrder = new EncounterTransaction.DrugOrder();
        drugOrder.setCareSetting("OUTPATIENT");
        EncounterTransaction.Drug drug = new EncounterTransaction.Drug();
        drug.setUuid(drugUuid);
        drugOrder.setDrug(drug);
        drugOrder.setDosingInstructionType("org.openmrs.SimpleDosingInstructions");
        drugOrder.setOrderType("Drug Order");
        EncounterTransaction.DosingInstructions dosingInstructions = DosingInstructionsBuilder.sample();
        drugOrder.setDosingInstructions(dosingInstructions);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        drugOrder.setScheduledDate(calendar.getTime());
        calendar.add(Calendar.MONTH, 1);
        drugOrder.setEndDate(calendar.getTime());
        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setUuid("331c6bf8-7846-11e3-a96a-0800271c1b75");
        drugOrder.setAction("NEW");
        drugOrder.setDuration(2);
        drugOrder.setDurationUnits(durationUnits);
        return drugOrder;
    }
}
