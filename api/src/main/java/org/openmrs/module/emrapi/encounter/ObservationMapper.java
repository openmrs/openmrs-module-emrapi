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

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.mapper.UserMapper;

import java.text.SimpleDateFormat;

public class ObservationMapper {
	
	private final ConceptMapper conceptMapper;
	
	private final DrugMapper drugMapper;
	
	private final UserMapper userMapper;
	
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
		observation.setFormNamespace(obs.getFormFieldNamespace());
		observation.setFormFieldPath(obs.getFormFieldPath());
		
		Obs.Interpretation obsInterpretation = obs.getInterpretation();
		Obs.Status obsStatus = obs.getStatus();
		
		String interpretation = (obsInterpretation != null) ? obsInterpretation.name() : null;
		String status = (obsStatus != null) ? obsStatus.name() : null;
		
		observation.setInterpretation(interpretation);
		observation.setStatus(status);
		
		return observation;
	}
	
	private Object getValue(Obs obs, Concept concept) {
		if (concept.getDatatype().isNumeric())
			return obs.getValueNumeric();
		if (concept.getDatatype().isCoded()) {
			Drug valueDrug = obs.getValueDrug();
			return valueDrug == null ? conceptMapper.map(obs.getValueCoded()) : drugMapper.map(valueDrug);
		}
		if (concept.getDatatype().isBoolean())
			return obs.getValueBoolean();
		// TODO: Remove this once openmrs date format issue is fixed
		// https://tickets.openmrs.org/browse/TRUNK-4280
		if (concept.getDatatype().isDate())
			return getDateString(obs);
		if (concept.getDatatype().isDateTime())
			return getDatetimeString(obs);
		else
			return obs.getValueAsString(Context.getLocale());
	}
	
	private String getDateString(Obs obs) {
		return obs.getValueDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(obs.getValueDate()) : null;
	}
	
	private String getDatetimeString(Obs obs) {
		return obs.getValueDatetime() != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(obs.getValueDatetime())
		        : null;
	}
}
