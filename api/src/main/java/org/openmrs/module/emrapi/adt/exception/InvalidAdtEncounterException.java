package org.openmrs.module.emrapi.adt.exception;

import java.util.Date;

import org.openmrs.Location;

public class InvalidAdtEncounterException extends IllegalArgumentException {

    public enum Type {
        PATIENT_NOT_ADMITTED("emrapi.encounter.adt.error.patientNotAdmitted"),
        PATIENT_ALREADY_ADMITTED("emrapi.encounter.adt.error.patientAlreadyAdmitted"),
        PATIENT_ALREADY_AT_LOCATION("emrapi.encounter.adt.error.patientAlreadyAtLocation");

        private final String code;

        Type(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    private Type type;
    private Location location; // nullable
    private Date date;

    public InvalidAdtEncounterException(Type type, Location location, Date date) {
        this.type = type;
        this.location = location;
        this.date = date;
    }

    public Type getType() {
        return type;
    }

    public String getCode() {
        return type.code;
    }

    public Location getLocation() {
        return location;
    }

    public Date getDate() {
        return date;
    }
}
