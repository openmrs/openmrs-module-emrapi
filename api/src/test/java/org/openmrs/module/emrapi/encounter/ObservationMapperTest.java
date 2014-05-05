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
import org.openmrs.*;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.test.builder.ObsBuilder;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

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
        initMocks(this);
        observationMapper = new ObservationMapper();
        obsBuilder = new ObsBuilder();
    }

    @Test
    public void map_Observation_With_NumericValue(){
        String uuid = UUID.randomUUID().toString();
        obsBuilder.setUuid(uuid).setValue(100.0).setConcept(conceptNumeric);
        when(conceptNumeric.getName()).thenReturn(new ConceptName());
        when(conceptNumeric.getDatatype()).thenReturn(conceptDatatype);
        when(conceptDatatype.isNumeric()).thenReturn(true);
        when(conceptNumeric.getUnits()).thenReturn("mg");

        Obs obs = obsBuilder.get();

        EncounterTransaction.Observation observation = observationMapper.map(obs);

        assertEquals(uuid, observation.getUuid());
        assertEquals(100.0, observation.getValue());
    }

    @Test
    public void map_Observation_With_Boolean_Value(){
        when(concept.getName()).thenReturn(new ConceptName());
        when(concept.getDatatype()).thenReturn(conceptDatatype);
        when(concept.getConceptClass()).thenReturn(getConceptClass("conceptClassName"));
        when(conceptDatatype.isBoolean()).thenReturn(true);
        Obs obs = obsBuilder.setConcept(concept).setValue(true).get();

        EncounterTransaction.Observation observation = observationMapper.map(obs);

        assertEquals(true, observation.getValue());
    }

    @Test
    public void map_Voided_Observation(){
        String uuid = UUID.randomUUID().toString();
        obsBuilder.setUuid(uuid).setValue(100.0).setConcept(conceptNumeric).setVoided(true).setVoidedReason("reason");
        when(conceptNumeric.getName()).thenReturn(new ConceptName());
        when(conceptNumeric.getDatatype()).thenReturn(conceptDatatype);
        when(conceptDatatype.isNumeric()).thenReturn(true);
        when(conceptNumeric.getUnits()).thenReturn("mg");
        when(conceptNumeric.getConceptClass()).thenReturn(getConceptClass("conceptClassName"));

        Obs obs = obsBuilder.get();

        EncounterTransaction.Observation observation = observationMapper.map(obs);

        assertEquals(uuid, observation.getUuid());
        assertEquals(100.0, observation.getValue());
        assertEquals(obs.getVoided(), observation.getVoided());
        assertEquals(obs.getVoidReason(), observation.getVoidReason());
    }

    @Test
    public void map_concept_class(){
        String conceptClassName = "conceptClassName";

        obsBuilder.setUuid(UUID.randomUUID().toString()).setValue(100.0).setConcept(conceptNumeric);
        when(conceptNumeric.getName()).thenReturn(new ConceptName());
        when(conceptNumeric.getDatatype()).thenReturn(conceptDatatype);
        when(conceptDatatype.isNumeric()).thenReturn(true);
        when(conceptNumeric.getUnits()).thenReturn("mg");
        when(conceptNumeric.getConceptClass()).thenReturn(getConceptClass(conceptClassName));

        Obs obs = obsBuilder.get();

        EncounterTransaction.Observation observation = observationMapper.map(obs);

        assertEquals(conceptClassName, observation.getConcept().getConceptClass());
    }

    private ConceptClass getConceptClass(String conceptClassName) {
        ConceptClass conceptClass = new ConceptClass();
        conceptClass.setName(conceptClassName);
        return conceptClass;
    }
}
