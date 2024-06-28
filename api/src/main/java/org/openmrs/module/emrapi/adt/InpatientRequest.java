package org.openmrs.module.emrapi.adt;

import lombok.Data;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.disposition.DispositionType;

import java.util.Date;

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
    private Date dispositionDate;
}
