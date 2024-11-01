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
    private List<Integer> patientIds;
    private List<Integer> visitIds;

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
