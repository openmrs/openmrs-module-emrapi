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
package org.openmrs.module.emrapi.encounter.domain;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.Condition;
import org.openmrs.module.emrapi.utils.CustomJsonDateSerializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EncounterTransaction {
    private String visitUuid;
    private String encounterUuid;
    private String locationUuid;
    private String patientUuid;
    private String visitTypeUuid;
    private String encounterTypeUuid;
    private Date encounterDateTime;
    private Disposition disposition;
    private List<Observation> observations = new ArrayList<Observation>();
    private List<TestOrder> testOrders = new ArrayList<TestOrder>();
    private List<DrugOrder> drugOrders = new ArrayList<DrugOrder>();
    private List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
    private List<org.openmrs.Condition> conditions = new ArrayList<Condition>();
    private Set<Provider> providers = new HashSet<Provider>();

    public EncounterTransaction() {
    }

    public EncounterTransaction(String visitUuid, String encounterUuid) {
        this.visitUuid = visitUuid;
        this.encounterUuid = encounterUuid;
    }

    public EncounterTransaction(String visitUuid, String encounterUuid, List<Condition> conditions) {
        this.visitUuid = visitUuid;
        this.encounterUuid = encounterUuid;
        this.conditions = conditions;
    }

    public Disposition getDisposition() {
        return disposition;
    }

    public void setDisposition(Disposition disposition) {
        this.disposition = disposition; 
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public String getEncounterTypeUuid() {
        return encounterTypeUuid;
    }

    public String getVisitTypeUuid() {
        return visitTypeUuid;
    }

    public EncounterTransaction setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
        return this;
    }

    public EncounterTransaction setVisitTypeUuid(String visitTypeUuid) {
        this.visitTypeUuid = visitTypeUuid;
        return this;
    }

    public EncounterTransaction setEncounterTypeUuid(String encounterTypeUuid) {
        this.encounterTypeUuid = encounterTypeUuid;
        return this;
    }

    public EncounterTransaction setObservations(List<Observation> observations) {
        this.observations = observations;
        return this;
    }

    public List<Observation> getObservations() {
        return observations;
    }

    public List<TestOrder> getTestOrders() {
        return testOrders;
    }

    public void setTestOrders(List<TestOrder> testOrders) {
        this.testOrders = testOrders;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<DrugOrder> getDrugOrders() {
        return drugOrders;
    }

    public void setDrugOrders(List<DrugOrder> drugOrders) {
        this.drugOrders = drugOrders;
    }

    @JsonSerialize(using = CustomJsonDateSerializer.class)
    public Date getEncounterDateTime() {
        return encounterDateTime;
    }

    public EncounterTransaction setEncounterDateTime(Date encounterDateTime) {
        this.encounterDateTime = encounterDateTime;
        return this;
    }

    public String getLocationUuid() {
        return locationUuid;
    }

    public EncounterTransaction setLocationUuid(String locationUuid) {
        this.locationUuid = locationUuid;
        return this;
    }

    public List<Diagnosis> getDiagnoses() {
        return diagnoses;
    }

    public void setDiagnoses(List<Diagnosis> diagnoses) {
        this.diagnoses = diagnoses;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getEncounterUuid() {
        return encounterUuid;
    }

    public void setEncounterUuid(String encounterUuid) {
        this.encounterUuid = encounterUuid;
    }

    public void addObservation(Observation observation) {
        observations.add(observation);
    }

    public void addTestOrder(TestOrder testOrder) {
        testOrders.add(testOrder);
    }

    public void addDrugOrder(DrugOrder drugOrder) {
        drugOrders.add(drugOrder);
    }

    public void addDiagnosis(Diagnosis diagnosis) {
        diagnoses.add(diagnosis);
    }

    public Set<Provider> getProviders() {
        return providers;
    }

    public void setProviders(Set<Provider> providers) {
        this.providers = providers;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Concept {
        private String uuid;
        private String name;
        private String dataType;
        private boolean isSet;
        private String shortName;
        @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
        private String units;
        private String conceptClass;

        public Concept(String uuid, String name, boolean isSet, String dataType, String units, String conceptClass, String shortName) {
            this.uuid = uuid;
            this.name = name;
            this.dataType = dataType;
            this.isSet = isSet;
            this.units = units;
            this.conceptClass = conceptClass;
            this.shortName = shortName;
        }

        public Concept() {
        }

        public Concept(String uuid, String name, boolean isSet) {
            this(uuid, name, isSet, null, null, null, null);
        }

        public Concept(String uuid, String name) {
            this (uuid, name, false);
        }

        public Concept(String uuid) {
            this (uuid, null, false);
        }

        public String getUuid() {
            return uuid;
        }

        public String getName() {
            return name;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isSet() {
            return isSet;
        }

        public void setSet(boolean set) {
            isSet = set;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public String getUnits() {
            return units;
        }

        public void setUnits(String units) {
            this.units = units;
        }

        public String getConceptClass() {
            return conceptClass;
        }

        public String getShortName() {
            return shortName;
        }

        public void setShortName(String shortName) {
            this.shortName = shortName;
        }

        public void setConceptClass(String conceptClass) {
            this.conceptClass = conceptClass;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Observation {
        private String uuid;
        private Object value;
        private String comment;
        private boolean voided;
        private String voidReason;
        private Concept concept;
        private List<Observation> groupMembers = new ArrayList<Observation>();
        private String orderUuid;
        private Date observationDateTime;

        public String getUuid() {
            return uuid;
        }

        public Observation setUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        @JsonIgnore
        public String getConceptUuid() {
            return concept.getUuid();
        }

        public Concept getConcept() {
            return concept;
        }

        public Observation setConcept(Concept concept) {
            this.concept = concept;
            return this;
        }

        public Object getValue() {
            return value;
        }

        public Observation setValue(Object value) {
            this.value = value;
            return this;
        }

        public String getComment() {
            return comment;
        }

        public Observation setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public Observation setVoided(boolean voided) {
            this.voided = voided;
            return this;
        }

        public boolean getVoided() {
            return voided;
        }

        public String getVoidReason() {
            return voidReason;
        }

        public Observation setVoidReason(String voidReason) {
            this.voidReason = voidReason;
            return this;
        }

        public List<Observation> getGroupMembers() {
            return groupMembers;
        }

        public void setGroupMembers(List<Observation> groupMembers) {
            this.groupMembers = groupMembers;
        }

        public void addGroupMember(Observation observation) {
            groupMembers.add(observation);
        }

        public String getOrderUuid() {
            return orderUuid;
        }

        public Observation setOrderUuid(String orderUuid) {
            this.orderUuid = orderUuid;
            return this;
        }

        public Observation setObservationDateTime(Date observationDateTime) {
            this.observationDateTime = observationDateTime;
            return this;
        }

        @JsonSerialize(using = CustomJsonDateSerializer.class)
        public Date getObservationDateTime() {
            return observationDateTime;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Disposition{
        private String code;
        private String conceptName;
        private String existingObs;
        private boolean voided;
        private String voidReason;
        private List<Observation> additionalObs;
        private Date dispositionDateTime;

        public List<Observation> getAdditionalObs() {
            return additionalObs;
        }

        public void setAdditionalObs(List<Observation> additionalObs) {
            this.additionalObs = additionalObs;
        }

        public Disposition() {
        }

        public Disposition(String code) {
            this.code = code;
        }

        public String getExistingObs() {
            return existingObs;
        }

        public boolean isVoided() {
            return voided;
        }

        public Disposition setVoided(boolean voided) {
            this.voided = voided;
            return this;
        }

        public String getVoidReason() {
            return voidReason;
        }

        public Disposition setVoidReason(String voidReason) {
            this.voidReason = voidReason;
            return this;
        }

        public Disposition setExistingObs(String existingObs) {
            this.existingObs = existingObs;
            return this;
        }

        public String getCode() {
            return code;
        }

        public Disposition setCode(String code) {
            this.code = code;
            return this;
        }

        @JsonSerialize(using = CustomJsonDateSerializer.class)
        public Date getDispositionDateTime() {
            return dispositionDateTime;
        }

        public void setDispositionDateTime(Date date) {
            this.dispositionDateTime = date;
        }

      /*  public String getDispositionNote() {
            return dispositionNote;
        }

        public Disposition setDispositionNote(String dispositionNote) {
            this.dispositionNote = dispositionNote;
            return this;
        }*/

        public String getConceptName() {
            return conceptName;
        }

        public void setConceptName(String conceptName) {
            this.conceptName = conceptName;
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TestOrder {
        private Concept concept;
        private String orderTypeUuid;
        private String instructions;
        private String uuid;
        private boolean voided;
        private String voidReason;
        private Date dateCreated;
        private Date dateChanged;
        private String orderNumber;

        @JsonIgnore
        public String getConceptUuid() {
            return concept.getUuid();
        }

        public Concept getConcept() {
            return concept;
        }

        public TestOrder setConcept(Concept concept) {
            this.concept = concept;
            return this;
        }

        public String getInstructions() {
            return instructions;
        }

        public TestOrder setInstructions(String instructions) {
            this.instructions = instructions;
            return this;
        }


        public String getUuid() {
            return uuid;
        }

        public TestOrder setUuid(String uuid) {
            this.uuid = uuid;
            return this;
        }

        public TestOrder setVoided(boolean voided) {
            this.voided = voided;
            return this;
        }

        public boolean isVoided() {
            return voided;
        }

        public String getVoidReason() {
            return voidReason;
        }

        public TestOrder setVoidReason(String voidReason) {
            this.voidReason = voidReason;
            return this;
        }

        public String getOrderTypeUuid() {
            return orderTypeUuid;
        }

        public TestOrder setOrderTypeUuid(String orderTypeUuid) {
            this.orderTypeUuid = orderTypeUuid;
            return this;
        }

        @JsonSerialize(using = CustomJsonDateSerializer.class)
        public Date getDateCreated() {
            return dateCreated;
        }

        public void setDateCreated(Date dateCreated) {
            this.dateCreated = dateCreated;
        }

        @JsonSerialize(using = CustomJsonDateSerializer.class)
        public Date getDateChanged() {
            return dateChanged;
        }

        public void setDateChanged(Date dateChanged) {
            this.dateChanged = dateChanged;
        }

        public void setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
        }

        public String getOrderNumber() {
            return orderNumber;
        }
    }



    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Diagnosis {
        private String order;
        private String certainty;
        private String freeTextAnswer;
        private Concept codedAnswer;
        private String existingObs;
        private Date diagnosisDateTime;
        private boolean voided;
        private String voidReason;
        private Set<Provider> providers = new HashSet<Provider>();

        public String getOrder() {
            return order;
        }

        public Diagnosis setOrder(String order) {
            this.order = order;
            return this;
        }

        public String getCertainty() {
            return certainty;
        }

        public Diagnosis setCertainty(String certainty) {
            this.certainty = certainty;
            return this;
        }

        public String getFreeTextAnswer() {
            return freeTextAnswer;
        }

        public Concept getCodedAnswer() {
            return codedAnswer;
        }

        public String getExistingObs() {
            return existingObs;
        }

        public Diagnosis setExistingObs(String existingObs) {
            this.existingObs = existingObs;
            return this;
        }

        public Diagnosis setFreeTextAnswer(String freeTextAnswer) {
            this.freeTextAnswer = freeTextAnswer;
            return this;
        }

        public Diagnosis setCodedAnswer(Concept codedAnswer) {
            this.codedAnswer = codedAnswer;
            return this;
        }

        @JsonSerialize(using = CustomJsonDateSerializer.class)
        public Date getDiagnosisDateTime() {
            return diagnosisDateTime;
        }

        public void setDiagnosisDateTime(Date date) {
            this.diagnosisDateTime = date;
        }

        public boolean isVoided() {
            return voided;
        }

        public Diagnosis setVoided(boolean voided) {
            this.voided = voided;
            return this;
        }

        public String getVoidReason() {
            return voidReason;
        }

        public void setVoidReason(String voidReason) {
            this.voidReason = voidReason;
        }

        public void setProviders(Set<Provider> providers) {
            this.providers = providers;
        }

        public Set<Provider> getProviders() {
            return providers;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DrugOrder {
        private String uuid;
        private String careSetting;
        private String orderType;
        private Drug drug;
        private String dosingInstructionType;
        private DosingInstructions dosingInstructions;
        private Date dateActivated;
        private Date scheduledDate;
        private Date effectiveStartDate;
        private Date autoExpireDate;
        private Date effectiveStopDate;
        private Date dateStopped;
        private String action;
        private String previousOrderUuid;
        private Concept orderReasonConcept;
        private String orderReasonText;
        private String instructions;
        private String commentToFulfiller;
        private Integer duration;
        private String durationUnits;
        private Boolean voided;
        private String voidReason;
        private String orderNumber;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getCareSetting() {
            return careSetting;
        }

        public void setCareSetting(String careSetting) {
            this.careSetting = careSetting;
        }

        public Drug getDrug() {
            return drug;
        }

        public void setDrug(Drug drug) {
            this.drug = drug;
        }

        public String getDosingInstructionType() {
            return dosingInstructionType;
        }

        public void setDosingInstructionType(String dosingInstructionType) {
            this.dosingInstructionType = dosingInstructionType;
        }

        public DosingInstructions getDosingInstructions() {
            return dosingInstructions;
        }

        public void setDosingInstructions(DosingInstructions dosingInstructions) {
            this.dosingInstructions = dosingInstructions;
        }

        public Date getScheduledDate() {
            return scheduledDate;
        }

        public void setScheduledDate(Date scheduledDate) {
            this.scheduledDate = scheduledDate;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getPreviousOrderUuid() {
            return previousOrderUuid;
        }

        public void setPreviousOrderUuid(String previousOrderUuid) {
            this.previousOrderUuid = previousOrderUuid;
        }

        public Concept getOrderReasonConcept() {
            return orderReasonConcept;
        }

        public void setOrderReasonConcept(Concept orderReasonConcept) {
            this.orderReasonConcept = orderReasonConcept;
        }

        public String getOrderReasonText() {
            return orderReasonText;
        }

        public void setOrderReasonText(String orderReasonText) {
            this.orderReasonText = orderReasonText;
        }

        public String getInstructions() {
            return instructions;
        }

        public void setInstructions(String instructions) {
            this.instructions = instructions;
        }

        public Integer getDuration() {
            return duration;
        }

        public void setDuration(Integer duration) {
            this.duration = duration;
        }

        public String getDurationUnits() {
            return durationUnits;
        }

        public void setDurationUnits(String durationUnits) {
            this.durationUnits = durationUnits;
        }

        public String getCommentToFulfiller() {
            return commentToFulfiller;
        }

        public void setCommentToFulfiller(String commentToFulfiller) {
            this.commentToFulfiller = commentToFulfiller;
        }

        public Date getDateActivated() {
            return dateActivated;
        }

        public void setDateActivated(Date dateActivated) {
            this.dateActivated = dateActivated;
        }

        public Date getEffectiveStartDate() {
            return effectiveStartDate;
        }

        public void setEffectiveStartDate(Date effectiveStartDate) {
            this.effectiveStartDate = effectiveStartDate;
        }

        public Date getAutoExpireDate() {
            return autoExpireDate;
        }

        public void setAutoExpireDate(Date autoExpireDate) {
            this.autoExpireDate = autoExpireDate;
        }

        public Date getEffectiveStopDate() {
            return effectiveStopDate;
        }

        public void setEffectiveStopDate(Date effectiveStopDate) {
            this.effectiveStopDate = effectiveStopDate;
        }

        public void setDateStopped(Date dateStopped) {
            this.dateStopped = dateStopped;
        }

        public Date getDateStopped() {
            return dateStopped;
        }

        public String getOrderType() {
            return orderType;
        }

        public void setOrderType(String orderType) {
            this.orderType = orderType;
        }

        public void setVoided(Boolean voided) {
            this.voided = voided;
        }

        public Boolean getVoided() {
            return voided;
        }

        public void setVoidReason(String voidReason) {
            this.voidReason = voidReason;
        }

        public String getVoidReason() {
            return voidReason;
        }

        public String getOrderNumber() {
            return orderNumber;
        }

        public void setOrderNumber(String orderNumber) {
            this.orderNumber = orderNumber;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Provider {
        private String uuid;
        private String name;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Drug {
        private String name;
        private String uuid;
        private String form;
        private String strength;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getForm() {
            return form;
        }

        public void setForm(String form) {
            this.form = form;
        }

        public String getStrength() {
            return strength;
        }

        public void setStrength(String strength) {
            this.strength = strength;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DosingInstructions {
        private Double dose;
        private String doseUnits;
        private String route;
        private String frequency;
        private Boolean asNeeded;
        private String administrationInstructions;
        private Double quantity;
        private String quantityUnits;
        private Integer numberOfRefills;

        public Double getDose() {
            return dose;
        }

        public void setDose(Double dose) {
            this.dose = dose;
        }

        public String getDoseUnits() {
            return doseUnits;
        }

        public void setDoseUnits(String doseUnits) {
            this.doseUnits = doseUnits;
        }

        public String getRoute() {
            return route;
        }

        public void setRoute(String route) {
            this.route = route;
        }

        public String getFrequency() {
            return frequency;
        }

        public void setFrequency(String frequency) {
            this.frequency = frequency;
        }

        public Boolean getAsNeeded() {
            return asNeeded;
        }

        public void setAsNeeded(Boolean asNeeded) {
            this.asNeeded = asNeeded;
        }

        public String getAdministrationInstructions() {
            return administrationInstructions;
        }

        public void setAdministrationInstructions(String administrationInstructions) {
            this.administrationInstructions = administrationInstructions;
        }

        public Double getQuantity() {
            return quantity;
        }

        public void setQuantity(Double quantity) {
            this.quantity = quantity;
        }

        public String getQuantityUnits() {
            return quantityUnits;
        }

        public void setQuantityUnits(String quantityUnits) {
            this.quantityUnits = quantityUnits;
        }

        public Integer getNumberOfRefills() {
            return numberOfRefills;
        }

        public void setNumberOfRefills(Integer numberOfRefills) {
            this.numberOfRefills = numberOfRefills;
        }
    }
}
