/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
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
 * @see org.openmrs.module.emrapi.encounter.matcher.BaseEncounterMatcher
 * </pre>
 */
public interface EmrEncounterService extends OpenmrsService {

    EncounterTransaction save(EncounterTransaction encounterTransaction);

    List<EncounterTransaction> find(EncounterSearchParameters encounterSearchParameters);

    EncounterTransaction getActiveEncounter(ActiveEncounterParameters activeEncounterParameters);

    EncounterTransaction getEncounterTransaction(String uuid, Boolean includeAll);
}
