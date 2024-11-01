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
    private List<Integer> patientIds;
    private List<Integer> visitIds;

    public void addCurrentInpatientLocation(Location location) {
        if (currentInpatientLocations == null) {
            currentInpatientLocations = new ArrayList<>();
        }
        currentInpatientLocations.add(location);
    }

    public void addPatientId(Integer patientId) {
        if (patientIds == null) {
            patientIds = new ArrayList<>();
        }
        patientIds.add(patientId);
    }

    public void addVisitId(Integer visitId) {
        if (visitIds == null) {
            visitIds = new ArrayList<>();
        }
        visitIds.add(visitId);
    }
}
