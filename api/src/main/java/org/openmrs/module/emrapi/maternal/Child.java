package org.openmrs.module.emrapi.maternal;

import lombok.Data;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.adt.InpatientAdmission;

@Data
public class Child {
    private Patient child;
    private Patient mother;
    private InpatientAdmission childAdmission;
}
