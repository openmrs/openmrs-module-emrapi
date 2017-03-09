package org.openmrs.module.emrapi.conditionslist.contract;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.conditionslist.ConditionListConstants;

public class ConditionMapper {
	
	public Condition map(org.openmrs.Condition openmrsCondition) {
		Concept concept = new Concept(openmrsCondition.getConcept().getUuid(),
				openmrsCondition.getConcept().getDisplayString());
		Condition condition = new Condition();
		condition.setUuid(openmrsCondition.getUuid());
		condition.setAdditionalDetail(openmrsCondition.getAdditionalDetail());
		condition.setStatus(openmrsCondition.getStatus());
		condition.setConcept(concept);
		condition.setPatientUuid(openmrsCondition.getPatient().getUuid());
		condition.setConditionNonCoded(openmrsCondition.getConditionNonCoded());
		condition.setOnSetDate(openmrsCondition.getOnsetDate());
		condition.setVoided(openmrsCondition.getVoided());
		condition.setVoidReason(openmrsCondition.getVoidReason());
		condition.setEndDate(openmrsCondition.getEndDate());
		condition.setCreator(openmrsCondition.getCreator().getDisplayString());
		condition.setDateCreated(openmrsCondition.getDateCreated());
		if (openmrsCondition.getPreviousCondition() != null) {
			condition.setPreviousConditionUuid(openmrsCondition.getPreviousCondition().getUuid());
		}
		if (openmrsCondition.getEndReason() != null) {
			Concept endReason = new Concept(openmrsCondition.getEndReason().getUuid(), openmrsCondition.getEndReason()
					.getDisplayString());
			condition.setEndReason(endReason);
		}
		return condition;
	}
	
	public org.openmrs.Condition map(Condition condition) {
		org.openmrs.Concept concept = Context.getConceptService().getConceptByUuid(condition.getConcept().getUuid());
		Patient patient = Context.getPatientService().getPatientByUuid(condition.getPatientUuid());
		String nonCodedConditionConcept = Context.getAdministrationService().getGlobalProperty(
				ConditionListConstants.GLOBAL_PROPERTY_NON_CODED_UUID);
		
		org.openmrs.Condition openmrsCondition = new org.openmrs.Condition();
		
		if (!isEmpty(condition.getConditionNonCoded())) {
			concept = Context.getConceptService().getConceptByUuid(nonCodedConditionConcept);
		}
		if (condition.getEndReason() != null) {
			org.openmrs.Concept endReason = Context.getConceptService().getConceptByUuid(
					condition.getEndReason().getUuid());
			openmrsCondition.setEndReason(endReason);
		}
		if (condition.getUuid() != null) {
			openmrsCondition.setUuid(condition.getUuid());
		}
		openmrsCondition.setAdditionalDetail(condition.getAdditionalDetail());
		openmrsCondition.setStatus(condition.getStatus());
		openmrsCondition.setConcept(concept);
		openmrsCondition.setPatient(patient);
		openmrsCondition.setConditionNonCoded(condition.getConditionNonCoded());
		openmrsCondition.setOnsetDate(condition.getOnSetDate());
		openmrsCondition.setEndDate(condition.getEndDate());
		openmrsCondition.setVoided(condition.getVoided());
		openmrsCondition.setVoidReason(condition.getVoidReason());
		
		return openmrsCondition;
	}
}
