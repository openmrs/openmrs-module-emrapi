package org.openmrs.module.emrapi.adt;

import org.openmrs.EncounterRole;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.Visit;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Helper class that captures details about admitting, discharging and transfering a patient within the hospital.
 * Use {@link AdtService} to save one of these (which actually creates and returns an encounter).
 * (This is a placeholder for a class representing a proper transfer note. Eventually this class will be extended to
 * include more properties, and support obs and orders too.)
 */
public class AdtAction {

    private Visit visit;
    private Location location;
    private Date actionDatetime;
    private Map<EncounterRole, Set<Provider>> providers;

    public AdtAction() {
    }

    public AdtAction(Visit visit, Location toLocation, Map<EncounterRole, Set<Provider>> providers) {
        this.visit = visit;
        this.location = toLocation;
        this.providers = providers;
    }

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Date getActionDatetime() {
        return actionDatetime;
    }

    public void setActionDatetime(Date actionDatetime) {
        this.actionDatetime = actionDatetime;
    }

    public Map<EncounterRole, Set<Provider>> getProviders() {
        return providers;
    }

    public void setProviders(Map<EncounterRole, Set<Provider>> providers) {
        this.providers = providers;
    }

}
