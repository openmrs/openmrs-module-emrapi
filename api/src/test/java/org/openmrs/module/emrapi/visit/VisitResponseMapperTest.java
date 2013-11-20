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
package org.openmrs.module.emrapi.visit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.visit.contract.VisitResponse;

import static org.mockito.MockitoAnnotations.initMocks;

public class VisitResponseMapperTest {

    @Mock
    private EncounterTransactionMapper encounterTransactionMapper;

    @Before
    public void setUp(){
        initMocks(this);
    }

    @Test
    public void testMap() throws Exception {
        Visit visit = new Visit();
        visit.addEncounter(new Encounter());

        VisitResponse visitResponse = new VisitResponseMapper(encounterTransactionMapper).map(visit);

        Assert.assertEquals(visit.getUuid(), visitResponse.getVisitUuid());
        Assert.assertEquals(visit.getEncounters().size(), visitResponse.getEncounters().size());
    }
}
