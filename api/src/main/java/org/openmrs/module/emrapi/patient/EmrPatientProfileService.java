package org.openmrs.module.emrapi.patient;

import org.openmrs.annotation.Authorized;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface EmrPatientProfileService {
    @Transactional
    @Authorized(PrivilegeConstants.EDIT_PATIENTS)
    PatientProfile save(PatientProfile patientProfile);
    @Authorized(PrivilegeConstants.GET_PATIENTS)
    PatientProfile get(String patientUuid);
}
