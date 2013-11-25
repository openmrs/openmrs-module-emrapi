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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Obs;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.test.builder.ObsBuilder;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ObservationMapperTest {

    @Mock
    private Concept concept;
    private ObservationMapper observationMapper;

    @Before
    public void setUp(){
        initMocks(this);
        when(concept.getName()).thenReturn(new ConceptName());
        observationMapper = new ObservationMapper();
    }

    @Test
    public void shouldMapObservation(){
        ObsBuilder obsBuilder = new ObsBuilder();
        String uuid = UUID.randomUUID().toString();
        obsBuilder.setUuid(uuid).setValue(100.0).setConcept(concept);
        when(concept.isNumeric()).thenReturn(true);
        Obs obs = obsBuilder.get();

        EncounterTransaction.Observation observation = observationMapper.map(obs);

        assertEquals(uuid, observation.getUuid());
        assertEquals(100.0, observation.getValue());
    }
}
