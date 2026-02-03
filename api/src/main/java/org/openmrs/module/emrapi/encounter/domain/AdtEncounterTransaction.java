package org.openmrs.module.emrapi.encounter.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction.Observation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction.Provider;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdtEncounterTransaction {
    private String visitUuid;
    private String locationUuid;
    private String encounterTypeUuid;
    private Date encounterDateTime;
    private List<Observation> observations = new ArrayList<Observation>();
    private Set<Provider> providers = new HashSet<Provider>();

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getLocationUuid() {
        return locationUuid;
    }

    public void setLocationUuid(String locationUuid) {
        this.locationUuid = locationUuid;
    }

    public String getEncounterTypeUuid() {
        return encounterTypeUuid;
    }

    public void setEncounterTypeUuid(String encounterTypeUuid) {
        this.encounterTypeUuid = encounterTypeUuid;
    }

    public Date getEncounterDateTime() {
        return encounterDateTime;
    }

    public void setEncounterDateTime(Date encounterDateTime) {
        this.encounterDateTime = encounterDateTime;
    }

    public List<Observation> getObservations() {
        return observations;
    }

    public void setObservations(List<Observation> observations) {
        this.observations = observations;
    }

    public Set<Provider> getProviders() {
        return providers;
    }

    public void setProviders(Set<Provider> providers) {
        this.providers = providers;
    }
}

