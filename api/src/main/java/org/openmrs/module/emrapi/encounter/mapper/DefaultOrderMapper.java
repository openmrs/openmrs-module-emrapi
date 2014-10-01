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

import java.util.List;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.TestOrder;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.emrapi.encounter.OrderMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.stereotype.Component;

@Component
@OpenmrsProfile(openmrsVersion = "1.9.*")
public class DefaultOrderMapper implements OrderMapper{
    @Override
    public List<EncounterTransaction.DrugOrder> mapDrugOrders(Encounter encounter) {
        return null;
    }

    @Override
    public List<EncounterTransaction.TestOrder> mapTestOrders(Encounter encounter) {
        return null;
    }

    @Override
    public EncounterTransaction.DrugOrder mapDrugOrder(DrugOrder openMRSDrugOrder) {
        return null;
    }

    @Override
    public EncounterTransaction.TestOrder mapTestOrder(TestOrder order) {
        return null;
    }
}
