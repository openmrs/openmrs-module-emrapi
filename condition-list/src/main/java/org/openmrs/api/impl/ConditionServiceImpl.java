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
package org.openmrs.api.impl;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Concept;
import org.openmrs.Condition;
import org.openmrs.ConditionHistory;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ConditionService;
import org.openmrs.api.db.ConditionDAO;
import org.openmrs.api.util.ConditionListConstants;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


public class ConditionServiceImpl extends BaseOpenmrsService implements ConditionService {

    private ConditionDAO conditionDAO;
    private ConceptService conceptService;
    private AdministrationService administrationService;

    public ConditionServiceImpl(ConditionDAO conditionDAO, ConceptService conceptService, AdministrationService administrationService) {
        this.conditionDAO = conditionDAO;
        this.conceptService = conceptService;
        this.administrationService = administrationService;
    }

    @Override
    public Condition save(Condition condition) {
        if (condition.getEndReason() != null && condition.getEndDate() == null) {
            condition.setEndDate(new Date());
        }
        return conditionDAO.saveOrUpdate(condition);
    }

    public List<ConditionHistory> getConditionHistory(Patient patient) {
        List<Condition> conditionList = conditionDAO.getConditionHistory(patient);
        List<ConditionHistory> conditionHistoryList = new ArrayList<ConditionHistory>();

        List<Condition> groupedConditions = new ArrayList<Condition>();

        if (CollectionUtils.isEmpty(conditionList)) return new ArrayList<ConditionHistory>();

        ConditionHistory conditionhistory = createConditionHistory(conditionList.get(0));
        groupedConditions.add(conditionList.get(0));
        for (int i = 1; i < conditionList.size(); i++) {
            Condition condition = conditionList.get(i);
            if (condition.getConcept().equals(conditionhistory.getCondition())) {
                groupedConditions.add(condition);
            } else {
                conditionhistory.setConditions(groupedConditions);
                conditionHistoryList.add(conditionhistory);
                conditionhistory = createConditionHistory(condition);
                groupedConditions = new ArrayList<Condition>();
                groupedConditions.add(condition);
            }
        }
        if (CollectionUtils.isNotEmpty(groupedConditions)) {
            updateConditionHistoryList(conditionHistoryList, groupedConditions, conditionhistory);
        }
        return conditionHistoryList;
    }

    private void updateConditionHistoryList(List<ConditionHistory> conditionHistoryList, List<Condition> groupedConditions, ConditionHistory conditionhistory) {
        conditionhistory.setConditions(groupedConditions);
        conditionHistoryList.add(conditionhistory);
    }

    private ConditionHistory createConditionHistory(Condition condition) {
        ConditionHistory conditionHistory = new ConditionHistory();
        conditionHistory.setCondition(condition.getConcept());
        if (condition.getConcept().getUuid().equals(administrationService.getGlobalProperty(ConditionListConstants.GLOBAL_PROPERTY_NON_CODED_UUID))) {
            conditionHistory.setNonCodedCondition(condition.getConditionNonCoded());
        }
        return conditionHistory;
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
