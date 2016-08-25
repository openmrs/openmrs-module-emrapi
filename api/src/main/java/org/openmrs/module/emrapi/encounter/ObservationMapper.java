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
import org.openmrs.Drug;
import org.openmrs.Obs;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;

@Component("observationMapper")
@OpenmrsProfile(openmrsVersion = "[1.9.* - 1.11.*]")
public class ObservationMapper {
    private ConceptMapper conceptMapper;
    private DrugMapper drugMapper;
    private UserMapper userMapper;

    @Autowired(required = false)
    public ObservationMapper(ConceptMapper conceptMapper, DrugMapper drugMapper, UserMapper userMapper) {
        this.conceptMapper = conceptMapper;
        this.drugMapper = drugMapper;
        this.userMapper = userMapper;
    }

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
        observation.setComment(obs.getComment());
        observation.setCreator(userMapper.map(obs.getCreator()));
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
        if (concept.getDatatype().isCoded()) {
            Drug valueDrug = obs.getValueDrug();
            return valueDrug == null ? conceptMapper.map(obs.getValueCoded()) : drugMapper.map(valueDrug);
        }
        if (concept.getDatatype().isBoolean()) return obs.getValueBoolean();
        // TODO: Remove this once openmrs date format issue is fixed
        // https://tickets.openmrs.org/browse/TRUNK-4280
        if (concept.getDatatype().isDate()) return getDateString(obs);
        if (concept.getDatatype().isDateTime()) return getDatetimeString(obs);
        else return obs.getValueAsString(Context.getLocale());
    }

    private String getDateString(Obs obs) {
        return obs.getValueDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(obs.getValueDate()) : null;
    }

    private String getDatetimeString(Obs obs) {
        return obs.getValueDatetime() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(obs.getValueDatetime()) : null;
    }
}