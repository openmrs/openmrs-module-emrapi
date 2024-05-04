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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.*;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.DrugMapper;
import org.openmrs.module.emrapi.encounter.builder.ObsBuilder1_12;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ObservationMapper1_12Test {
    @Mock
    private Concept concept;
    @Mock
    private DrugMapper drugMapper;
    @Mock
    private ConceptMapper conceptMapper;
    @Mock
    private ConceptNumeric conceptNumeric;
    @Mock
    private ConceptDatatype conceptDatatype;
    private ObservationMapper1_12 observationMapper;
    private ObsBuilder1_12 obsBuilder;

    @Before
    public void setUp(){
        User creator = mock(User.class);
        when(creator.getUuid()).thenReturn("uuid");
        PersonName mockPersonName = mock(PersonName.class);
        when(mockPersonName.toString()).thenReturn("superman");
        when(creator.getPersonName()).thenReturn(mockPersonName);
        MockitoAnnotations.initMocks(this);
        observationMapper = new ObservationMapper1_12(conceptMapper, drugMapper, new UserMapper());
        obsBuilder = new ObsBuilder1_12();
        obsBuilder.setUuid(UUID.randomUUID().toString()).setConcept(concept).setCreator(creator);
        when(concept.getName()).thenReturn(new ConceptName());
        when(concept.getDatatype()).thenReturn(conceptDatatype);
        when(concept.getConceptClass()).thenReturn(getConceptClass("conceptClassName"));
    }

    @Test
    public void shouldMapObservationWithNumericValue(){
        when(conceptDatatype.isNumeric()).thenReturn(true);
        Obs obs = obsBuilder.setFormField("form uuid", "formFieldPath").get();

        EncounterTransaction.Observation observation = observationMapper.map(obs);
        assertEquals(observation.getFormNamespace(), "form uuid");
        assertEquals(observation.getFormFieldPath(), "formFieldPath");
    }
    private ConceptClass getConceptClass(String conceptClassName) {
        ConceptClass conceptClass = new ConceptClass();
        conceptClass.setName(conceptClassName);
        return conceptClass;
    }
}
