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

import java.text.SimpleDateFormat;

public class ObservationMapper {
    private final ConceptMapper conceptMapper = new ConceptMapper();

    public EncounterTransaction.Observation map(Obs obs) {
        Concept concept = obs.getConcept();
        Object value = getValue(obs, concept);
        EncounterTransaction.Observation observation = new EncounterTransaction.Observation();
        observation.setUuid(obs.getUuid());
        observation.setConcept(conceptMapper.map(concept));
        observation.setValue(value);
        observation.setVoided(obs.getVoided());
        observation.setVoidReason(obs.getVoidReason());
        observation.setObservationDateTime(obs.getObsDatetime());
        if (obs.getOrder() != null) {
            observation.setOrderUuid(obs.getOrder().getUuid());
        }
        if (obs.getGroupMembers() != null) {
            for (Obs obsGroupMember : obs.getGroupMembers()) {
                observation.addGroupMember(map(obsGroupMember));
            }
        }
        return observation;
    }

    private Object getValue(Obs obs, Concept concept) {
        if (concept.getDatatype().isNumeric()) return obs.getValueNumeric();
        if (concept.getDatatype().isCoded()) return conceptMapper.map(obs.getValueCoded());
        // TODO: Remove this once openmrs date format issue is fixed
        // https://tickets.openmrs.org/browse/TRUNK-4280
        if (concept.getDatatype().isDate()) return getDateString(obs);
        else return obs.getValueAsString(Context.getLocale());
    }

    private String getDateString(Obs obs) {
        return obs.getValueDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(obs.getValueDate()) : null;
    }
}