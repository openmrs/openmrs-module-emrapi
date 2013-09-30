package org.openmrs.module.emrapi.bedmanagement;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.bedmanagement.AdmissionLocation;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BedManagementServiceTest {
    @Test
    public void getAllAdmissionLocations_gets_locations_that_support_admission() {
        ArrayList<AdmissionLocation> expectedWards = new ArrayList<AdmissionLocation>();

        BedManagementDAO bedManagementDao = mock(BedManagementDAO.class);
        when(bedManagementDao.getAllLocationsBy(EmrApiConstants.LOCATION_TAG_SUPPORTS_ADMISSION)).thenReturn(expectedWards);

        BedManagementServiceImpl bedManagementService = new BedManagementServiceImpl();
        bedManagementService.setDao(bedManagementDao);

        List<AdmissionLocation> wards = bedManagementService.getAllAdmissionLocations();
        Assert.assertSame(expectedWards, wards);
    }
}
