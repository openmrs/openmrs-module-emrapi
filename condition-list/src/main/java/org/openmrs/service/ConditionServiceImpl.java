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
package org.openmrs.service;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.dao.ConditionDAO;
import org.openmrs.Condition;
import org.openmrs.util.ConditionListConstants;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;


public class ConditionServiceImpl extends BaseOpenmrsService implements ConditionService {

    private ConditionDAO conditionDao;
    private ConceptService conceptService;
    private AdministrationService administrationService;

    public ConditionServiceImpl(ConditionDAO conditionDao, ConceptService conceptService, AdministrationService administrationService) {
        this.conditionDao = conditionDao;
        this.conceptService = conceptService;
        this.administrationService = administrationService;
    }

    @Override
    public Condition save(Condition condition) {
        return conditionDao.saveOrUpdate(condition);
    }

    @Override
    public List<Condition> getConditionsByPatient(Patient patient) {
        return conditionDao.getConditionsByPatient(patient);
    }

    @Override
    public Condition voidCondition(Condition condition, String voidReason) {
        if (!StringUtils.hasLength(voidReason)) {
            throw new IllegalArgumentException("voidReason cannot be empty or null");
        }
        return conditionDao.saveOrUpdate(condition);
    }

    @Override
    public Condition getConditionByUuid(String uuid) {
        return conditionDao.getConditionByUuid(uuid);
    }

    @Override
    public Condition endCondition(Condition condition, Date endDate, Concept endReason) {
        if (endDate == null) {
            endDate = new Date();
        }
        condition.setEndDate(endDate);
        condition.setEndReason(endReason);
        return conditionDao.saveOrUpdate(condition);
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
