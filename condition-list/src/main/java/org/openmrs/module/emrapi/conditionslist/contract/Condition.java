package org.openmrs.module.emrapi.conditionslist.contract;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Condition {
	
	public String uuid;
	
	private String patientUuid;
	
	private Concept concept;
	
	private String conditionNonCoded;
	
	private org.openmrs.Condition.Status status;
	
	private Date onSetDate;
	
	private Date endDate;
	
	private Concept endReason;
	
	private String additionalDetail;
	
	private Boolean voided;
	
	private String voidReason;
	
	private String creator;
	
	private Date dateCreated;
	
	private String previousConditionUuid;
	
	public String getPreviousConditionUuid() {
		return previousConditionUuid;
	}
	
	public void setPreviousConditionUuid(String previousConditionUuid) {
		this.previousConditionUuid = previousConditionUuid;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public String getCreator() {
		return creator;
	}
	
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	public String getConditionNonCoded() {
		return conditionNonCoded;
	}
	
	public void setConditionNonCoded(String conditionNonCoded) {
		this.conditionNonCoded = conditionNonCoded;
	}
	
	public Date getOnSetDate() {
		return onSetDate;
	}
	
	public void setOnSetDate(Date onSetDate) {
		this.onSetDate = onSetDate;
	}
	
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	public Concept getEndReason() {
		return endReason;
	}
	
	public void setEndReason(Concept endReason) {
		this.endReason = endReason;
	}
	
	public String getAdditionalDetail() {
		return additionalDetail;
	}
	
	public void setAdditionalDetail(String additionalDetail) {
		this.additionalDetail = additionalDetail;
	}
	
	public Boolean getVoided() {
		return voided;
	}
	
	public void setVoided(Boolean voided) {
		this.voided = voided;
	}
	
	public String getVoidReason() {
		return voidReason;
	}
	
	public void setVoidReason(String voidReason) {
		this.voidReason = voidReason;
	}
	
	public org.openmrs.Condition.Status getStatus() {
		return status;
	}
	
	public void setStatus(org.openmrs.Condition.Status status) {
		this.status = status;
	}
	
	public String getPatientUuid() {
		return patientUuid;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public Concept getConcept() {
		return concept;
	}
	
	public void setConcept(Concept concept) {
		this.concept = concept;
	}
	
}
