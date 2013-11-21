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
import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Locale;

public class ObservationMapper {
    private final ConceptMapper conceptMapper = new ConceptMapper();

    public EncounterTransaction.Observation map(Obs obs) {
        Concept concept = obs.getConcept();
        ConceptDatatype dataType = concept.getDatatype();
        Object value = dataType.isNumeric() ? obs.getValueNumeric() : obs.getValueAsString(Locale.getDefault());
        EncounterTransaction.Observation observation = new EncounterTransaction.Observation();
        observation.setConcept(conceptMapper.map(concept));
        observation.setValue(value);
        if(obs.getGroupMembers() != null) {
            for (Obs obsGroupMember : obs.getGroupMembers()) {
                observation.addGroupMember(map(obsGroupMember));
            }
        }
        return observation;
    }
}