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

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public class ObservationMapper {
    private final ConceptMapper conceptMapper = new ConceptMapper();

    public EncounterTransaction.Observation map(Obs obs) {
        Concept concept = obs.getConcept();
        Object value = concept.getDatatype().isNumeric() ? obs.getValueNumeric() : obs.getValueAsString(Context.getLocale());
        EncounterTransaction.Observation observation = new EncounterTransaction.Observation();
        observation.setUuid(obs.getUuid());
        observation.setConcept(conceptMapper.map(concept));
        observation.setValue(value);
        observation.setVoided(obs.getVoided());
        observation.setVoidReason(obs.getVoidReason());
        observation.setObservationDateTime(obs.getObsDatetime());
        if(obs.getGroupMembers() != null) {
            for (Obs obsGroupMember : obs.getGroupMembers()) {
                observation.addGroupMember(map(obsGroupMember));
            }
        }
        return observation;
    }
}