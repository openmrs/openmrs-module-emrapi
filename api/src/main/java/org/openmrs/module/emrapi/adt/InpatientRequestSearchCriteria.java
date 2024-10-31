package org.openmrs.module.emrapi.adt;

import lombok.Data;
import org.openmrs.Location;
import org.openmrs.module.emrapi.disposition.DispositionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents criteria for searching for AdtRequests
 * Currently the assumption is that all requests returned are active, and this will be the default regardless
 */
@Data
public class InpatientRequestSearchCriteria {

    private Location visitLocation;
    private List<Location> dispositionLocations;
    private List<DispositionType> dispositionTypes;
    private List<String> patients;
    private List<String> visits;

    public void addDispositionLocation(Location location) {
        if (dispositionLocations == null) {
            dispositionLocations = new ArrayList<>();
        }
        dispositionLocations.add(location);
    }

    public void addDispositionType(DispositionType dispositionType) {
        if (dispositionTypes == null) {
            dispositionTypes = new ArrayList<>();
        }
        dispositionTypes.add(dispositionType);
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
