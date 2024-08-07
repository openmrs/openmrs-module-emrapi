package org.openmrs.module.emrapi.maternal;

import lombok.Data;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.adt.InpatientAdmission;


@Data
public class MotherAndChild {
    private Patient mother;
    private Patient child;
    private InpatientAdmission motherAdmission;
    private InpatientAdmission childAdmission;
}
