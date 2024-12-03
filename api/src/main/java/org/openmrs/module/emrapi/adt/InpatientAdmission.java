package org.openmrs.module.emrapi.adt;

import lombok.Data;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Represents a hospital Admission
 */
@Data
public class InpatientAdmission {

    private Visit visit;
    private Patient patient;
    private InpatientRequest currentInpatientRequest;
    private Set<Encounter> admissionEncounters = new TreeSet<>(getEncounterComparator());
    private Set<Encounter> transferEncounters = new TreeSet<>(getEncounterComparator());
    private Set<Encounter> dischargeEncounters = new TreeSet<>(getEncounterComparator());

    public List<Encounter> getAdmissionAndTransferEncounters() {
        List<Encounter> encounters = new ArrayList<>();
        encounters.addAll(admissionEncounters);
        encounters.addAll(transferEncounters);
        encounters.sort(getEncounterComparator());
        return Collections.unmodifiableList(encounters);
    }

    public List<Encounter> getAdtEncounters() {
        List<Encounter> encounters = new ArrayList<>();
        encounters.addAll(admissionEncounters);
        encounters.addAll(transferEncounters);
        encounters.addAll(dischargeEncounters);
        encounters.sort(getEncounterComparator());
        return Collections.unmodifiableList(encounters);
    }

    public Encounter getFirstAdmissionOrTransferEncounter() {
        List<Encounter> encounters = getAdmissionAndTransferEncounters();
        return encounters.isEmpty() ? null : encounters.get(0);
    }

    public Encounter getLatestAdmissionOrTransferEncounter() {
        List<Encounter> encounters = getAdmissionAndTransferEncounters();
        return encounters.isEmpty() ? null : encounters.get(encounters.size() - 1);
    }

    public Encounter getLatestAdtEncounter() {
        List<Encounter> encounters = getAdtEncounters();
        return encounters.isEmpty() ? null : encounters.get(encounters.size() - 1);
    }

    public Location getCurrentInpatientLocation() {
        if (isDischarged()) {
            return null;
        }
        Encounter encounter = getLatestAdmissionOrTransferEncounter();
        return encounter == null ? null : encounter.getLocation();
    }

    public Encounter getEncounterAssigningToCurrentInpatientLocation() {
        Location location = getCurrentInpatientLocation();
        if (location == null) {
            return null;
        }
        List<Encounter> encounters = getAdmissionAndTransferEncounters();
        if (encounters.isEmpty()) {
            return null;
        }
        Encounter ret = encounters.get(encounters.size() - 1);
        if (!ret.getLocation().equals(location)) { // Sanity check, this should not happen
            return null;
        }
        for (int i=encounters.size()-2; i>=0; i--) {
            Encounter e = encounters.get(i);
            if (e.getLocation().equals(location)) {
                ret = e;
            }
            else {
                return ret;
            }
        }
        return ret;
    }

    public boolean isDischarged() {
        if (dischargeEncounters.isEmpty()) {
            return false;
        }
        return dischargeEncounters.contains(getLatestAdtEncounter());
    }

    private Comparator<Encounter> getEncounterComparator() {
        return Comparator.comparing(Encounter::getEncounterDatetime)
                .thenComparing(Encounter::getDateCreated)
                .thenComparing(Encounter::getEncounterId);
    }

    public List<Encounter> getAllEncounters() {
        return Collections.unmodifiableList(visit.getEncounters().stream().filter(e -> !e.getVoided()).sorted(getEncounterComparator()).collect(Collectors.toList()));
    }

    public Encounter getFirstEncounter() {
        List<Encounter> encounters = getAllEncounters();
        return encounters.isEmpty() ? null : encounters.get(0);
    }

    public Encounter getLatestEncounter() {
        List<Encounter> encounters = getAllEncounters();
        return encounters.isEmpty() ? null : encounters.get(encounters.size() - 1);
    }
}
