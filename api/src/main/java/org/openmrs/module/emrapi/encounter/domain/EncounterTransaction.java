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
    private Set<Provider> providers = new HashSet<Provider>();

    public EncounterTransaction() {
    }

    public EncounterTransaction(String visitUuid, String encounterUuid) {
        this.visitUuid = visitUuid;
        this.encounterUuid = encounterUuid;
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

    public List<DrugOrder> getDrugOrders() {
        return drugOrders;
    }

    public void setDrugOrders(List<DrugOrder> drugOrders) {
        this.drugOrders = drugOrders;
    }

    @JsonSerialize(using = CustomJsonDateSerializer.class)
    public Date getEncounterDateTime() {
        return encounterDateTime == null ? new Date() : encounterDateTime;
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
        private boolean isSet;

        public Concept(String uuid, String name,boolean isSet) {
            this.uuid = uuid;
            this.name = name;
            this.isSet = isSet;
        }

        public Concept(String uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        public Concept(String uuid) {
            this(uuid, null,false);
        }

        public Concept() {
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

        public void setObservationDateTime(Date observationDateTime) {
            this.observationDateTime = observationDateTime;
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
            return dispositionDateTime == null ? new Date() : dispositionDateTime;
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
    }



    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Diagnosis {
        private String order;
        private String certainty;
        private String freeTextAnswer;
        private Concept codedAnswer;
        private String existingObs;
        private Date diagnosisDateTime;

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
            return diagnosisDateTime == null ? new Date() : diagnosisDateTime;
        }

        public void setDiagnosisDateTime(Date date) {
            this.diagnosisDateTime = date;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DrugOrder {
        private String uuid;
        private Concept concept;
        private String notes;
        private Date startDate;
        private Date  endDate;
        private Integer numberPerDosage;
        private Concept dosageInstruction;
        private Concept dosageFrequency;
        private boolean prn;
        private Double doseStrength;
        private String dosageForm;
        private String drugName;
        private String drugUnits;
        private Date dateCreated;
        private Date dateChanged;

        public String getUuid() {
            return uuid;
        }

        public Concept getDosageInstruction() {
            return dosageInstruction;
        }

        public void setDosageInstruction(Concept dosageInstruction) {
            this.dosageInstruction = dosageInstruction;
        }

        public Concept getDosageFrequency() {
            return dosageFrequency;
        }

        public void setDosageFrequency(Concept dosageFrequency) {
            this.dosageFrequency = dosageFrequency;
        }

        @JsonIgnore
        public String getConceptUuid() {
            return concept.getUuid();
        }

        public Concept getConcept() {
            return concept;
        }

        public String getNotes() {
            return notes;
        }

        @JsonSerialize(using = CustomJsonDateSerializer.class)
        public Date getStartDate() {
            return startDate;
        }

        @JsonSerialize(using = CustomJsonDateSerializer.class)
        public Date getEndDate() {
            return endDate;
        }

        public Integer getNumberPerDosage() {
            return numberPerDosage;
        }

        @JsonIgnore
        public String getDosageInstructionUuid() {
            return dosageInstruction == null ? null : dosageInstruction.getUuid();
        }

        @JsonIgnore
        public String getDosageFrequencyUuid() {
            return dosageFrequency == null ?  null : dosageFrequency.getUuid();
        }

        public boolean isPrn() {
            return prn;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public DrugOrder setConcept(Concept concept) {
            this.concept = concept;
            return this;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public void setNumberPerDosage(Integer numberPerDosage) {
            this.numberPerDosage = numberPerDosage;
        }

        public void setPrn(boolean prn) {
            this.prn = prn;
        }

        public void setDoseStrength(Double doseStrength) {
            this.doseStrength = doseStrength;
        }

        public Double getDoseStrength() {
            return doseStrength;
        }

        public void setDosageForm(String dosageForm) {
            this.dosageForm = dosageForm;
        }

        public String getDosageForm() {
            return dosageForm;
        }


        public void setDrugName(String drugName) {
            this.drugName = drugName;
        }

        public String getDrugName() {
            return drugName;
        }

        public void setDrugUnits(String drugUnits) {
            this.drugUnits = drugUnits;
        }

        public String getDrugUnits() {
            return drugUnits;
        }

        public void setDateCreated(Date dateCreated) {
            this.dateCreated = dateCreated;
        }

        @JsonSerialize(using = CustomJsonDateSerializer.class)
        public Date getDateCreated() {
            return dateCreated;
        }

        public void setDateChanged(Date dateChanged) {
            this.dateChanged = dateChanged;
        }

        @JsonSerialize(using = CustomJsonDateSerializer.class)
        public Date getDateChanged() {
            return dateChanged;
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
}
