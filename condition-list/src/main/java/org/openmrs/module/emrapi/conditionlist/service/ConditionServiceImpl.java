package org.openmrs.module.emrapi.conditionlist.service;
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

import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.conditionlist.dao.ConditionDao;
import org.openmrs.module.emrapi.conditionlist.domain.Condition;
import org.openmrs.module.emrapi.conditionlist.util.ConditionListConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class ConditionServiceImpl implements ConditionService {

    @Autowired
    private ConditionDao conditionDao;

    public ConditionDao getConditionDao() {
        return conditionDao;
    }

    public void setConditionDao(ConditionDao conditionDao) {
        this.conditionDao = conditionDao;
    }

    @Override
    public Condition save(Condition condition) {
        String nonCodedConditionUuid = Context.getAdministrationService().getGlobalProperty(ConditionListConstants.GLOBAL_PROPERTY_NON_CODED_UUID);
        if (condition.getConditionNonCoded() != null) {
            if (!condition.getConcept().getUuid().equals(nonCodedConditionUuid)) {
                throw new APIException("Non-coded value not supported for coded condition");
            }
        } else {
            if (condition.getConcept().getUuid().equals(nonCodedConditionUuid)) {
                throw new APIException("Non-coded value needed for non-coded condition");
            }
        }
        Condition existingCondition = getConditionByUuid(condition.getUuid());
        if (existingCondition != null && !condition.getConcept().equals(existingCondition.getConcept())) {
            throw new APIException("Concept cannot be updated");
        }
        List<Condition> conditionsForPatient = getConditions(condition.getPatient());
        if (condition.getConditionNonCoded() != null) {
            for (Condition eachCondition : conditionsForPatient) {
                if (eachCondition.getConcept().equals(condition.getConcept())
                        && eachCondition.getConditionNonCoded().equalsIgnoreCase(condition.getConditionNonCoded().replaceAll("\\s", "")) && !eachCondition.getUuid().equals(condition.getUuid())) {
                    throw new APIException("Duplicates are not allowed");
                }
            }
        }
        return conditionDao.saveOrUpdate(condition);
    }

    @Override
    public List<Condition> getConditions(Patient patient) {
        return conditionDao.getConditions(patient);
    }

    @Override
    public Condition getConditionByUuid(String uuid) {
        return conditionDao.getConditionByUuid(uuid);
    }
}
