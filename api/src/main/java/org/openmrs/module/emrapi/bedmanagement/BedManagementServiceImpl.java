package org.openmrs.module.emrapi.bedmanagement;

import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.bedmanagement.domain.AdmissionLocation;

import java.util.List;

public class BedManagementServiceImpl extends BaseOpenmrsService implements BedManagementService {

    BedManagementDAO bedManagementDao;

    public void setBedManagementDao(BedManagementDAO bedManagementDao) {
        this.bedManagementDao = bedManagementDao;
    }

    @Override
    public List<AdmissionLocation> getAllAdmissionLocations() {
        return bedManagementDao.getAllLocationsBy(EmrApiConstants.LOCATION_TAG_SUPPORTS_ADMISSION);
    }
}
