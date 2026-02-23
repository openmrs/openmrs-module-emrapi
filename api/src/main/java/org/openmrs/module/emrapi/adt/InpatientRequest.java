package org.openmrs.module.emrapi.adt;

import lombok.Data;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.disposition.DispositionType;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents and Admission, Discharge, or Transfer request
 */
@Data
public class InpatientRequest {
    private Visit visit;
    private Patient patient;
    private DispositionType dispositionType;
    private Encounter dispositionEncounter;
    private Obs dispositionObsGroup;
    private Concept disposition;
    private Location dispositionLocation;

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

    private Comparator<Encounter> getEncounterComparator() {
        return Comparator.comparing(Encounter::getEncounterDatetime)
                .thenComparing(Encounter::getDateCreated)
                .thenComparing(Encounter::getEncounterId);
    }
}
