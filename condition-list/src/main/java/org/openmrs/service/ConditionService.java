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
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.Condition;
import org.openmrs.util.PrivilegeConstants;

import java.util.Date;
import java.util.List;

public interface ConditionService extends OpenmrsService {
    @Authorized({PrivilegeConstants.EDIT_CONDITIONS})
    Condition save(Condition condition);

    @Authorized({PrivilegeConstants.EDIT_CONDITIONS})
    Condition voidCondition(Condition condition, String voidReason);

    @Authorized({PrivilegeConstants.EDIT_CONDITIONS})
    Condition endCondition(Condition condition, Date endDate, Concept endReason);

    Condition getConditionByUuid(String uuid);

    List<Condition> getConditionsByPatient(Patient patient);

    List<Concept> getEndReasonConcepts();
}
