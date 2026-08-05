/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.web.controller.types.DoseFormGroupRoutes;
import org.openmrs.module.emrapi.web.controller.types.DoseFormGroupsResponse;
import org.openmrs.module.emrapi.web.controller.types.DoseFormMembership;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/**/emrapi/doseFormGroups")
public class DoseFormGroupController {

	private static final String DRUG_FORM_CONCEPT_CLASS_NAME = "Drug form";

	private static final String DOSE_FORM_GROUP_CONCEPT_CLASS_NAME = "Dose Form Group";

	/**
	 * Returns a {@link DoseFormGroupsResponse}, which contains mappings for: - dose form to its dose
	 * form group, which is null for a dose form that belongs to no group - dose form group to its
	 * routes of administration
	 *
	 * @param request the current request, used to resolve the requested representation
	 * @param response the current response
	 * @return the dose forms and dose form groups, converted to the requested representation
	 * @throws APIException if a dose form belongs to more than one dose form group
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Object getDoseFormGroups(HttpServletRequest request, HttpServletResponse response) {
		RequestContext context = RestUtil.getRequestContext(request, response, Representation.DEFAULT);

		ConceptService cs = Context.getConceptService();
		ConceptClass doseFormGroupClass = cs.getConceptClassByName(DOSE_FORM_GROUP_CONCEPT_CLASS_NAME);
		List<Concept> doseFormGroups = cs.getConceptsByClass(doseFormGroupClass);

		List<DoseFormGroupRoutes> doseFormGroupRoutes = new ArrayList<>();
		// A dose form may belong to at most one dose form group; the concept set membership does not
		// enforce this, so track the claiming group and fail if a second group claims the same form
		Map<String, Concept> claimingGroupByDoseFormUuid = new HashMap<>();

		for (Concept doseFormGroup : doseFormGroups) {
			for (Concept doseForm : doseFormGroup.getSetMembers()) {
				Concept claimingGroup = claimingGroupByDoseFormUuid.put(doseForm.getUuid(), doseFormGroup);
				if (claimingGroup != null) {
					throw new APIException("Dose form '" + displayOf(doseForm) + "' (" + doseForm.getUuid()
					        + ") belongs to more than one dose form group: '" + displayOf(claimingGroup) + "' and '"
					        + displayOf(doseFormGroup) + "'. A dose form may belong to at most one group.");
				}
			}

			doseFormGroupRoutes.add(new DoseFormGroupRoutes(doseFormGroup, getRoutesOfAdministration(cs, doseFormGroup)));
		}

		// Every dose form is reported, whether or not a group claims it. The dose forms are enumerated
		// from their concept class rather than from group membership so that a dose form belonging to no
		// group is still returned, with a null dose form group.
		ConceptClass doseFormClass = cs.getConceptClassByName(DRUG_FORM_CONCEPT_CLASS_NAME);
		List<DoseFormMembership> doseFormMemberships = new ArrayList<>();
		for (Concept doseForm : cs.getConceptsByClass(doseFormClass)) {
			doseFormMemberships.add(new DoseFormMembership(doseForm, claimingGroupByDoseFormUuid.get(doseForm.getUuid())));
		}

		DoseFormGroupsResponse result = new DoseFormGroupsResponse(doseFormMemberships, doseFormGroupRoutes);
		return ConversionUtil.convertToRepresentation(result, context.getRepresentation());
	}

	/**
	 * Returns the routes of administration mapped to the given dose form groupt add.
	 */
	private List<Concept> getRoutesOfAdministration(ConceptService cs, Concept doseFormGroup) {
		List<Concept> routes = new ArrayList<>();
		for (ConceptMap conceptMap : doseFormGroup.getConceptMappings()) {
			ConceptMapType mapType = conceptMap.getConceptMapType();
			if (EmrApiConstants.ROUTE_OF_ADMINISTRATION_CONCEPT_MAP_TYPE_UUID.equals(mapType.getUuid())) {
				ConceptReferenceTerm term = conceptMap.getConceptReferenceTerm();
				Concept route = getConceptBySameAsMapping(cs, term);
				if (route != null) {
					routes.add(route);
				}
			}
		}
		return routes;
	}

	/**
	 * Retrieves a concept by ConceptReferenceTerm (conceptSource + code) with SAME-AS mapping. This
	 * function is similar to ConceptService.getConceptByMapping(). However, that function does not
	 * check for the mapping type (we need the SAME-AS mapping), and throws an exception if the
	 * ConceptReferenceTerm has multiple mappings, even if they are of different mapping types.
	 */
	private Concept getConceptBySameAsMapping(ConceptService cs, ConceptReferenceTerm term) {
		List<Concept> candidates = cs.getConceptsByMapping(term.getCode(), term.getConceptSource().getName(), false);
		for (Concept candidate : candidates) {
			for (ConceptMap candidateMap : candidate.getConceptMappings()) {
				if (EmrApiConstants.SAME_AS_CONCEPT_MAP_TYPE_UUID.equals(candidateMap.getConceptMapType().getUuid())
				        && term.getUuid().equals(candidateMap.getConceptReferenceTerm().getUuid())) {
					return candidate;
				}
			}
		}
		return null;
	}

	private String displayOf(Concept concept) {
		return concept.getName() == null ? null : concept.getName().getName();
	}
}
