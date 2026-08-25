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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.web.controller.types.DoseFormGroupsResponse;
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
	
	/**
	 * Exposes {@link EmrConceptService#getDoseFormMemberships()} and
	 * {@link EmrConceptService#getDoseFormGroupRoutes()} as a single payload: which dose form groups
	 * each dose form belongs to, and which routes of administration each dose form group has. A dose
	 * form may belong to more than one dose form group, so a client that wants the routes of
	 * administration of a dose form unions the routes of all the groups it belongs to.
	 *
	 * @param request the current request, used to resolve the requested representation
	 * @param response the current response
	 * @return the dose forms and dose form groups, converted to the requested representation
	 */
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Object getDoseFormGroups(HttpServletRequest request, HttpServletResponse response) {
		RequestContext context = RestUtil.getRequestContext(request, response, Representation.REF);
		
		EmrConceptService emrConceptService = Context.getService(EmrConceptService.class);
		DoseFormGroupsResponse result = new DoseFormGroupsResponse(emrConceptService.getDoseFormMemberships(),
		        emrConceptService.getDoseFormGroupRoutes());
		
		return ConversionUtil.convertToRepresentation(result, context.getRepresentation());
	}
}
