package org.openmrs.module.emrapi.adt.exception;

public class InvalidAdtEncounterException extends IllegalArgumentException {
  public static final String PATIENT_NOT_ADMITTED_CODE = "emrapi.encounter.adt.error.patientNotAdmitted";
  public static final String PATIENT_ALREADY_ADMITTED_CODE = "emrapi.encounter.adt.error.patientAlreadyAdmitted";
  public static final String PATIENT_ALREADY_AT_LOCATION_CODE = "emrapi.encounter.adt.error.patientAlreadyAtLocation";

  private String code;

  public InvalidAdtEncounterException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
