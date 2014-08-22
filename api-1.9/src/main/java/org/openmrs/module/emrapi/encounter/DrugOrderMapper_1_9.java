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
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component (value = "drugOrderMapper")
public class DrugOrderMapper_1_9 extends DrugOrderMapperBaseImpl implements DrugOrderMapper {
    private ConceptService conceptService;

    @Autowired
    public DrugOrderMapper_1_9(ConceptService conceptService) {
        this(new ConceptMapper(), conceptService);
    }

    public DrugOrderMapper_1_9(ConceptMapper conceptMapper, ConceptService conceptService) {
        super(conceptMapper);
        this.conceptService = conceptService;
    }

    @Override
    protected void mapVersionSpecificFields(DrugOrder drugOrder, EncounterTransaction.DrugOrder emrDrugOrder) {
        emrDrugOrder.setPrn(drugOrder.getPrn());
        emrDrugOrder.setDosageInstruction(conceptMapper.map(conceptService.getConceptByUuid(drugOrder.getUnits())));
        emrDrugOrder.setStartDate(drugOrder.getStartDate());
        emrDrugOrder.setDosageFrequency(conceptMapper.map(conceptService.getConceptByUuid(drugOrder.getFrequency())));
    }
}