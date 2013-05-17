package org.openmrs.module.emrapi.adt;

import org.openmrs.EncounterRole;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.Visit;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Helper class that captures details about discharging a patient from the hospital.
 * Use {@link AdtService} to save one of these (which actually creates and returns an encounter).
 * (This is a placeholder for a class representing a proper discharge note. Eventually this class will be extended to
 * include more properties, and support obs and orders too.)
 */
public class Discharge {

    private Visit visit;
    private Location location;
    private Date dischargeDatetime;
    private Map<EncounterRole, Set<Provider>> providers;

    public Discharge() {
    }

    public Discharge(Visit visit, Location location, Map<EncounterRole, Set<Provider>> providers) {
        this.visit = visit;
        this.location = location;
        this.providers = providers;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public Visit getVisit() {
        return visit;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Date getDischargeDatetime() {
        return dischargeDatetime;
    }

    public void setDischargeDatetime(Date dischargeDatetime) {
        this.dischargeDatetime = dischargeDatetime;
    }

    public Map<EncounterRole, Set<Provider>> getProviders() {
        return providers;
    }

    public void setProviders(Map<EncounterRole, Set<Provider>> providers) {
        this.providers = providers;
    }

}
