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

import java.util.LinkedHashSet;
import java.util.List;

import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.EncounterService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.mapper.OpenMRSDrugOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service(value = "emrOrderService")
@OpenmrsProfile(openmrsVersion = "1.11")
public class EmrOrderServiceImpl_1_11 implements EmrOrderService {
    private final OpenMRSDrugOrderMapper openMRSDrugOrderMapper;
    private final EncounterService encounterService;

    @Autowired
    public EmrOrderServiceImpl_1_11(OpenMRSDrugOrderMapper openMRSDrugOrderMapper, EncounterService encounterService) {
        this.openMRSDrugOrderMapper = openMRSDrugOrderMapper;
        this.encounterService = encounterService;
    }

    @Override
    public void save(List<EncounterTransaction.DrugOrder> drugOrders, Encounter encounter) {
        encounter.setOrders(new LinkedHashSet<Order>(encounter.getOrders()));
        for (EncounterTransaction.DrugOrder drugOrder : drugOrders) {
            DrugOrder omrsDrugOrder = openMRSDrugOrderMapper.map(drugOrder, encounter);
            encounter.addOrder(omrsDrugOrder);
        }
        encounterService.saveEncounter(encounter);
    }
}
