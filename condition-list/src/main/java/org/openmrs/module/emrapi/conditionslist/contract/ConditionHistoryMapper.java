package org.openmrs.module.emrapi.conditionslist.contract;

import java.util.ArrayList;
import java.util.List;

public class ConditionHistoryMapper {
	
	private ConditionMapper conditionMapper;
	
	public ConditionHistoryMapper(ConditionMapper conditionMapper) {
		this.conditionMapper = conditionMapper;
	}
	
	public ConditionHistory map(org.openmrs.ConditionHistory conditionHistory) {
		ConditionHistory conditionHistoryContract = new ConditionHistory();
		conditionHistoryContract.setConceptUuid(conditionHistory.getCondition().getUuid());
		
		ArrayList<Condition> conditions = new ArrayList<Condition>();
		for (org.openmrs.Condition condition : conditionHistory.getConditions()) {
			conditions.add(conditionMapper.map(condition));
		}
		conditionHistoryContract.setConditions(conditions);
		conditionHistoryContract.setConditionNonCoded(conditionHistory.getNonCodedCondition());
		return conditionHistoryContract;
	}
	
	public List<ConditionHistory> map(List<org.openmrs.ConditionHistory> conditionHistories100) {
		List<ConditionHistory> conditionHistories101 = new ArrayList<ConditionHistory>();
		for (org.openmrs.ConditionHistory conditionHistory : conditionHistories100) {
			conditionHistories101.add(map(conditionHistory));
		}
		return conditionHistories101;
	}
}
