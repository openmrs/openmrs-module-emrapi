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

import org.openmrs.DrugOrder;
import org.openmrs.OrderFrequency;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.stereotype.Component;

@Component(value = "drugOrderMapper")
@OpenmrsProfile(openmrsVersion = "1.10")
public class DrugOrderMapper_1_10 extends DrugOrderMapperBaseImpl implements DrugOrderMapper {

    public DrugOrderMapper_1_10() {
        super(new ConceptMapper());
    }

    @Override
    protected void mapVersionSpecificFields(DrugOrder drugOrder, EncounterTransaction.DrugOrder emrDrugOrder) {
        emrDrugOrder.setPrn(drugOrder.getAsNeeded());
        emrDrugOrder.setDosageInstruction(conceptMapper.map(drugOrder.getDoseUnits()));
        OrderFrequency frequency = drugOrder.getFrequency();
        if (frequency != null) {
            emrDrugOrder.setDosageFrequency(conceptMapper.map(frequency.getConcept()));
        }
        emrDrugOrder.setDosingInstructions(drugOrder.getDosingInstructions());
        emrDrugOrder.setStartDate(drugOrder.getDateActivated());

    }
}