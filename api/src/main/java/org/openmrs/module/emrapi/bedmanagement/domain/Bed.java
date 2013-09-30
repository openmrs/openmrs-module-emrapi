package org.openmrs.module.emrapi.bedmanagement.domain;

import java.util.Set;

public class Bed {
    private int id;
    private String number;
    private Set<BedPatientAssignment> bedPatientAssignment;

    public Set<BedPatientAssignment> getBedPatientAssignment() {
        return bedPatientAssignment;
    }

    public void setBedPatientAssignment(Set<BedPatientAssignment> bedPatientAssignment) {
        this.bedPatientAssignment = bedPatientAssignment;
    }

}
