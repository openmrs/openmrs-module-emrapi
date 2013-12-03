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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.DrugOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.builder.DrugOrderBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import static org.mockito.MockitoAnnotations.initMocks;

public class DrugOrderMapperTest {
    @Mock
    private ConceptService conceptService;
    private DrugOrderMapper drugOrderMapper;

    @Before
    public void setUp(){
        initMocks(this);
        drugOrderMapper = new DrugOrderMapper(conceptService);
    }

    @Test
    public void shouldNotFailWhenDosageFormIsNull() {
        DrugOrder drugOrder = new DrugOrderBuilder().build();
        drugOrder.getDrug().setDosageForm(null);

        EncounterTransaction.DrugOrder encounterTransactionDrugOrder = drugOrderMapper.map(drugOrder);

        Assert.assertNotNull(encounterTransactionDrugOrder);
    }
}
