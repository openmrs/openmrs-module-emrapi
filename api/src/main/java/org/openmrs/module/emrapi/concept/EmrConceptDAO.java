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
     * Searches for concepts by name either 1) within the specified concept sources and/or concept classes, or 2) within the specified concept sets.
	 * The name search within classes and/or sources is bypassed when sets are provided for the search. In that case the name search only operates within those sets, 
	 * and the classes and/or sources are just ignored.
	 * Also searches for concepts by mapping code if the concept sources are specified (regardless whether classes and/or sets are specified.)
     *
     * @param query name or term of concept to search for
     * @param locale locale to search in
     * @param classes concept classes to search against
     * @param inSets concept sets to search in
     * @param sources concept source to search against
     * @param limit the maximum results to fetch
     * @return concept search results
     */
    List<ConceptSearchResult> conceptSearch(String query, Locale locale, Collection<ConceptClass> classes, Collection<Concept> inSets, Collection<ConceptSource> sources, Integer limit);

}
