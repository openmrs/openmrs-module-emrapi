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
package org.openmrs.module.emrapi.conditionslist.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Condition;
import org.openmrs.ConditionHistory;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.conditionslist.ConditionListConstants;
import org.openmrs.module.emrapi.conditionslist.ConditionService;
import org.openmrs.module.emrapi.conditionslist.db.ConditionDAO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

public class ConditionServiceImpl extends BaseOpenmrsService implements ConditionService {
	
	private ConditionDAO conditionDAO;
	
	private ConceptService conceptService;
	
	private AdministrationService administrationService;
	
	public ConditionServiceImpl(ConditionDAO conditionDAO, ConceptService conceptService,
	                            AdministrationService administrationService) {
		this.conditionDAO = conditionDAO;
		this.conceptService = conceptService;
		this.administrationService = administrationService;
	}
	
	@Override
	public Condition save(Condition condition) {
		Date endDate = condition.getEndDate() != null ? condition.getEndDate() : new Date();
		if (condition.getEndReason() != null) {
			condition.setEndDate(endDate);
		}
		Condition existingCondition = getConditionByUuid(condition.getUuid());
		if (condition.equals(existingCondition)) {
			return existingCondition;
		}
		if (existingCondition == null) {
			return conditionDAO.saveOrUpdate(condition);
		}
		condition = Condition.newInstance(condition);
		condition.setPreviousCondition(existingCondition);
		if (existingCondition.getStatus().equals(condition.getStatus())) {
			existingCondition.setVoided(true);
			conditionDAO.saveOrUpdate(existingCondition);
			return conditionDAO.saveOrUpdate(condition);
		}
		Date onSetDate = condition.getOnsetDate() != null ? condition.getOnsetDate() : new Date();
		existingCondition.setEndDate(onSetDate);
		conditionDAO.saveOrUpdate(existingCondition);
		condition.setOnsetDate(onSetDate);
		return conditionDAO.saveOrUpdate(condition);
	}
	
	public List<ConditionHistory> getConditionHistory(Patient patient) {
		List<Condition> conditionList = conditionDAO.getConditionHistory(patient);
		Map<String, ConditionHistory> allConditions = new LinkedHashMap<String, ConditionHistory>();
		for (Condition condition : conditionList) {
			Concept concept = condition.getConcept();
			
			String nonCodedConceptUuid = administrationService.getGlobalProperty(
					ConditionListConstants.GLOBAL_PROPERTY_NON_CODED_UUID);
			
			String key = concept.getUuid().equals(nonCodedConceptUuid) ?
					condition.getConditionNonCoded() :
					concept.getUuid();
			ConditionHistory conditionHistory = allConditions.get(key);
			if (conditionHistory != null) {
				conditionHistory.getConditions().add(condition);
			} else {
				conditionHistory = new ConditionHistory();
				List<Condition> conditions = new ArrayList<Condition>();
				conditions.add(condition);
				conditionHistory.setConditions(conditions);
				conditionHistory.setCondition(condition.getConcept());
				if (concept.getUuid().equals(nonCodedConceptUuid)) {
					conditionHistory.setNonCodedCondition(condition.getConditionNonCoded());
				}
			}
			allConditions.put(key, conditionHistory);
		}
		return new ArrayList<ConditionHistory>(allConditions.values());
	}
	
	@Override
	public Condition voidCondition(Condition condition, String voidReason) {
		if (!StringUtils.hasLength(voidReason)) {
			throw new IllegalArgumentException("voidReason cannot be empty or null");
		}
		return conditionDAO.saveOrUpdate(condition);
	}
	
	@Override
	public Condition getConditionByUuid(String uuid) {
		return conditionDAO.getConditionByUuid(uuid);
	}
	
	@Override
	public List<Condition> getActiveConditions(Patient patient) {
		return conditionDAO.getActiveConditions(patient);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Concept> getEndReasonConcepts() {
		return getSetMembersOfConceptSetFromGP(ConditionListConstants.GP_END_REASON_CONCEPT_SET_UUID);
	}
	
	private List<Concept> getSetMembersOfConceptSetFromGP(String globalProperty) {
		String conceptUuid = administrationService.getGlobalProperty(globalProperty);
		Concept concept = conceptService.getConceptByUuid(conceptUuid);
		if (concept != null && concept.isSet()) {
			return concept.getSetMembers();
		}
		return Collections.emptyList();
	}
}
