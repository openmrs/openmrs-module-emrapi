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
}
