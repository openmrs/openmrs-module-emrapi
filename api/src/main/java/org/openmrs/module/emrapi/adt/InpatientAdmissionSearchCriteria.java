package org.openmrs.module.emrapi.adt;

import lombok.Data;
import org.openmrs.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents criteria for searching for InpatientAdmissions
 */
@Data
public class InpatientAdmissionSearchCriteria {

    private Location visitLocation;
    private List<Location> currentInpatientLocations;
    private boolean includeDischarged = false;
    private List<String> patients;
    private List<String> visits;

    public void addCurrentInpatientLocation(Location location) {
        if (currentInpatientLocations == null) {
            currentInpatientLocations = new ArrayList<>();
        }
        currentInpatientLocations.add(location);
    }

    public void addPatientUuid(String patientUuid) {
        if (patients == null) {
            patients = new ArrayList<>();
        }
        patients.add(patientUuid);
    }

    public void addVisitUuid(String visitUuid) {
        if (visits == null) {
            visits = new ArrayList<>();
        }
        visits.add(visitUuid);
    }
}
