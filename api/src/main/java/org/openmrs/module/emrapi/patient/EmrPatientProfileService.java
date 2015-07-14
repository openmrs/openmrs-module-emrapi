package org.openmrs.module.emrapi.patient;

public interface EmrPatientProfileService {
    PatientProfile save(PatientProfile patientProfile);
    PatientProfile get(String patientUuid);
}
