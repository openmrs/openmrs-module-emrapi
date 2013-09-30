package org.openmrs.module.emrapi.bedmanagement.db;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.bedmanagement.BedManagementDAO;
import org.openmrs.module.emrapi.bedmanagement.domain.AdmissionLocation;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BedManagementDaoComponentTest extends BaseModuleContextSensitiveTest {
    @Autowired
    private BedManagementDAO bedManagementDao;

    @Before
    public void beforeAllTests() throws Exception {
        executeDataSet("bedManagementDAOComponentTestDataset.xml");
    }

    @Test
    public void getAllLocationsBy_gets_locations_for_a_tag() {
        List<AdmissionLocation> admissionLocationList = bedManagementDao.getAllLocationsBy(EmrApiConstants.LOCATION_TAG_SUPPORTS_ADMISSION);
        assertThat(admissionLocationList.size(), is(2));

        AdmissionLocation cardioWard = getWard(admissionLocationList, "Cardio ward on first floor");
        Assert.assertEquals(10, cardioWard.getTotalBeds());
        Assert.assertEquals(1, cardioWard.getOccupiedBeds());

        AdmissionLocation orthoWard = getWard(admissionLocationList, "Orthopaedic ward");
        Assert.assertEquals(4, orthoWard.getTotalBeds());
        Assert.assertEquals(2, orthoWard.getOccupiedBeds());
    }

    private AdmissionLocation getWard(List<AdmissionLocation> admissionLocationList, String wardName) {
        for (AdmissionLocation admissionLocation : admissionLocationList) {
            if(admissionLocation.getName().equals(wardName))
                return admissionLocation;
        }
        return null;
    }

}
