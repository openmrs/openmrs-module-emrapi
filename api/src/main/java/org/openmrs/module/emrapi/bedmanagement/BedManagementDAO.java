package org.openmrs.module.emrapi.bedmanagement;

import org.openmrs.module.emrapi.bedmanagement.domain.AdmissionLocation;

import java.util.List;

public interface BedManagementDAO {
    List<AdmissionLocation> getAllLocationsBy(String locationTagSupportsAdmission);
}
