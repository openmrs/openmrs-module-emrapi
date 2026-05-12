/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.encounter.matcher;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.encounter.EncounterParameters;

/**
 * Find {@link org.openmrs.Encounter} from a {@link org.openmrs.Visit} by matching
 * {@link org.openmrs.EncounterType}.
 */
public class DefaultEncounterMatcher implements BaseEncounterMatcher {
	
	@Override
	public Encounter findEncounter(Visit visit, EncounterParameters encounterParameters) {
		EncounterType encounterType = encounterParameters.getEncounterType();
		String encounterUuid = encounterParameters.getEncounterUuid();
		
		if (encounterType == null && StringUtils.isBlank(encounterUuid)) {
			throw new IllegalArgumentException("Encounter Type or Encounter Uuid must be specified");
		}
		
		if (visit.getEncounters() != null) {
			for (Encounter encounter : visit.getEncounters()) {
				if (!encounter.isVoided() && (encounter.getUuid().equals(encounterUuid)
				        || encounter.getEncounterType().equals(encounterType))) {
					return encounter;
				}
			}
		}
		return null;
	}
}
