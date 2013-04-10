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

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiConstants;

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
}
