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
package org.openmrs;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Defines a Condition in the system.
 *
 * @version 2.0
 */
public class Condition extends BaseOpenmrsData implements java.io.Serializable {
	
	public static final long serialVersionUID = 2L;
	
	protected final Log log = LogFactory.getLog(getClass());
	
	/**
	 * default empty constructor
	 */
	public Condition() {
	}
	
	/**
	 * @param conditionId Integer to create this Condition object from
	 */
	public Condition(Integer conditionId) {
		this.conditionId = conditionId;
	}
	
	public enum Status {
		ACTIVE, INACTIVE, HISTORY_OF
	}
	
	private Integer conditionId;
	
	private Condition previousCondition;
	
	private Patient patient;
	
	private Status status = Status.ACTIVE;
	
	private Concept concept;
	
	private String conditionNonCoded;
	
	private Date onsetDate;
	
	private String additionalDetail;
	
	private Date endDate;
	
	private Concept endReason;
	
	public static Condition newInstance(Condition condition) {
		return copy(condition, new Condition());
	}
	
	public static Condition copy(Condition fromCondition, Condition toCondition) {
		toCondition.setPreviousCondition(fromCondition.getPreviousCondition());
		toCondition.setPatient(fromCondition.getPatient());
		toCondition.setStatus(fromCondition.getStatus());
		toCondition.setConcept(fromCondition.getConcept());
		toCondition.setConditionNonCoded(fromCondition.getConditionNonCoded());
		toCondition.setOnsetDate(fromCondition.getOnsetDate());
		toCondition.setAdditionalDetail(fromCondition.getAdditionalDetail());
		toCondition.setEndDate(fromCondition.getEndDate());
		toCondition.setEndReason(fromCondition.getEndReason());
		toCondition.setVoided(fromCondition.getVoided());
		toCondition.setVoidedBy(fromCondition.getVoidedBy());
		toCondition.setVoidReason(fromCondition.getVoidReason());
		toCondition.setDateVoided(fromCondition.getDateVoided());
		return toCondition;
	}
	
	/**
	 * @return Returns the conditionId.
	 */
	public Integer getConditionId() {
		return conditionId;
	}
	
	/**
	 * @param conditionId The conditionId to set.
	 */
	public void setConditionId(Integer conditionId) {
		this.conditionId = conditionId;
	}
	
	/**
	 * @return Returns the previousCondition.
	 */
	public Condition getPreviousCondition() {
		return previousCondition;
	}
	
	/**
	 * @param previousCondition The previousCondition to set.
	 *                          When a condition is altered (e.g., a symptom explicitly converted into a diagnosis), this
	 *                          field
	 *                          is used to link the new condition to the condition(s) it has replaced.
	 */
	public void setPreviousCondition(Condition previousCondition) {
		this.previousCondition = previousCondition;
	}
	
	/**
	 * @return Returns the patient.
	 */
	public Patient getPatient() {
		return patient;
	}
	
	/**
	 * @param patient The patient to set.
	 */
	public void setPatient(Patient patient) {
		if (getConditionId() != null && getPatient() != null && !getPatient().equals(patient)) {
			throw new IllegalArgumentException("Patient cannot be changed");
		}
		this.patient = patient;
	}
	
	/**
	 * @return Returns the status.
	 */
	public Status getStatus() {
		return status;
	}
	
	/**
	 * @param status The status to set.
	 *               The clinical status of the condition.  Default is ACTIVE.
	 *               <ul>
	 *               <li><b>ACTIVE</b> when the condition is suspected, but not yet confirmed
	 *               (HL7 uses the term "working")</li>
	 *               <li><b>INACTIVE</b> when the condition has been confirmed (typically for
	 *               diagnoses)</li>
	 *               <li><b>HISTORY_OF</b> when the history of a condition is relevant to the
	 *               patient's ongoing medical care (e.g., history of stroke)</li>
	 *               </ul>
	 */
	public void setStatus(Status status) {
		this.status = status;
	}
	
