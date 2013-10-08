package org.openmrs.module.emrapi.bedmanagement;

import java.util.Set;

public class Bed {
    private int id;
    private String number;
    private Set<BedPatientAssignment> bedPatientAssignment;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Set<BedPatientAssignment> getBedPatientAssignment() {
        return bedPatientAssignment;
    }

    public void setBedPatientAssignment(Set<BedPatientAssignment> bedPatientAssignment) {
        this.bedPatientAssignment = bedPatientAssignment;
    }
}
