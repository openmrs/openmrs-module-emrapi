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
package org.openmrs.module.emrapi.conditionslist;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.openmrs.Concept;

public class ConditionHistory {
	
	private String nonCodedCondition;
	
	private Concept condition;
	
	private List<Condition> conditions;
	
	public String getNonCodedCondition() {
		return nonCodedCondition;
	}
	
	public void setNonCodedCondition(String nonCodedCondition) {
		this.nonCodedCondition = nonCodedCondition;
	}
	
	public Concept getCondition() {
		return condition;
	}
	
	public void setCondition(Concept condition) {
		this.condition = condition;
	}
	
	public List<Condition> getConditions() {
		return conditions;
	}
	
	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}
	
	@Override
	public String toString() {
		String name = nonCodedCondition;
		if (name != null && condition != null && condition.getName() != null) {
			name = condition.getName().getName();
		}
		
		return new ToStringBuilder(this).append("condition", name).append("count", conditions.size()).build();
	}
}