	/**
	 * @return Returns the concept.
	 */
	public Concept getConcept() {
		return concept;
	}
	
	/**
	 * @param concept The concept to set.
	 */
	public void setConcept(Concept concept) {
		if (getConditionId() != null && getConcept() != null && !getConcept().equals(concept)) {
			throw new IllegalArgumentException("Concept cannot be changed");
		}
		this.concept = concept;
	}
	
	/**
	 * @return Returns the conditionNonCoded.
	 */
	public String getConditionNonCoded() {
		return conditionNonCoded;
	}
	
	/**
	 * @param conditionNonCoded The conditionNonCoded to set.
	 *                          When a condition is not codified, the concept for the condition is set to a concept for
	 *                          NON-CODED and the free text representation of the condition is stored here.
	 */
	public void setConditionNonCoded(String conditionNonCoded) {
		if (getConditionId() != null && getConditionNonCoded() != null && !getConditionNonCoded().equals(
				conditionNonCoded)) {
			throw new IllegalArgumentException("Condition non coded cannot be changed");
		}
		this.conditionNonCoded = conditionNonCoded;
	}
	
	/**
	 * @return Returns the onsetDate.
	 */
	public Date getOnsetDate() {
		return onsetDate;
	}
	
	/**
	 * @param onsetDate The onsetDate to set.
	 */
	public void setOnsetDate(Date onsetDate) {
		this.onsetDate = onsetDate;
	}
	
	/**
	 * @return Returns the additionalDetail.
	 */
	public String getAdditionalDetail() {
		return additionalDetail;
	}
	
	/**
	 * @param additionalDetail The additionalDetail to set.
	 *                         Additional detail about the condition.  This is used to further refine the concept and
	 *                         <em>not</em> meant for encounter-specific detail or notes.  For example, detail
	 *                         such as "left more than right" or "diagnosed by chest x-ray 5-June-2010" would be
	 *                         appropriate additional detail; however, "hurts worse today" would not, since the
	 *                         additional detail is assumed to be refining the condition and not providing encounter-
	 *                         specific information.
	 */
	public void setAdditionalDetail(String additionalDetail) {
		this.additionalDetail = additionalDetail;
	}
	
	/**
	 * @return Returns the endDate.
	 */
	
	public Date getEndDate() {
		return endDate;
	}
	
	/**
	 * @param endDate The endDate to set.
	 */
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	/**
	 * @return Returns the endReason.
	 */
	
	public Concept getEndReason() {
		return endReason;
	}
	
	/**
	 * @param endReason The endReason to set.
	 */
	
	public void setEndReason(Concept endReason) {
		this.endReason = endReason;
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#getId()
	 */
	@Override
	public Integer getId() {
		return getConditionId();
	}
	
	/**
	 * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
	 */
	@Override
	public void setId(Integer conditionId) {
		setConditionId(conditionId);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		if (!super.equals(o)) {
			return false;
		}
		
		Condition condition = (Condition) o;
		
		if (!patient.equals(condition.patient)) {
			return false;
		}
		if (status != condition.status) {
			return false;
		}
		if (!concept.equals(condition.concept)) {
			return false;
		}
		if (conditionNonCoded != null ?
				!conditionNonCoded.equals(condition.conditionNonCoded) :
				condition.conditionNonCoded != null) {
			return false;
		}
		if (onsetDate != null ? !onsetDate.equals(condition.onsetDate) : condition.onsetDate != null) {
			return false;
		}
		if (additionalDetail != null ?
				!additionalDetail.equals(condition.additionalDetail) :
				condition.additionalDetail != null) {
			return false;
		}
		if (endDate != null ? !endDate.equals(condition.endDate) : condition.endDate != null) {
			return false;
		}
		return endReason != null ? endReason.equals(condition.endReason) : condition.endReason == null;
	}
}
