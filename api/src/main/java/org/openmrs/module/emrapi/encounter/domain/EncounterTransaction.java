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
import org.openmrs.ConceptMap;
import org.openmrs.module.emrapi.CareSettingType;
import org.openmrs.module.emrapi.utils.CustomJsonDateSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EncounterTransaction {
    private String visitUuid;
    private String encounterUuid;
    private String locationUuid;
    private String locationName;
    private String patientUuid;
    private String visitTypeUuid;
    private String encounterTypeUuid;
    private Date encounterDateTime;
    private Disposition disposition;
    private List<Observation> observations = new ArrayList<Observation>();
    private List<Order> orders = new ArrayList<Order>();
    private List<DrugOrder> drugOrders = new ArrayList<DrugOrder>();
    private List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
    private Set<Provider> providers = new HashSet<Provider>();
    private Map<String,Object> extensions = new HashMap<String,Object>();
    private Map<String, Object> context = new HashMap<String, Object>();
    private String visitLocationUuid;


    public String getVisitLocationUuid() { return visitLocationUuid; }

    public void setVisitLocationUuid(String visitLocationUuid) { this.visitLocationUuid = visitLocationUuid; }

    public EncounterTransaction() {
    }

    public EncounterTransaction(String visitUuid, String encounterUuid) {
        this.visitUuid = visitUuid;
        this.encounterUuid = encounterUuid;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public void setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
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

    public String getLocationName() {
        return locationName;
    }

    public EncounterTransaction setLocationName(String locationName) {
        this.locationName = locationName;
        return this;
    }

    public List<Observation> getObservations() {
        return observations;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
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

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }

    public void addObservation(Observation observation) {
        observations.add(observation);
    }

    public void addOrder(Order order) {
        orders.add(order);
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
        private Collection<ConceptMap> conceptMaps;
        private Double hiNormal;
        private Double lowNormal;

        public Concept(String uuid, String name, boolean isSet, String dataType, String units, String conceptClass, String shortName, Collection<ConceptMap> conceptMaps) {
            this.uuid = uuid;
            this.name = name;
            this.dataType = dataType;
            this.isSet = isSet;
            this.units = units;
            this.conceptClass = conceptClass;
            this.shortName = shortName;
            this.conceptMaps = conceptMaps;
        }

        public Concept() {
        }

        public Concept(String uuid, String name, boolean isSet) {
            this(uuid, name, isSet, null, null, null, null, null);
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

        public List<Map<String, Object>> getMappings() {
            List<Map<String,Object>> mappings = new ArrayList<Map<String, Object>>();
            if (conceptMaps == null) {
                return mappings;
            }
            for (ConceptMap conceptMap : conceptMaps) {
                Map<String,Object> mappingInfo = new HashMap<String, Object>();
                mappingInfo.put("source", conceptMap.getConceptReferenceTerm().getConceptSource().getName());
                mappingInfo.put("code", conceptMap.getConceptReferenceTerm().getCode());
                mappingInfo.put("name", conceptMap.getConceptReferenceTerm().getName());
                mappings.add(mappingInfo);
            }
            return mappings;
        }

        public void setHiNormal(Double hiNormal) {
            this.hiNormal = hiNormal;
        }

        public void setLowNormal(Double lowNormal) {
            this.lowNormal = lowNormal;
        }

        public Double getLowNormal() {
            return lowNormal;
        }

        public Double getHiNormal() {
            return hiNormal;
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String uuid;
        private String personName;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getPersonName() {
            return personName;
        }

        public void setPersonName(String personName) {
            this.personName = personName;
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
        private User creator;

        private String formNamespace;
        private String formFieldPath;

        private String interpretation;
        private String status;

        public String getFormNamespace() {
            return formNamespace;
        }

        public Observation setFormNamespace(String formNamespace) {
            this.formNamespace = formNamespace;
            return this;
        }

        public String getFormFieldPath() {
            return formFieldPath;
        }

        public Observation setFormFieldPath(String formFieldPath) {
            this.formFieldPath = formFieldPath;
            return this;
        }

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

        public User getCreator() {
            return creator;
        }

        public void setCreator(User creator) {
            this.creator = creator;
        }

        @JsonSerialize(using = CustomJsonDateSerializer.class)
        public Date getObservationDateTime() {
            return observationDateTime;
        }

        public String getInterpretation() {
            return interpretation;
        }

        public Observation setInterpretation(String interpretation) {
            this.interpretation = interpretation;
            return this;
        }

        public String getStatus() {
            return status;
        }

        public Observation setStatus(String status) {
            this.status = status;
            return this;
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

        public String getConceptName() {
            return conceptName;
        }

        public void setConceptName(String conceptName) {
            this.conceptName = conceptName;
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderGroup {
        private String uuid;
        private OrderSet orderSet;

        public OrderGroup(){
        }

        public OrderGroup(String uuid){
            this.uuid = uuid;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public OrderSet getOrderSet() {
            return orderSet;
        }

        public void setOrderSet(OrderSet orderSet) { this.orderSet = orderSet; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Order {
        private Concept concept;
        private String instructions;
        private String uuid;
        private String orderType;
        private OrderGroup orderGroup;
        private Date dateCreated;
        private Date dateChanged;
        private Date dateStopped;
        private String orderNumber;
        private CareSettingType careSetting;
        private String action;
        private String commentToFulfiller;
        private Date autoExpireDate;
        private String urgency;

        public void setUrgency(String urgency) {
            this.urgency = urgency;
        }

        public String getUrgency() {
            return urgency;
        }

        public Date getAutoExpireDate() {
            return autoExpireDate;
        }

        public void setAutoExpireDate(Date autoExpireDate) {
            this.autoExpireDate = autoExpireDate;
        }

        public void setPreviousOrderUuid(String previousOrderUuid) {
            this.previousOrderUuid = previousOrderUuid;
        }

        private String previousOrderUuid;

        public String getCommentToFulfiller() {
            return commentToFulfiller;
        }

        public void setCommentToFulfiller(String commentToFulfiller) {
            this.commentToFulfiller = commentToFulfiller;
        }


        @JsonIgnore
        public String getConceptUuid() {
            return concept.getUuid();
        }

        public Concept getConcept() {
            return concept;
        }

        public Order setConcept(Concept concept) {
            this.concept = concept;
            return this;
        }

        public String getInstructions() {
            return instructions;
        }

        public Order setInstructions(String instructions) {
            this.instructions = instructions;
            return this;
        }


        public String getUuid() {
            return uuid;
        }

        public Order setUuid(String uuid) {
            this.uuid = uuid;
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

        public void setCareSetting(CareSettingType careSetting) {
            this.careSetting = careSetting;
        }

        public CareSettingType getCareSetting() {
            return careSetting;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getAction() {
            return action;
        }

        public String getOrderType() {
            return orderType;
        }

        public void setOrderType(String orderType) {
            this.orderType = orderType;
        }

        public OrderGroup getOrderGroup() {
            return orderGroup;
        }

        public void setOrderGroup(OrderGroup orderGroup) {
            this.orderGroup = orderGroup;
        }

        public String getPreviousOrderUuid() {
            return previousOrderUuid;
        }

        @JsonSerialize(using = CustomJsonDateSerializer.class)
        public Date getDateStopped() {
            return dateStopped;
        }

        @JsonSerialize(using = CustomJsonDateSerializer.class)
        public void setDateStopped(Date dateStopped) {
            this.dateStopped = dateStopped;
        }


    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderSet {
        private String uuid;

        public OrderSet(){
        }
        public OrderSet(String uuid){
            this.uuid = uuid;
        }
        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
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
        private String comments;
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

        public String getComments() {
            return comments;
        }

        public Diagnosis setComments(String comments) {
            this.comments = comments;
            return this;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DrugOrder extends Order {
        private Drug drug;
        private String drugNonCoded;
        private String dosingInstructionType;
        private DosingInstructions dosingInstructions;
        private Date dateActivated;
        private Date scheduledDate;
        private Date effectiveStartDate;
        private Date effectiveStopDate;
        private String previousOrderUuid;
        private String orderReasonText;
        private Integer duration;
        private String durationUnits;
        private Boolean voided;
        private String voidReason;
        private EncounterTransaction.Concept orderReasonConcept;

        private Double sortWeight;

        public Drug getDrug() {
            return drug;
        }

        public void setDrug(Drug drug) {
            this.drug = drug;
        }

        public String getDrugNonCoded() {
            return drugNonCoded;
        }

        public void setDrugNonCoded(String drugNonCoded) {
            this.drugNonCoded = drugNonCoded;
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
        public String getPreviousOrderUuid() {
            return previousOrderUuid;
        }

        public void setPreviousOrderUuid(String previousOrderUuid) {
            this.previousOrderUuid = previousOrderUuid;
        }

        public String getOrderReasonText() {
            return orderReasonText;
        }

        public void setOrderReasonText(String orderReasonText) {
            this.orderReasonText = orderReasonText;
        }

        public Concept getOrderReasonConcept() {
            return orderReasonConcept;
        }

        public void setOrderReasonConcept(Concept orderReasonConcept) {
            this.orderReasonConcept = orderReasonConcept;
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

        public Date getEffectiveStopDate() {
            return effectiveStopDate;
        }

        public void setEffectiveStopDate(Date effectiveStopDate) {
            this.effectiveStopDate = effectiveStopDate;
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

        public Double getSortWeight() {
            return sortWeight;
        }

        public void setSortWeight(Double sortWeight) {
            this.sortWeight = sortWeight;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Provider {
        private String uuid;
        private String name;
        private String encounterRoleUuid;

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

        public String getEncounterRoleUuid() {
            return encounterRoleUuid;
        }

        public void setEncounterRoleUuid(String encounterRoleUuid) {
            this.encounterRoleUuid = encounterRoleUuid;
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
