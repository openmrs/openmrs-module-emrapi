/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
	 * Searches for concepts by name either 1) within the specified concept sources and/or concept
	 * classes, or 2) within the specified concept sets. The name search within classes and/or sources
	 * is bypassed when sets are provided for the search. In that case the name search only operates
	 * within those sets, and the classes and/or sources are just ignored. Also searches for concepts by
	 * mapping code if the concept sources are specified (regardless whether classes and/or sets are
	 * specified.)
	 *
	 * @param query name or term of concept to search for
	 * @param locale locale to search in
	 * @param classes concept classes to search against
	 * @param inSets concept sets to search in
	 * @param sources concept source to search against
	 * @param limit the maximum results to fetch
	 * @return concept search results
	 */
	List<ConceptSearchResult> conceptSearch(String query, Locale locale, Collection<ConceptClass> classes,
	        Collection<Concept> inSets, Collection<ConceptSource> sources, Integer limit);
	
}
