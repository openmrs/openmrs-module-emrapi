package org.openmrs.module.emrapi.bedmanagement;

import org.openmrs.api.OpenmrsService;
import org.openmrs.module.emrapi.bedmanagement.domain.AdmissionLocation;

import java.util.List;

public interface BedManagementService extends OpenmrsService {
    List<AdmissionLocation> getAllAdmissionLocations();
}
