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
package org.openmrs.api;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.simpleframework.xml.Attribute;

import java.util.Date;


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
        PRESUMED, CONFIRMED, HISTORY_OF
    }

    private Integer conditionId;
    private Condition previousCondition;
    private Patient patient;
    private Status status;
    private Concept concept;
    private String conditionNonCoded;
    private Date onsetDate;
    private String additionalDetail;
    private Date endDate;
    private Concept endReason;


    /**
     * @return Returns the conditionId.
     */
    @Attribute(required = true)
    public Integer getConditionId() {
        return conditionId;
    }

    /**
     * @param conditionId The conditionId to set.
     */
    @Attribute(required = true)
    public void setConditionId(Integer conditionId) {
        this.conditionId = conditionId;
    }

    /**
     * @return Returns the previousCondition.
     */
    @Attribute(required = false)
    public Condition getPreviousCondition() {
        return previousCondition;
    }

    /**
     * @param previousCondition The previousCondition to set.
     */
    @Attribute(required = false)
    public void setPreviousCondition(Condition previousCondition) {
        this.previousCondition = previousCondition;
    }

    /**
     * @return Returns the patient.
     */
    @Attribute(required = true)
    public Patient getPatient() {
        return patient;
    }

    /**
     * @param patient The patient to set.
     */
    @Attribute(required = true)
    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    /**
     * @return Returns the status.
     */
    @Attribute(required = true)
    public Status getStatus() {
        return status;
    }

    /**
     * @param status The status to set.
     */
    @Attribute(required = true)
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return Returns the concept.
     */
    @Attribute(required = true)
    public Concept getConcept() {
        return concept;
    }

    /**
     * @param concept The concept to set.
     */
    @Attribute(required = true)
    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    /**
     * @return Returns the conditionNonCoded.
     */
    @Attribute(required = false)
    public String getConditionNonCoded() {
        return conditionNonCoded;
    }

    /**
     * @param conditionNonCoded The conditionNonCoded to set.
     */
    @Attribute(required = false)
    public void setConditionNonCoded(String conditionNonCoded) {
        this.conditionNonCoded = conditionNonCoded;
    }

    /**
     * @return Returns the onsetDate.
     */
    @Attribute(required = false)
    public Date getOnsetDate() {
        return onsetDate;
    }

    /**
     * @param onsetDate The onsetDate to set.
     */
    @Attribute(required = false)
    public void setOnsetDate(Date onsetDate) {
        this.onsetDate = onsetDate;
    }

    /**
     * @return Returns the additionalDetail.
     */
    @Attribute(required = false)
    public String getAdditionalDetail() {
        return additionalDetail;
    }

    /**
     * @param additionalDetail The additionalDetail to set.
     */
    @Attribute(required = false)
    public void setAdditionalDetail(String additionalDetail) {
        this.additionalDetail = additionalDetail;
    }

    /**
     * @return Returns the endDate.
     */
    @Attribute(required = true)
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate The endDate to set.
     */
    @Attribute(required = false)
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * @return Returns the endReason.
     */
    @Attribute(required = true)
    public Concept getEndReason() {
        return endReason;
    }

    /**
     * @param endReason The endReason to set.
     */
    @Attribute(required = false)
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

}
