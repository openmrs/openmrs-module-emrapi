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
package org.openmrs.module.emrapi.conditionlist.service;

import org.openmrs.Patient;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.conditionlist.dao.ConditionDAO;
import org.openmrs.module.emrapi.conditionlist.domain.Condition;
import org.springframework.util.StringUtils;

import java.util.List;


public class ConditionServiceImpl extends BaseOpenmrsService implements ConditionService {

    private ConditionDAO conditionDao;

    public ConditionServiceImpl(ConditionDAO conditionDao) {
        this.conditionDao = conditionDao;
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
}
