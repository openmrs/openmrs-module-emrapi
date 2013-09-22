package org.openmrs.module.emrapi.encounter.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class EncounterTransaction {
    private String locationUuid;
    private Set<String> providerUuid;
    private String patientUuid;     // TODO: mandatory validation
    private String visitTypeUuid;  // TODO: mandatory validation
    private String encounterTypeUuid;   // TODO: mandatory validation
    private Date encounterDateTime;

    private List<Observation> observations = new ArrayList<Observation>();

    private List<TestOrder> testOrders = new ArrayList<TestOrder>();

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

    public EncounterTransaction setTestOrders(List<TestOrder> testOrders) {
        this.testOrders = testOrders;
        return this;
    }

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

    public Set<String> getProviderUuids() {
        return providerUuid;
    }

    public EncounterTransaction setProviderUuid(Set<String> providerUuid) {
        this.providerUuid = providerUuid;
        return this;
    }

    public static class Observation {
        private String observationUuid;
        private String conceptUuid;     // TODO: mandatory validation
        private Object value;
        private String comment;
        private boolean voided;
        private String voidReason;

        public String getObservationUuid() {
            return observationUuid;
        }

        public Observation setObservationUuid(String observationUuid) {
            this.observationUuid = observationUuid;
            return this;
        }

        public String getConceptUuid() {
            return conceptUuid;
        }

        public Observation setConceptUuid(String conceptUuid) {
            this.conceptUuid = conceptUuid;
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

        public boolean isVoided() {
            return voided;
        }

        public String getVoidReason() {
            return voidReason;
        }

        public Observation setVoidReason(String voidReason) {
            this.voidReason = voidReason;
            return this;
        }
    }

    public static class TestOrder {
        private String conceptUuid;     // TODO: mandatory validation
        private String instructions;
        private String uuid;
        private boolean voided;
        private String voidReason;

        public String getConceptUuid() {
            return conceptUuid;
        }

        public TestOrder setConceptUuid(String conceptUuid) {
            this.conceptUuid = conceptUuid;
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
    }
}