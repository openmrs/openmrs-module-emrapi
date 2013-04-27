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
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.core.Is.is;
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

}
