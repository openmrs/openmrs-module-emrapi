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
        if(visit == null) return null;
        VisitResponse visitResponse = new VisitResponse(visit.getUuid());
        for (Encounter encounter : visit.getEncounters()) {
            visitResponse.addEncounter(encounterTransactionMapper.map(encounter, true));
        }
        return visitResponse;
    }
}
