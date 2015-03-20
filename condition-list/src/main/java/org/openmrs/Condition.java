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
    private Status status = Status.PRESUMED;
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
     * When a condition is altered (e.g., a symptom explicitly converted into a diagnosis), this field
     * is used to link the new condition to the condition(s) it has replaced.
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
        if(getConditionId() != null && getPatient() !=null && !getPatient().equals(patient)) {
            throw new IllegalArgumentException("Patient cannot be changed");
        }
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
     * The clinical status of the condition.  Default is PRESUMED.
     * <ul>
     * <li><b>PRESUMED</b> when the condition is suspected, but not yet confirmed
     * (HL7 uses the term "working")</li>
     * <li><b>CONFIRMED</b> when the condition has been confirmed (typically for
     * diagnoses)</li>
     * <li><b>HISTORY_OF</b> when the history of a condition is relevant to the
     * patient's ongoing medical care (e.g., history of stroke)</li>
     * </ul>
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
        if(getConditionId() != null && getConcept() !=null && !getConcept().equals(concept)){
            throw new IllegalArgumentException("Concept cannot be changed");
        }
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
     * When a condition is not codified, the concept for the condition is set to a concept for
     * NON-CODED and the free text representation of the condition is stored here.
     */
    @Attribute(required = false)
    public void setConditionNonCoded(String conditionNonCoded) {
        if(getConditionId() != null && getConditionNonCoded() != null && !getConditionNonCoded().equals(conditionNonCoded))
        {
            throw new IllegalArgumentException("Condition non coded cannot be changed");
        }
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
     *  Additional detail about the condition.  This is used to further refine the concept and
     *  <em>not</em> meant for encounter-specific detail or notes.  For example, detail
     *  such as "left more than right" or "diagnosed by chest x-ray 5-June-2010" would be
     *  appropriate additional detail; however, "hurts worse today" would not, since the
     *  additional detail is assumed to be refining the condition and not providing encounter-
     *  specific information.
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
