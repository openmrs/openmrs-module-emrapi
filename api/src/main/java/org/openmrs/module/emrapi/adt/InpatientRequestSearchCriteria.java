package org.openmrs.module.emrapi.adt;

import lombok.Data;
import org.openmrs.Location;
import org.openmrs.module.emrapi.disposition.DispositionType;

import java.util.List;

/**
 * Represents criteria for searching for AdtRequests
 */
@Data
public class InpatientRequestSearchCriteria {
    private Location visitLocation;
    private List<Location> dispositionLocations;
    private List<DispositionType> dispositionTypes;
}
