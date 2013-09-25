package org.openmrs.module.emrapi.bedmanagement.domain;

import org.openmrs.Patient;

import java.util.Date;

public class BedPatientAssignment {
    private int id;
    private Bed bed;
    private Patient patient;
    private Date startDateTime;
    private Date endDateTime;
}
