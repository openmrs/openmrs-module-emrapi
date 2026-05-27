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

import org.openmrs.module.emrapi.encounter.ActiveEncounterParameters;
import org.openmrs.module.emrapi.encounter.EmrEncounterService;
import org.openmrs.module.emrapi.encounter.EncounterSearchParameters;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/**/emrapi/encounter")
public class EmrEncounterController extends BaseRestController {
	
	@Autowired
	private EmrEncounterService emrEncounterService;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	@Transactional
	public EncounterTransaction update(@RequestBody EncounterTransaction encounterTransaction) {
		return emrEncounterService.save(encounterTransaction);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/active")
	@ResponseBody
	public EncounterTransaction getActiveEncounter(
	        @ModelAttribute("activeEncounterParameters") ActiveEncounterParameters activeEncounterParameters) {
		return emrEncounterService.getActiveEncounter(activeEncounterParameters);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{uuid}")
	@ResponseBody
	public EncounterTransaction get(@PathVariable("uuid") String uuid,
	        @RequestParam(value = "includeAll", required = false) Boolean includeAll) {
		return emrEncounterService.getEncounterTransaction(uuid, includeAll);
	}
	
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public List<EncounterTransaction> find(@RequestParam(required = false) List<String> visitUuids,
	        @RequestParam String patientUuid, @RequestParam(required = false) List<String> visitTypeUuids,
	        @RequestParam(required = false) Date encounterDateTimeFrom,
	        @RequestParam(required = false) Date encounterDateTimeTo,
	        @RequestParam(required = false) List<String> providerUuids,
	        @RequestParam(required = false) List<String> encounterTypeUuids,
	        @RequestParam(required = false) String locationUuid, @RequestParam Boolean includeAll) {
		EncounterSearchParameters encounterSearchParameters = new EncounterSearchParameters(visitUuids, patientUuid,
		        visitTypeUuids, encounterDateTimeFrom, encounterDateTimeTo, providerUuids, encounterTypeUuids, locationUuid,
		        includeAll);
		return emrEncounterService.find(encounterSearchParameters);
	}
}
