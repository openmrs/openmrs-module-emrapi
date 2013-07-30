/*
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

package org.openmrs.module.emrapi.diagnosis;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.test.MockMetadataTestUtil;

import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class DiagnosisMetadataTest {

    private ConceptMapType sameAs;
    private ConceptSource emrConceptSource;
    private ConceptService conceptService;

    @Before
    public void setUp() throws Exception {
        sameAs = new ConceptMapType();
        emrConceptSource = new ConceptSource();
        emrConceptSource.setName(EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
        conceptService = mock(ConceptService.class);
    }

    @Test
    public void testConstructor() throws Exception {
        Concept codedDiagnosis = setupConcept(conceptService, "Coded Diagnosis", EmrApiConstants.CONCEPT_CODE_CODED_DIAGNOSIS);
        Concept nonCodedDiagnosis = setupConcept(conceptService, "Non-Coded Diagnosis", EmrApiConstants.CONCEPT_CODE_NON_CODED_DIAGNOSIS);
        Concept diagnosisOrder = setupConcept(conceptService, "Diagnosis Order", EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER);
        diagnosisOrder.addAnswer(new ConceptAnswer(setupConcept(conceptService, "Primary", EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_PRIMARY)));
        diagnosisOrder.addAnswer(new ConceptAnswer(setupConcept(conceptService, "Secondary", EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_SECONDARY)));
        Concept diagnosisCertainty = setupConcept(conceptService, "Diagnosis Certainty", EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY);
        diagnosisCertainty.addAnswer(new ConceptAnswer(setupConcept(conceptService, "Confirmed", EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY_CONFIRMED)));
        diagnosisCertainty.addAnswer(new ConceptAnswer(setupConcept(conceptService, "Presumed", EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY_PRESUMED)));

        Concept diagnosisSet = setupConcept(conceptService, "Diagnosis Set", EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CONCEPT_SET);
        diagnosisSet.addSetMember(codedDiagnosis);
        diagnosisSet.addSetMember(nonCodedDiagnosis);
        diagnosisSet.addSetMember(diagnosisOrder);
        diagnosisSet.addSetMember(diagnosisCertainty);

        DiagnosisMetadata diagnosisMetadata = new DiagnosisMetadata(conceptService, emrConceptSource);
        assertThat(diagnosisMetadata.getDiagnosisSetConcept(), is(diagnosisSet));
        assertThat(diagnosisMetadata.getCodedDiagnosisConcept(), is(codedDiagnosis));
        assertThat(diagnosisMetadata.getNonCodedDiagnosisConcept(), is(nonCodedDiagnosis));
        assertThat(diagnosisMetadata.getDiagnosisOrderConcept(), is(diagnosisOrder));
        assertThat(diagnosisMetadata.getDiagnosisCertaintyConcept(), is(diagnosisCertainty));
    }

    private Concept setupConcept(ConceptService mockConceptService, String name, String mappingCode) {
        Concept concept = new Concept();
        concept.addName(new ConceptName(name, Locale.ENGLISH));
        concept.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(emrConceptSource, mappingCode, null), sameAs));
        when(mockConceptService.getConceptByMapping(mappingCode, emrConceptSource.getName())).thenReturn(concept);
        return concept;
    }

    @Test
    public void buildDiagnosisObsGroup_should_createNewObsGroup() throws Exception {
        String nonCodedAnswer = "Free text";

        EmrApiProperties emrApiProperties = mock(EmrApiProperties.class);
        MockMetadataTestUtil.setupMockConceptService(conceptService, emrApiProperties);
        MockMetadataTestUtil.setupDiagnosisMetadata(emrApiProperties, conceptService);

        Diagnosis diagnosis = new Diagnosis(new CodedOrFreeTextAnswer(nonCodedAnswer), Diagnosis.Order.PRIMARY);
        diagnosis.setCertainty(Diagnosis.Certainty.PRESUMED);

        DiagnosisMetadata dmd = emrApiProperties.getDiagnosisMetadata();
        Obs obs = dmd.buildDiagnosisObsGroup(diagnosis);

        assertThat(obs.getConcept(), is(dmd.getDiagnosisSetConcept()));
        assertThat(obs.getGroupMembers().size(), is(3));
        assertThat(obs, hasGroupMember(dmd.getDiagnosisOrderConcept(), dmd.getConceptFor(Diagnosis.Order.PRIMARY), false));
        assertThat(obs, hasGroupMember(dmd.getDiagnosisCertaintyConcept(), dmd.getConceptFor(Diagnosis.Certainty.PRESUMED), false));
        assertThat(obs, hasGroupMember(dmd.getNonCodedDiagnosisConcept(), nonCodedAnswer, false));
    }

    @Test
    public void buildDiagnosisObsGroup_should_editExistingObsGroup() throws Exception {
        String oldNonCodedAnswer = "Free text";
        String newNonCodedAnswer = "Another answer";

        EmrApiProperties emrApiProperties = mock(EmrApiProperties.class);
        MockMetadataTestUtil.setupMockConceptService(conceptService, emrApiProperties);
        MockMetadataTestUtil.setupDiagnosisMetadata(emrApiProperties, conceptService);

        // first, build the original obs
        Diagnosis diagnosis = new Diagnosis(new CodedOrFreeTextAnswer(oldNonCodedAnswer), Diagnosis.Order.PRIMARY);
        diagnosis.setCertainty(Diagnosis.Certainty.PRESUMED);

        DiagnosisMetadata dmd = emrApiProperties.getDiagnosisMetadata();
        Obs obs = dmd.buildDiagnosisObsGroup(diagnosis);

        // now modify it
        diagnosis.setExistingObs(obs);
        diagnosis.setCertainty(Diagnosis.Certainty.CONFIRMED);
        diagnosis.setDiagnosis(new CodedOrFreeTextAnswer(newNonCodedAnswer));
        obs = dmd.buildDiagnosisObsGroup(diagnosis);

        assertThat(obs.getConcept(), is(dmd.getDiagnosisSetConcept()));
        assertThat(obs.getGroupMembers(false).size(), is(3));
        assertThat(obs.getGroupMembers(true).size(), is(5));
        assertThat(obs, hasGroupMember(dmd.getDiagnosisOrderConcept(), dmd.getConceptFor(Diagnosis.Order.PRIMARY), false));
        assertThat(obs, hasGroupMember(dmd.getDiagnosisCertaintyConcept(), dmd.getConceptFor(Diagnosis.Certainty.CONFIRMED), false));
        assertThat(obs, hasGroupMember(dmd.getNonCodedDiagnosisConcept(), newNonCodedAnswer, false));
        assertThat(obs, hasGroupMember(dmd.getDiagnosisCertaintyConcept(), dmd.getConceptFor(Diagnosis.Certainty.PRESUMED), true));
        assertThat(obs, hasGroupMember(dmd.getNonCodedDiagnosisConcept(), oldNonCodedAnswer, true));
    }

    private Matcher<? super Obs> hasGroupMember(final Concept question, final Object answer, final boolean isVoided) {
        return new ArgumentMatcher<Obs>() {
            @Override
            public boolean matches(Object argument) {
                Obs actualGroup = (Obs) argument;
                return CoreMatchers.hasItem(new ArgumentMatcher<Obs>() {
                    @Override
                    public boolean matches(Object argument) {
                        Obs actual = (Obs) argument;
                        return actual.getConcept().equals(question) &&
                                actual.isVoided() == isVoided &&
                                (answer instanceof Concept && actual.getValueCoded().equals(answer)
                                        || answer instanceof String && actual.getValueText().equals(answer));
                    }
                }).matches(actualGroup.getGroupMembers(true));
            }
        };
    }

}
