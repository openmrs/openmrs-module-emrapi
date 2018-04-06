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
import java.util.List;

import org.openmrs.Condition;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.conditionslist.ConditionService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;


public class ConditionServiceImpl extends BaseOpenmrsService implements ConditionService {

	public ConditionServiceImpl() {}

     private List<Condition> convert(List<org.openmrs.Condition> coreConditions) {
         List<Condition> conditions = new ArrayList<Condition>();
         for (org.openmrs.Condition coreCondition : coreConditions) {
             Condition condition = convert(coreCondition);
             conditions.add(condition);
         }
         return conditions;
     }

    private Condition convert(Condition coreCondition) {
        Condition condition = new Condition();
        condition.setCondition(coreCondition.getCondition());
        condition.setConditionId(coreCondition.getConditionId());
        condition.setClinicalStatus(coreCondition.getClinicalStatus());
        condition.setVerificationStatus(coreCondition.getVerificationStatus());
        condition.setPreviousVersion(coreCondition.getPreviousVersion());
        condition.setOnsetDate(coreCondition.getOnsetDate());
        condition.setEndDate(coreCondition.getEndDate());
        condition.setAdditionalDetail(coreCondition.getAdditionalDetail());
        condition.setVoided(coreCondition.getVoided());
        condition.setVoidedBy(coreCondition.getVoidedBy());
        condition.setVoidReason(coreCondition.getVoidReason());

        return condition;
    }

    @Override
    public Condition save(Condition condition) {
	    return  convert(Context.getConditionService().saveCondition(condition));
    }

    @Override
    public Condition voidCondition(Condition condition, String voidReason) {
	    return convert(Context.getConditionService().voidCondition(condition, voidReason));
    }

    @Override
    public Condition getConditionByUuid(String uuid) {
	    return convert(Context.getConditionService().getConditionByUuid(uuid));
    }

    @Override
    public List<Condition> getActiveConditions(Patient patient) {
        return convert(Context.getConditionService().getActiveConditions(patient));
    }

    @Override
    public Condition getCondition(Integer conditionId) {
        return convert(Context.getConditionService().getCondition(conditionId));
    }

    @Override
    public Condition unvoidCondition(Condition condition) {
        return convert(Context.getConditionService().unvoidCondition(condition));
    }

    @Override
    public void purgeCondition(Condition condition) {
         Context.getConditionService().purgeCondition(condition);
    }

}
