package org.openmrs.module.emrapi.conditionslist.contract;

import java.util.List;

public class ConditionHistory {
	
	private String conditionNonCoded;
	
	private String conceptUuid;
	
	private List<Condition> conditions;
	
	public String getConditionNonCoded() {
		return conditionNonCoded;
	}
	
	public void setConditionNonCoded(String conditionNonCoded) {
		this.conditionNonCoded = conditionNonCoded;
	}
	
	public String getConceptUuid() {
		return conceptUuid;
	}
	
	public void setConceptUuid(String conceptUuid) {
		this.conceptUuid = conceptUuid;
	}
	
	public List<Condition> getConditions() {
		return conditions;
	}
	
	public void setConditions(List<Condition> conditions) {
		this.conditions = conditions;
	}
}
