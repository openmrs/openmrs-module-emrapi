package org.openmrs.module.emrapi.encounter;


import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;

import java.util.Date;
import java.util.List;

public class EncounterParameters {

    private Location location;
    private EncounterType encounterType;
    private List<Provider> providers;
    private Date encounterDateTime;
    private Patient patient;

    private EncounterParameters(){

    }

    public static EncounterParameters instance() {
        return new EncounterParameters();
    }

    public EncounterParameters setLocation(Location location) {
        this.location = location;
        return this;
    }

    public EncounterParameters setEncounterType(EncounterType encounterType) {
        this.encounterType = encounterType;
        return this;
    }

    public EncounterParameters setProviders(List<Provider> providers) {
        this.providers = providers;
        return this;
    }

    public EncounterParameters setEncounterDateTime(Date encounterDateTime) {
        this.encounterDateTime = encounterDateTime;
        return this;
    }

    public EncounterParameters setPatient(Patient patient) {
        this.patient = patient;
        return this;
    }

    public Location getLocation() {
        return location;
    }

    public EncounterType getEncounterType() {
        return encounterType;
    }

    public List<Provider> getProviders() {
        return providers;
    }

    public Date getEncounterDateTime() {
        return encounterDateTime;
    }

    public Patient getPatient() {
        return patient;
    }
}
