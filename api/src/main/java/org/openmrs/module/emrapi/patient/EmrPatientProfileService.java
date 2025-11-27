package org.openmrs.module.emrapi.patient;

import org.openmrs.annotation.Authorized;
import org.openmrs.util.PrivilegeConstants;

public interface EmrPatientProfileService {
    @Authorized(PrivilegeConstants.EDIT_PATIENTS)
    PatientProfile save(PatientProfile patientProfile);
    @Authorized(PrivilegeConstants.VIEW_PATIENTS)
    PatientProfile get(String patientUuid);
}
