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
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public interface EmrConceptDAO {

    List<Concept> getConceptsMappedTo(Collection<ConceptMapType> mapTypes, ConceptReferenceTerm term);

    /**
     * Searches for concepts based on either inSets or classes, sources or combination of classes and sources 
     * in the specified locale. If inSets is specified, it overrides sources and classes.
     * Can search concepts based on code as the query restricted by the specified sources.
     *
     * @param query name or term of concept to search for
     * @param locale locale to search in
     * @param classes concept classes to search against
     * @param inSets concept sets to search in
     * @param sources concept source to searcch against
     * @param limit the maximum results to fetch
     */
    List<ConceptSearchResult> conceptSearch(String query, Locale locale, Collection<ConceptClass> classes, Collection<Concept> inSets, Collection<ConceptSource> sources, Integer limit);

}
