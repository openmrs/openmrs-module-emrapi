/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.encounter;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.List;

/**
 * <pre>
 * Handy service to create/update an {@link org.openmrs.Encounter}. Use this to add {@link org.openmrs.Obs}, {@link org.openmrs.Order} to an Encounter.
 * The encounter is saved against the latest active visit of the {@link org.openmrs.Patient} if one exists, else a new visit is created.
 *
 * A strategy can be specified to choose which Encounter to update
 * &#64;see org.openmrs.module.emrapi.encounter.matcher.BaseEncounterMatcher
 * </pre>
 */
public interface EmrEncounterService extends OpenmrsService {
	
	EncounterTransaction save(EncounterTransaction encounterTransaction);
	
	List<EncounterTransaction> find(EncounterSearchParameters encounterSearchParameters);
	
	EncounterTransaction getActiveEncounter(ActiveEncounterParameters activeEncounterParameters);
	
	EncounterTransaction getEncounterTransaction(String uuid, Boolean includeAll);
}
