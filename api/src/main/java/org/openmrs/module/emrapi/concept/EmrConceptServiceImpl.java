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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class EmrConceptServiceImpl extends BaseOpenmrsService implements EmrConceptService {
	
	private final Log log = LogFactory.getLog(getClass());
	
	private EmrConceptDAO dao;
	
	private ConceptService conceptService;
	
	private EmrApiProperties emrApiProperties;
	
	// This will match "ICD10:A50" or "PIH : Admit"
	// [^:]+? ... anything that is not a colon, reluctantly (so the next thing catches trailing spaces)
	// \s* ... 0 or more whitespaces, greedily
	// .+ ... anything
	private Pattern codePattern = Pattern.compile("([^:]+?)\\s*:\\s*(.+)");
	
	public void setDao(EmrConceptDAO dao) {
		this.dao = dao;
	}
	
	public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
		this.emrApiProperties = emrApiProperties;
	}
	
	public void setConceptService(ConceptService conceptService) {
		this.conceptService = conceptService;
	}
	
	@Override
	public List<Concept> getConceptsSameOrNarrowerThan(ConceptReferenceTerm term) {
		if (term == null) {
			throw new IllegalArgumentException("term is required");
		}
		return dao.getConceptsMappedTo(
		    Arrays.asList(emrApiProperties.getSameAsConceptMapType(), emrApiProperties.getNarrowerThanConceptMapType()),
		    term);
	}
	
	@Override
	public List<Concept> getConceptsSameAs(ConceptReferenceTerm term) {
		if (term == null) {
			throw new IllegalArgumentException("term is required");
		}
		return dao.getConceptsMappedTo(Collections.singletonList(emrApiProperties.getSameAsConceptMapType()), term);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Concept getConcept(String mappingOrUuid) {
		Concept concept = null;
		
		Matcher matcher = codePattern.matcher(mappingOrUuid);
		if (matcher.matches()) {
			String sourceName = matcher.group(1);
			String code = matcher.group(2);
			ConceptSource source = conceptService.getConceptSourceByName(sourceName);
			if (source == null) {
				log.warn("Couldn't find concept source named " + sourceName + " while looking up concept by mapping: "
				        + mappingOrUuid);
			} else {
				ConceptReferenceTerm referenceTerm = conceptService.getConceptReferenceTermByCode(code, source);
				// TODO ensure we return a SAME-AS mapping if one exists
				if (referenceTerm != null) {
					List<Concept> concepts = getConceptsSameOrNarrowerThan(referenceTerm);
					if (concepts.size() > 0) {
						return concepts.get(0);
					}
				}
			}
		}
		
		return conceptService.getConceptByUuid(mappingOrUuid);
	}
	
	@Override
	public List<ConceptSearchResult> conceptSearch(String query, Locale locale, Collection<ConceptClass> classes,
	        Collection<Concept> inSets, Collection<ConceptSource> sources, Integer limit) {
		if (limit == null) {
			limit = 100;
		}
		return dao.conceptSearch(query, locale, classes, inSets, sources, limit);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<DoseFormMembership> getDoseFormMemberships() {
		Map<String, List<Concept>> groupsByDoseFormUuid = getDoseFormGroupsByDoseFormUuid();
		
		// Every dose form is reported, whether or not a group claims it. The dose forms are enumerated
		// from their concept class rather than from group membership so that a dose form belonging to no
		// group is still returned, with an empty list of dose form groups.
		List<DoseFormMembership> memberships = new ArrayList<DoseFormMembership>();
		for (Concept doseForm : getConceptsByClassName(EmrApiConstants.DOSE_FORM_CONCEPT_CLASS_NAME)) {
			List<Concept> groups = groupsByDoseFormUuid.get(doseForm.getUuid());
			memberships.add(new DoseFormMembership(doseForm, groups == null ? new ArrayList<Concept>() : groups));
		}
		return memberships;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<DoseFormGroupRoutes> getDoseFormGroupRoutes() {
		List<DoseFormGroupRoutes> doseFormGroupRoutes = new ArrayList<DoseFormGroupRoutes>();
		for (Concept doseFormGroup : getConceptsByClassName(EmrApiConstants.DOSE_FORM_GROUP_CONCEPT_CLASS_NAME)) {
			doseFormGroupRoutes.add(new DoseFormGroupRoutes(doseFormGroup, getRoutesOfDoseFormGroup(doseFormGroup)));
		}
		return doseFormGroupRoutes;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Concept> getRoutesOfAdministration(Concept doseForm) {
		if (doseForm == null) {
			throw new IllegalArgumentException("doseForm is required");
		}
		
		List<Concept> doseFormGroups = getDoseFormGroupsByDoseFormUuid().get(doseForm.getUuid());
		if (doseFormGroups == null) {
			return Collections.emptyList();
		}
		
		List<Concept> routes = new ArrayList<Concept>();
		for (Concept doseFormGroup : doseFormGroups) {
			for (Concept route : getRoutesOfDoseFormGroup(doseFormGroup)) {
				if (!routes.contains(route)) {
					routes.add(route);
				}
			}
		}
		return routes;
	}
	
	/**
	 * Which dose form groups claim each dose form, keyed by the uuid of the dose form. A dose form that
	 * no group claims is absent. Group membership is not exclusive: hypothetically, "oral spray" in
	 * both the oral spray group and the oral group.
	 */
	private Map<String, List<Concept>> getDoseFormGroupsByDoseFormUuid() {
		Map<String, List<Concept>> groupsByDoseFormUuid = new HashMap<String, List<Concept>>();
		for (Concept doseFormGroup : getConceptsByClassName(EmrApiConstants.DOSE_FORM_GROUP_CONCEPT_CLASS_NAME)) {
			for (Concept doseForm : doseFormGroup.getSetMembers()) {
				List<Concept> groups = groupsByDoseFormUuid.get(doseForm.getUuid());
				if (groups == null) {
					groups = new ArrayList<Concept>();
					groupsByDoseFormUuid.put(doseForm.getUuid(), groups);
				}
				groups.add(doseFormGroup);
			}
		}
		return groupsByDoseFormUuid;
	}
	
	/**
	 * The routes of administration a dose form group is mapped to. A route reference term that no
	 * non-retired concept claims via a SAME-AS mapping is skipped rather than failing the lookup.
	 */
	private List<Concept> getRoutesOfDoseFormGroup(Concept doseFormGroup) {
		List<Concept> routes = new ArrayList<Concept>();
		for (ConceptMap conceptMap : doseFormGroup.getConceptMappings()) {
			if (EmrApiConstants.ROUTE_OF_ADMINISTRATION_CONCEPT_MAP_TYPE_UUID
			        .equals(conceptMap.getConceptMapType().getUuid())) {
				ConceptReferenceTerm term = conceptMap.getConceptReferenceTerm();
				Concept route = getRouteConcept(term);
				if (route == null) {
					log.warn("No non-retired concept has a SAME-AS mapping to reference term '"
					        + term.getConceptSource().getName() + ":" + term.getCode()
					        + "', which is mapped as a route of administration of dose form group " + doseFormGroup.getUuid()
					        + "; omitting it from that group's routes");
				} else {
					routes.add(route);
				}
			}
		}
		return routes;
	}
	
	/**
	 * The non-retired concept a route of administration reference term stands for, or null if there is
	 * none.
	 */
	private Concept getRouteConcept(ConceptReferenceTerm term) {
		for (Concept candidate : getConceptsSameAs(term)) {
			if (!candidate.getRetired()) {
				return candidate;
			}
		}
		return null;
	}
	
	/**
	 * The non-retired concepts of the given concept class, or an empty list if no such concept class
	 * exists, which is the case on a dictionary that has not imported the dose form group metadata.
	 * ConceptService.getConceptsByClass() does not filter on retired, so that is done here: a retired
	 * dose form must not be offered to a prescriber.
	 */
	private List<Concept> getConceptsByClassName(String conceptClassName) {
		ConceptClass conceptClass = conceptService.getConceptClassByName(conceptClassName);
		if (conceptClass == null) {
			log.warn("No concept class named '" + conceptClassName + "'; returning no concepts for it");
			return Collections.emptyList();
		}
		
		List<Concept> concepts = new ArrayList<Concept>();
		for (Concept concept : conceptService.getConceptsByClass(conceptClass)) {
			if (!concept.getRetired()) {
				concepts.add(concept);
			}
		}
		return concepts;
	}
	
}
