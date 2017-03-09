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
package org.openmrs.module.emrapi.conditionslist.db;

import java.util.List;

import org.openmrs.Condition;
import org.openmrs.Patient;

public interface ConditionDAO {
	
	Condition saveOrUpdate(Condition condition);
	
	Condition getConditionByUuid(String uuid);
	
	List<Condition> getConditionHistory(Patient patient);
	
	List<Condition> getActiveConditions(Patient patient);
}
