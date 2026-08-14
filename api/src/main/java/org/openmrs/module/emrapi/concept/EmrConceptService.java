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
	 * Unlike ConceptService.getConceptByMapping(), this does not fail when a term carries mappings of
	 * more than one type, and it only considers SAME-AS mappings.
	 *
	 * @param term
	 * @return all concepts with SAME-AS mappings to term, including retired ones
	 */
	List<Concept> getConceptsSameAs(ConceptReferenceTerm term);
	
	/**
	 * Dose forms are grouped into dose form groups, and it is the groups that carry the routes of
	 * administration. Retired dose forms and retired dose form groups are excluded.
	 *
	 * @return every dose form, with the dose form groups it belongs to, which is empty for a dose form
	 *         belonging to no group
	 */
	List<DoseFormMembership> getDoseFormMemberships();
	
	/**
	 * Retired dose form groups are excluded, as are routes that resolve to no concept or only to a
	 * retired one, so a group may be returned with no routes at all.
	 *
	 * @return every dose form group, with its routes of administration
	 */
	List<DoseFormGroupRoutes> getDoseFormGroupRoutes();
	
	/**
	 * The routes of administration of a dose form are those of all the dose form groups it belongs to,
	 * e.g. "oral spray" is administered by every route of both the oral spray group and the oral group.
	 *
	 * @param doseForm
	 * @return the distinct routes of administration of the dose form, empty if it belongs to no dose
	 *         form group or none of its groups has a route
	 */
	List<Concept> getRoutesOfAdministration(Concept doseForm);
	
	/**
	 * Searches for a concept by treating mappingOrUuid as (in order): (1) source_name:code (2) uuid
	 *
	 * @param mappingOrUuid
	 * @return
	 */
	Concept getConcept(String mappingOrUuid);
	
	/**
	 * Searches for concepts by a fuzzy name match, or an exact match on a concept mapping
	 *
	 * @param query
	 * @param locale
	 * @param classes if specified, only search among concepts with this class
	 * @param inSets if specified, only search among concepts within these sets (doesn't explode
	 *            sets-of-sets; caller must do this)
	 * @param sources if specified, search for exact matches on mappings in this source
	 * @param limit return up to this many results (defaults to 100)
	 * @return
	 */
	List<ConceptSearchResult> conceptSearch(String query, Locale locale, Collection<ConceptClass> classes,
	        Collection<Concept> inSets, Collection<ConceptSource> sources, Integer limit);
	
}
