package org.openmrs.module.emrapi.maternal;

import lombok.Data;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.adt.InpatientAdmission;


@Data
public class Mother {
    private Patient mother;
    private Patient newborn;
    private InpatientAdmission motherAdmission;
}
