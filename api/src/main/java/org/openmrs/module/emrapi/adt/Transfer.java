package org.openmrs.module.emrapi.adt;

import org.openmrs.EncounterRole;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.Visit;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Helper class that captures details about transfering a patient within the hospital.
 * Use {@link AdtService} to save one of these (which actually creates and returns an encounter).
 * (This is a placeholder for a class representing a proper transfer note. Eventually this class will be extended to
 * include more properties, and support obs and orders too.)
 */
public class Transfer {

    private Visit visit;
    private Location toLocation;
    private Date transferDatetime;
    private Map<EncounterRole, Set<Provider>> providers;

    public Transfer() {
    }

    public Transfer(Visit visit, Location toLocation, Map<EncounterRole, Set<Provider>> providers) {
        this.visit = visit;
        this.toLocation = toLocation;
        this.providers = providers;
    }

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public Location getToLocation() {
        return toLocation;
    }

    public void setToLocation(Location toLocation) {
        this.toLocation = toLocation;
    }

    public Date getTransferDatetime() {
        return transferDatetime;
    }

    public void setTransferDatetime(Date transferDatetime) {
        this.transferDatetime = transferDatetime;
    }

    public Map<EncounterRole, Set<Provider>> getProviders() {
        return providers;
    }

    public void setProviders(Map<EncounterRole, Set<Provider>> providers) {
        this.providers = providers;
    }

}
