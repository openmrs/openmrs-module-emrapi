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
import org.openmrs.Drug;
import org.openmrs.User;
import org.openmrs.PersonName;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.mapper.UserMapper;
import org.openmrs.module.emrapi.test.builder.ConceptBuilder;
import org.openmrs.module.emrapi.test.builder.ObsBuilder;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ObservationMapperTest extends BaseModuleContextSensitiveTest {

    @Mock
    private Concept concept;
    @Mock
    private ConceptNumeric conceptNumeric;
    @Mock
    private ConceptDatatype conceptDatatype;
    @Mock
    private DrugMapper drugMapper;
    private ObservationMapper observationMapper;
    private ObsBuilder obsBuilder;

    @Before
    public void setUp(){
        User creator = mock(User.class);
        when(creator.getUuid()).thenReturn("uuid");
        PersonName mockPersonName = mock(PersonName.class);
        when(mockPersonName.toString()).thenReturn("superman");
        when(creator.getPersonName()).thenReturn(mockPersonName);
        MockitoAnnotations.initMocks(this);
        observationMapper = new ObservationMapper(new ConceptMapper(), drugMapper, new UserMapper());
        obsBuilder = new ObsBuilder();
        obsBuilder.setUuid(UUID.randomUUID().toString()).setConcept(concept).setCreator(creator);
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
    public void shouldMapObservationWithCodedValue(){
        when(conceptDatatype.isCoded()).thenReturn(true);
        Concept concept = new ConceptBuilder(null, new ConceptDatatype(2), null).addName("concept-name").get();
        Obs obs = obsBuilder.setValue(concept).get();

        EncounterTransaction.Observation observation = observationMapper.map(obs);

        assertEquals(obs.getUuid(), observation.getUuid());
        EncounterTransaction.Concept answer = (EncounterTransaction.Concept) observation.getValue();
        assertEquals(concept.getName().getName(), answer.getName());
    }

    @Test
    public void shouldMapObservationWithValueAsDrug(){
        when(conceptDatatype.isCoded()).thenReturn(true);
        Drug drug = new Drug(2);
        Obs obs = obsBuilder.setValue(drug).get();
        EncounterTransaction.Drug mappedDrug = new EncounterTransaction.Drug();
        when(drugMapper.map(drug)).thenReturn(mappedDrug);

        EncounterTransaction.Observation observation = observationMapper.map(obs);

        assertEquals(obs.getUuid(), observation.getUuid());
        assertEquals(mappedDrug, observation.getValue());
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

    @Test
    public void shouldMapDateTime() {
        when(conceptDatatype.isDateTime()).thenReturn(true);
        Obs obs = obsBuilder.setValue("2015-02-01 03:45:09").get();

        EncounterTransaction.Observation observation = observationMapper.map(obs);

        assertEquals(observation.getValue(), obs.getValueDatetime());
    }

    @Test
    public void shouldMapDate() {
        when(conceptDatatype.isDate()).thenReturn(true);
        Obs obs = obsBuilder.setValue("2015-02-01").get();

        EncounterTransaction.Observation observation = observationMapper.map(obs);

        assertEquals(observation.getValue(), obs.getValueDate());
    }

    @Test
    public void shouldMapCreator() {
        when(conceptDatatype.isDate()).thenReturn(true);
        Obs obs = obsBuilder.setValue("2015-02-01").get();

        EncounterTransaction.Observation observation = observationMapper.map(obs);

        assertEquals("uuid", observation.getCreator().getUuid());
        assertEquals("superman", observation.getCreator().getPersonName());
    }

    private ConceptClass getConceptClass(String conceptClassName) {
        ConceptClass conceptClass = new ConceptClass();
        conceptClass.setName(conceptClassName);
        return conceptClass;
    }
}
