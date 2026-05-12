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

import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.encounter.matcher.BaseEncounterMatcher;
import org.openmrs.module.emrapi.encounter.EncounterParameters;
import org.springframework.stereotype.Component;

@Component
public class CustomMatcher implements BaseEncounterMatcher {
	
	@Override
	public Encounter findEncounter(Visit visit, EncounterParameters encounterParameters) {
		for (Encounter encounter : visit.getEncounters()) {
			if (!encounter.isVoided() && encounter.getUuid().equals("f13d6fae-baa9-4553-955d-920098bec08g")) {
				return encounter;
			}
		}
		return null;
	}
}
