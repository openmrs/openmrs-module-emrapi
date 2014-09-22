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
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.test.builder.ObsBuilder;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class ObservationMapperTest extends BaseModuleContextSensitiveTest {

    @Mock
    private Concept concept;
    @Mock
    private ConceptNumeric conceptNumeric;
    @Mock
    private ConceptDatatype conceptDatatype;
    private ObservationMapper observationMapper;
    private ObsBuilder obsBuilder;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        observationMapper = new ObservationMapper();
        obsBuilder = new ObsBuilder();
        obsBuilder.setUuid(UUID.randomUUID().toString()).setConcept(concept);
        when(concept.getName()).thenReturn(new ConceptName());
        when(concept.getDatatype()).thenReturn(conceptDatatype);
        when(concept.getConceptClass()).thenReturn(getConceptClass("conceptClassName"));
    }

    @Test
    public void shouldMapObservationWithNumericValue(){
        when(conceptDatatype.isNumeric()).thenReturn(true);
        Obs obs = obsBuilder.setValue(100.0).get();

        EncounterTransaction.Observation observation = observationMapper.map(obs);

        assertEquals(obs.getUuid(), observation.getUuid());
        assertEquals(100.0, observation.getValue());
    }

    @Test
    public void shouldMapObservationWithBooleanValue(){
        when(conceptDatatype.isBoolean()).thenReturn(true);
        Obs obs = obsBuilder.setValue(true).get();

        EncounterTransaction.Observation observation = observationMapper.map(obs);

        assertEquals(true, observation.getValue());
    }

    @Test
    public void shouldMapVoidedObservation(){
        when(conceptDatatype.isNumeric()).thenReturn(true);
        Obs obs = obsBuilder.setVoided(true).setVoidedReason("reason").get();

        EncounterTransaction.Observation observation = observationMapper.map(obs);

        assertEquals(obs.getUuid(), observation.getUuid());
        assertEquals(obs.getVoided(), observation.getVoided());
        assertEquals(obs.getVoidReason(), observation.getVoidReason());
    }

    @Test
    public void shouldMapConceptClassAndComment(){
        when(conceptDatatype.isNumeric()).thenReturn(true);
        Obs obs = obsBuilder.setComment("Intermittent Pain").get();

        EncounterTransaction.Observation observation = observationMapper.map(obs);

        assertEquals(obs.getComment(), observation.getComment());
        assertEquals(obs.getConcept().getConceptClass().getName(), observation.getConcept().getConceptClass());
    }

    private ConceptClass getConceptClass(String conceptClassName) {
        ConceptClass conceptClass = new ConceptClass();
        conceptClass.setName(conceptClassName);
        return conceptClass;
    }
}
