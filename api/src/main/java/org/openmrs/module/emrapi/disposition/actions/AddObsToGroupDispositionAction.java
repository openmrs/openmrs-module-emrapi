/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.disposition.actions;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component("addObsToGroupDispositionAction")
public class AddObsToGroupDispositionAction implements DispositionAction {
	
	private static final String CONCEPT = "concept";
	
	private static final String VALUE = "valueCoded";
	
	@Autowired
	private EmrConceptService emrConceptService;
	
	@Override
	public void action(EncounterDomainWrapper encounterDomainWrapper, Obs dispositionObsGroupBeingCreated,
	        Map<String, String[]> requestParameters) {
		String concept = DispositionActionUtils.getSingleRequiredParameter(requestParameters, CONCEPT);
		String value = DispositionActionUtils.getSingleRequiredParameter(requestParameters, VALUE);
		
		Concept question = emrConceptService.getConcept(concept);
		Concept answer = emrConceptService.getConcept(value);
		
		Obs obs = createObs(question, answer);
		
		dispositionObsGroupBeingCreated.addGroupMember(obs);
	}
	
	private Obs createObs(Concept question, Concept answer) {
		Obs obs = new Obs();
		obs.setConcept(question);
		obs.setValueCoded(answer);
		return obs;
	}
	
}
