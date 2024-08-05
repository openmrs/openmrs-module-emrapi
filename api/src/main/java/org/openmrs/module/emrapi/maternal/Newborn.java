package org.openmrs.module.emrapi.maternal;

import lombok.Data;
import org.openmrs.Patient;
import org.openmrs.Visit;

@Data
public class Newborn {
    private Patient newborn;
    private Visit newbornVisit;
}
