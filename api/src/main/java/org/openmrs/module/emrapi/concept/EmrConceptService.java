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

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Additional useful methods not (yet) available via the core OpenMRS API
 */
public interface EmrConceptService {

    /**
     * @param term
     * @return all concepts with SAME-AS or NARROWER-THAN mappings to term
     */
    List<Concept> getConceptsSameOrNarrowerThan(ConceptReferenceTerm term);

    /**
     * Searches for a concept by treating mappingOrUuid as (in order):
     * (1) source_name:code
     * (2) uuid
     * @param mappingOrUuid
     * @return
     */
    Concept getConcept(String mappingOrUuid);

    /**
     * Searches for concepts by a fuzzy name match, or an exact match on a concept mapping
     * @param query
     * @param locale
     * @param classes if specified, only search among concepts with this class
     * @param inSets if specified, only search among concepts within these sets (doesn't explode sets-of-sets; caller must do this)
     * @param sources if specified, search for exact matches on mappings in this source
     * @param limit return up to this many results (defaults to 100)
     * @return
     */
    List<ConceptSearchResult> conceptSearch(String query, Locale locale, Collection<ConceptClass> classes, Collection<Concept> inSets, Collection<ConceptSource> sources, Integer limit);

}
