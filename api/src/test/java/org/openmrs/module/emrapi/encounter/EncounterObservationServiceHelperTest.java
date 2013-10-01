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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterObservationServiceHelperTest {

    public static final String TEXT_CONCEPT_UUID = "text-concept-uuid";
    public static final String NUMERIC_CONCEPT_UUID = "numeric-concept-uuid";
    @Mock
    private ConceptService conceptService;
    @Mock
    private ObsService obsService;
    @Mock
    private DiagnosisMetadata diagnosisMetadata;
    @Mock
    private EmrApiProperties emrApiProperties;

    private EncounterObservationServiceHelper encounterObservationServiceHelper;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(emrApiProperties.getDiagnosisMetadata()).thenReturn(diagnosisMetadata);
        encounterObservationServiceHelper = new EncounterObservationServiceHelper(conceptService, emrApiProperties);
    }

    @Test
    public void shouldAddNewObservation() throws ParseException {
        newConcept(ConceptDatatype.TEXT, TEXT_CONCEPT_UUID);

        List<EncounterTransaction.Observation> observations = asList(
            new EncounterTransaction.Observation().setConceptUuid(TEXT_CONCEPT_UUID).setValue("text value").setComment("overweight")
        );

        Patient patient = new Patient();
        Encounter encounter = new Encounter();
        encounter.setUuid("e-uuid");
        encounter.setPatient(patient);

        Date observationDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse("2005-01-01T00:00:00.000+0000");

        encounterObservationServiceHelper.update(encounter, observations, observationDateTime);

        assertEquals(1, encounter.getObs().size());
        Obs textObservation = encounter.getObs().iterator().next();

        assertEquals(patient, textObservation.getPerson());

        assertEquals("text value", textObservation.getValueText());
        assertEquals(TEXT_CONCEPT_UUID, textObservation.getConcept().getUuid());
        assertEquals("e-uuid", textObservation.getEncounter().getUuid());
        assertEquals("overweight", textObservation.getComment());

        assertEquals(observationDateTime, textObservation.getObsDatetime());
    }

    @Test
    public void shouldUpdateExistingObservation() throws ParseException {
        Concept numericConcept = newConcept(ConceptDatatype.NUMERIC, NUMERIC_CONCEPT_UUID);

        List<EncounterTransaction.Observation> observations = asList(
                new EncounterTransaction.Observation().setConceptUuid(NUMERIC_CONCEPT_UUID).setValue(35.0).setComment("overweight")
        );

        Encounter encounter = new Encounter();
        encounter.setUuid("e-uuid");
        Obs obs = new Obs();
        obs.setUuid("o-uuid");
        obs.setConcept(numericConcept);
        encounter.addObs(obs);

        Date observationDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse("2005-01-01T00:00:00.000+0000");
        encounterObservationServiceHelper.update(encounter, observations, observationDateTime);

        assertEquals(1, encounter.getObs().size());
        Obs textObservation = encounter.getObs().iterator().next();

        assertEquals(new Double(35.0), textObservation.getValueNumeric());
        assertEquals("overweight", textObservation.getComment());

        assertEquals(observationDateTime, textObservation.getObsDatetime());
    }

    @Test
    public void shouldVoidExistingObservation() throws ParseException {
        Concept numericConcept = newConcept(ConceptDatatype.NUMERIC, NUMERIC_CONCEPT_UUID);

        List<EncounterTransaction.Observation> observations = asList(
                new EncounterTransaction.Observation().setObservationUuid("o-uuid").setConceptUuid(NUMERIC_CONCEPT_UUID).setVoided(true).setVoidReason("closed")
        );

        Encounter encounter = new Encounter();
        encounter.setUuid("e-uuid");
        Obs obs = new Obs();
        obs.setUuid("o-uuid");
        obs.setConcept(numericConcept);
        encounter.addObs(obs);

        Date observationDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse("2005-01-01T00:00:00.000+0000");
        encounterObservationServiceHelper.update(encounter, observations, observationDateTime);

        assertEquals(0, encounter.getObs().size());
        assertEquals(1, encounter.getAllObs(true).size());

        Obs voidedObs = encounter.getAllObs(true).iterator().next();
        assertTrue(voidedObs.isVoided());
        assertEquals("closed", voidedObs.getVoidReason());
    }

    @Test(expected = ConceptNotFoundException.class)
    public void shouldReturnErrorWhenObservationConceptIsNotFound() throws Exception {
        List<EncounterTransaction.Observation> observations = asList(
                new EncounterTransaction.Observation().setConceptUuid("non-existent")
        );
        Encounter encounter = new Encounter();
        encounterObservationServiceHelper.update(encounter, observations, null);
    }

    @Test
    public void shouldSaveDiagnosisAsAnObservation() {
        String diagnosisConceptId = "123";
        List<EncounterTransaction.DiagnosisRequest> diagnosisRequests = asList(
                new EncounterTransaction.DiagnosisRequest().setCertainty("CONFIRMED").setOrder("PRIMARY").setDiagnosis("Concept:" + diagnosisConceptId)
        );

        Encounter encounter = new Encounter();
        encounter.setUuid("e-uuid");
        Obs savedObservations = new Obs();
        savedObservations.addGroupMember(new Obs());
        savedObservations.addGroupMember(new Obs());
        savedObservations.addGroupMember(new Obs());
        Concept conceptForDiagnosis = new Concept();

        when(diagnosisMetadata.buildDiagnosisObsGroup(any(Diagnosis.class))).thenReturn(savedObservations);
        when(conceptService.getConcept(Integer.valueOf(diagnosisConceptId))).thenReturn(conceptForDiagnosis);

        encounterObservationServiceHelper.updateDiagnoses(encounter, diagnosisRequests, new Date());

        Set<Obs> parentObservations = encounter.getObsAtTopLevel(false);
        assertEquals(1, parentObservations.size());
        Obs parent = parentObservations.iterator().next();
        assertTrue(parent.isObsGrouping());
        int children = parent.getGroupMembers().size();
        assertEquals(3, children);
        ArgumentCaptor<Diagnosis> diagnosisCaptor = ArgumentCaptor.forClass(Diagnosis.class);
        verify(diagnosisMetadata, times(1)).buildDiagnosisObsGroup(diagnosisCaptor.capture());
        Diagnosis diagnosis = diagnosisCaptor.getValue();
        assertEquals(conceptForDiagnosis, diagnosis.getDiagnosis().getCodedAnswer());
        assertEquals(Diagnosis.Certainty.CONFIRMED, diagnosis.getCertainty());
        assertEquals(Diagnosis.Order.PRIMARY, diagnosis.getOrder());
    }

    private Concept newConcept(String hl7, String uuid) {
        Concept concept = new Concept();
        ConceptDatatype textDataType = new ConceptDatatype();
        textDataType.setHl7Abbreviation(hl7);
        concept.setDatatype(textDataType);
        concept.setUuid(uuid);
        when(conceptService.getConceptByUuid(uuid)).thenReturn(concept);
        return concept;
    }
}
