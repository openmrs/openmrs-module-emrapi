package org.openmrs.module.emrapi.maternal;

import lombok.Data;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.adt.InpatientAdmission;

@Data
public class Newborn {
    private Patient newborn;
    private Patient mother;
    private InpatientAdmission newbornAdmission;
}
