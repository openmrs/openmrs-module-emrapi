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

package org.openmrs.module.emrapi.concept;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.test.builder.ConceptBuilder;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class EmrConceptServiceComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EmrConceptService emrConceptService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("conceptMapTypes.xml");
    }

    @Test
    public void testGetConceptsSameOrNarrowerThanTerm() throws Exception {
        ConceptSource source = conceptService.getConceptSource(1);

        ConceptMapType sameAs = conceptService.getConceptMapTypeByUuid(EmrApiConstants.SAME_AS_CONCEPT_MAP_TYPE_UUID);
        ConceptMapType narrowerThan = conceptService.getConceptMapTypeByUuid(EmrApiConstants.NARROWER_THAN_CONCEPT_MAP_TYPE_UUID);
        ConceptMapType someOtherType = conceptService.getConceptMapType(5);

        ConceptReferenceTerm term = new ConceptReferenceTerm(source, "food-assist", null);
        conceptService.saveConceptReferenceTerm(term);

        Concept foodAssistance = conceptService.getConcept(18);
        foodAssistance.addConceptMapping(new ConceptMap(term, sameAs));
        conceptService.saveConcept(foodAssistance);

        Concept foodAssistanceForEntireFamily = conceptService.getConcept(21);
        foodAssistanceForEntireFamily.addConceptMapping(new ConceptMap(term, narrowerThan));
        conceptService.saveConcept(foodAssistanceForEntireFamily);

        Concept anotherConcept = conceptService.getConcept(20);
        anotherConcept.addConceptMapping(new ConceptMap(term, someOtherType));
        conceptService.saveConcept(anotherConcept);

        List<Concept> actual = emrConceptService.getConceptsSameOrNarrowerThan(term);
        assertThat(actual.size(), is(2));
        assertThat(actual, IsIterableContainingInAnyOrder.containsInAnyOrder(foodAssistance, foodAssistanceForEntireFamily));
    }

    @Test
    public void testConceptSearchByName() throws Exception {
        Map<String, Concept> concepts = setupConcepts();
        ConceptClass diagnosis = conceptService.getConceptClassByName("Diagnosis");

        List<ConceptSearchResult> searchResults = emrConceptService.conceptSearch("malaria", Locale.ENGLISH, Collections.singleton(diagnosis), null, null, null);

        assertThat(searchResults.size(), is(2));

        ConceptSearchResult firstResult = searchResults.get(0);
        ConceptSearchResult otherResult = searchResults.get(1);

        assertThat(firstResult.getConcept(), is(concepts.get("malaria")));
        assertThat(firstResult.getConceptName().getName(), is("Malaria"));

        assertThat(otherResult.getConcept(), is(concepts.get("cerebral malaria")));
        assertThat(otherResult.getConceptName().getName(), is("Cerebral Malaria"));
    }

    @Test
    public void testConceptSearchInAnotherLocale() throws Exception {
        Map<String, Concept> concepts = setupConcepts();
        ConceptClass diagnosis = conceptService.getConceptClassByName("Diagnosis");

        List<ConceptSearchResult> searchResults = emrConceptService.conceptSearch("malaria", Locale.FRENCH, Collections.singleton(diagnosis), null, null, null);
        ConceptSearchResult firstResult = searchResults.get(0);

        assertThat(searchResults.size(), is(1));
        assertThat(firstResult.getConcept(), is(concepts.get("cerebral malaria")));
        assertThat(firstResult.getConceptName().getName(), is("Malaria célébrale"));
    }

    @Test
    public void testConceptSearchByIcd10Code() throws Exception {
        ConceptClass diagnosis = conceptService.getConceptClassByName("Diagnosis");
        ConceptSource icd10 = conceptService.getConceptSourceByName("ICD-10");

        Map<String, Concept> concepts = setupConcepts();

        List<ConceptSearchResult> searchResults = emrConceptService.conceptSearch("E11.9", Locale.ENGLISH, Collections.singleton(diagnosis), null, Collections.singleton(icd10), null);
        ConceptSearchResult firstResult = searchResults.get(0);

        assertThat(searchResults.size(), is(1));
        assertThat(firstResult.getConcept(), is(concepts.get("diabetes")));
        assertThat(firstResult.getConceptName(), nullValue());
    }

    @Test
    public void testConceptSearchForSetMembers() throws Exception {
        Map<String, Concept> concepts = setupConcepts();

        List<ConceptSearchResult> searchResults = emrConceptService.conceptSearch("malar", Locale.ENGLISH, null, Collections.singleton(concepts.get("allowedDiagnoses")), null, null);
        assertThat(searchResults.size(), is(1));
        ConceptSearchResult firstResult = searchResults.get(0);
        assertThat(firstResult.getConcept(), is(concepts.get("malaria")));

        searchResults = emrConceptService.conceptSearch("diab", Locale.ENGLISH, null, Collections.singleton(concepts.get("allowedDiagnoses")), null, null);
        assertThat(searchResults.size(), is(1));
        firstResult = searchResults.get(0);
        assertThat(firstResult.getConcept(), is(concepts.get("diabetes")));
    }

    private Map<String, Concept> setupConcepts() {
        Map<String, Concept> concepts = new HashMap<String, Concept>();

        ConceptMapType sameAs = conceptService.getConceptMapTypeByName("same-as");
        ConceptSource icd10 = conceptService.getConceptSourceByName("ICD-10");

        ConceptDatatype na = conceptService.getConceptDatatypeByName("N/A");
        ConceptClass diagnosis = conceptService.getConceptClassByName("Diagnosis");
        ConceptClass convSet = conceptService.getConceptClassByName("ConvSet");

        concepts.put("malaria", conceptService.saveConcept(new ConceptBuilder(conceptService, na, diagnosis)
                .add(new ConceptName("Malaria", Locale.ENGLISH))
                .add(new ConceptName("Clinical Malaria", Locale.ENGLISH))
                .add(new ConceptName("Paludisme", Locale.FRENCH))
                .addMapping(sameAs, icd10, "B54").get()));

        concepts.put("cerebral malaria", conceptService.saveConcept(new ConceptBuilder(conceptService, na, diagnosis)
                .add(new ConceptName("Cerebral Malaria", Locale.ENGLISH))
                .add(new ConceptName("Malaria célébrale", Locale.FRENCH))
                .addMapping(sameAs, icd10, "B50.0").get()));

        concepts.put("diabetes", conceptService.saveConcept(new ConceptBuilder(conceptService, na, diagnosis)
                .add(new ConceptName("Diabetes Mellitus, Type II", Locale.ENGLISH))
                .addVoidedName(new ConceptName("Malaria", Locale.ENGLISH))
                .addMapping(sameAs, icd10, "E11.9").get()));

        concepts.put("allowedDiagnoses", conceptService.saveConcept(new ConceptBuilder(conceptService, na, convSet)
                .add(new ConceptName("Allowed Diagnoses", Locale.ENGLISH))
                .addSetMember(concepts.get("malaria"))
                .addSetMember(concepts.get("diabetes")).get()));

        return concepts;
    }

    private ArgumentMatcher<ConceptSearchResult> searchResultMatcher(final Concept concept, final String nameMatched) {
        return new ArgumentMatcher<ConceptSearchResult>() {
            @Override
            public boolean matches(Object o) {
                ConceptSearchResult actual = (ConceptSearchResult) o;
                return actual.getConcept().equals(concept) && actual.getConceptName().getName().equals(nameMatched);
            }
        };
    }

}
