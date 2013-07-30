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

package org.openmrs.module.emrapi.test.builder;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;

import java.util.Locale;

/**
 * Helper for building concepts in unit tests
 */
public class ConceptBuilder {

    private ConceptService conceptService;
    private Concept concept;

    public ConceptBuilder(ConceptService conceptService, ConceptDatatype datatype, ConceptClass conceptClass) {
        this.conceptService = conceptService;
        concept = new Concept();
        concept.setDatatype(datatype);
        concept.setConceptClass(conceptClass);
    }

    public Concept get() {
        return concept;
    }

    public ConceptBuilder setUuid(String uuid) {
        concept.setUuid(uuid);
        return this;
    }

    public ConceptBuilder add(ConceptName conceptName) {
        if (concept.getNames().size() == 0) {
            conceptName.setLocalePreferred(true);
            conceptName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
        }
        concept.addName(conceptName);
        return this;
    }

    public ConceptBuilder addMapping(ConceptMapType mapType, ConceptSource source, String code) {
        if (mapType == null || source == null || code == null) {
            throw new IllegalArgumentException("mapType, source, and code are all required");
        }
        ConceptReferenceTerm term = new ConceptReferenceTerm(source, code, null);
        if (conceptService != null) {
            conceptService.saveConceptReferenceTerm(term);
        }
        ConceptMap conceptMap = new ConceptMap(term, mapType);
        concept.addConceptMapping(conceptMap);
        return this;
    }

    public ConceptBuilder addVoidedName(ConceptName voidedName) {
        voidedName.setVoided(true);
        return add(voidedName);
    }

    public ConceptBuilder addSetMember(Concept setMember) {
        concept.addSetMember(setMember);
        return this;
    }

    public ConceptBuilder addName(String englishName) {
        return add(new ConceptName(englishName, Locale.ENGLISH));
    }

    public Concept saveAndGet() {
        if (conceptService == null) {
            throw new IllegalStateException("No conceptService available");
        }
        return conceptService.saveConcept(concept);
    }

    public ConceptBuilder addAnswers(Concept... answers) {
        if (!concept.getDatatype().isCoded()) {
            throw new IllegalStateException("Cannot add answers to a concept with datatype " + concept.getDatatype().getName());
        }
        for (Concept answer : answers) {
            concept.addAnswer(new ConceptAnswer(answer));
        }
        return this;
    }

    public ConceptBuilder addSetMembers(Concept... setMembers) {
        for (Concept concept : setMembers) {
            addSetMember(concept);
        }
        return this;
    }

}
