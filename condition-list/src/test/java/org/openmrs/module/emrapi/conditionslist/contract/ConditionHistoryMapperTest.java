package org.openmrs.module.emrapi.conditionslist.contract;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class ConditionHistoryMapperTest {
	
	@Mock
	public ConditionMapper conditionMapper;
	
	private ConditionHistoryMapper conditionHistoryMapper;
	
	@Before
	public void setUp() throws Exception {
		initMocks(this);
		
		conditionHistoryMapper = new ConditionHistoryMapper(conditionMapper);
		
		when(conditionMapper.map(any(org.openmrs.Condition.class))).then(new Answer<Condition>() {
			
			@Override
			public Condition answer(final InvocationOnMock invocationOnMock) throws Throwable {
				return new Condition() {{
					setUuid(((org.openmrs.Condition) invocationOnMock.getArguments()[0]).getUuid());
				}};
			}
		});
	}
	
	@Test
	public void shouldMapConditionHistoryModalToContract() throws Exception {
		org.openmrs.ConditionHistory conditionHistory = new org.openmrs.ConditionHistory();
		org.openmrs.Condition condition1 = getCondition("uuid_one");
		org.openmrs.Condition condition2 = getCondition("uuid_two");
		org.openmrs.Condition condition3 = getCondition("uuid_three");
		conditionHistory.setConditions(Arrays.asList(condition1, condition2, condition3));
		conditionHistory.setCondition(new Concept());
		
		ConditionHistory conditionHistoryContract = conditionHistoryMapper.map(conditionHistory);
		
		assertEquals("uuid_one", conditionHistoryContract.getConditions().get(0).getUuid());
		assertEquals("uuid_two", conditionHistoryContract.getConditions().get(1).getUuid());
		assertEquals("uuid_three", conditionHistoryContract.getConditions().get(2).getUuid());
	}
	
	@Test
	public void shouldMapConditionHistoryModalsToListOfContracts() throws Exception {
		org.openmrs.ConditionHistory conditionHistory1 = new org.openmrs.ConditionHistory();
		org.openmrs.Condition condition1 = getCondition("uuid_one");
		org.openmrs.Condition condition2 = getCondition("uuid_two");
		org.openmrs.Condition condition3 = getCondition("uuid_three");
		conditionHistory1.setConditions(Arrays.asList(condition1, condition2, condition3));
		conditionHistory1.setCondition(new Concept());
		
		org.openmrs.ConditionHistory conditionHistory2 = new org.openmrs.ConditionHistory();
		org.openmrs.Condition condition4 = getCondition("uuid_four");
		org.openmrs.Condition condition5 = getCondition("uuid_five");
		org.openmrs.Condition condition6 = getCondition("uuid_six");
		conditionHistory2.setConditions(Arrays.asList(condition4, condition5, condition6));
		conditionHistory2.setCondition(new Concept());
		
		List<ConditionHistory> conditionHistoryContracts = conditionHistoryMapper.map(Arrays.asList(conditionHistory1,
				conditionHistory2));
		
		List<Condition> conditions1 = conditionHistoryContracts.get(0).getConditions();
		List<Condition> conditions2 = conditionHistoryContracts.get(1).getConditions();
		assertEquals("uuid_one", conditions1.get(0).getUuid());
		assertEquals("uuid_two", conditions1.get(1).getUuid());
		assertEquals("uuid_three", conditions1.get(2).getUuid());
		
		assertEquals("uuid_four", conditions2.get(0).getUuid());
		assertEquals("uuid_five", conditions2.get(1).getUuid());
		assertEquals("uuid_six", conditions2.get(2).getUuid());
	}
	
	private org.openmrs.Condition getCondition(String uuid) {
		org.openmrs.Condition condition = new org.openmrs.Condition();
		condition.setPatient(new Patient());
		condition.setConcept(new Concept());
		condition.setUuid(uuid);
		return condition;
	}
}