/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.visit;

import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.visit.contract.VisitResponse;

public class VisitResponseMapper {
	
	private EncounterTransactionMapper encounterTransactionMapper;
	
	public VisitResponseMapper(EncounterTransactionMapper encounterTransactionMapper) {
		this.encounterTransactionMapper = encounterTransactionMapper;
	}
	
	public VisitResponse map(Visit visit) {
		if (visit == null)
			return null;
		VisitResponse visitResponse = new VisitResponse(visit.getUuid());
		for (Encounter encounter : visit.getEncounters()) {
			visitResponse.addEncounter(encounterTransactionMapper.map(encounter, true));
		}
		return visitResponse;
	}
}
