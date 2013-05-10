package org.openmrs.module.emrapi.adt;

import org.openmrs.Location;
import org.openmrs.Patient;

import java.util.Date;

/**
 * Helper class that captures details about admitting a patient to the hospital.
 * Use {@link AdtService} to save one of these (which actually creates and returns an encounter).
 * (This is a placeholder for a class representing a proper admission note. Eventually this class will be extended to
 * include more properties, and support obs and orders too.)
 */
public class Admission {

    private Patient patient;
    private Date admitDatetime;
    private Location location;

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setAdmitDatetime(Date admitDatetime) {
        this.admitDatetime = admitDatetime;
    }

    public Date getAdmitDatetime() {
        return admitDatetime;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
