package org.openmrs.module.emrapi.bedmanagement.db;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.bedmanagement.AdmissionLocation;
import org.openmrs.module.emrapi.bedmanagement.BedManagementService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class BedManagementServiceComponentTest extends BaseModuleContextSensitiveTest {
    @Autowired
    private BedManagementService bedManagementService;

    @Before
    public void beforeAllTests() throws Exception {
        executeDataSet("bedManagementDAOComponentTestDataset.xml");
    }

    @Test
    public void getAllLocationsBy_gets_locations_for_a_tag() {
        List<AdmissionLocation> admissionLocationList = bedManagementService.getAllAdmissionLocations();
        assertThat(admissionLocationList.size(), is(2));

        AdmissionLocation cardioWard = getWard(admissionLocationList, "Cardio ward on first floor");
        Assert.assertEquals(10, cardioWard.getTotalBeds());
        Assert.assertEquals(1, cardioWard.getOccupiedBeds());

        AdmissionLocation orthoWard = getWard(admissionLocationList, "Orthopaedic ward");
        Assert.assertEquals(4, orthoWard.getTotalBeds());
        Assert.assertEquals(2, orthoWard.getOccupiedBeds());
    }

    @Test
    public void getBedsForWard_gets_all_bed_layouts_for_ward() {
        LocationService locationService = Context.getLocationService();

        Location ward = locationService.getLocationByUuid("19e023e8-20ee-4237-ade6-9e68f897b7a9");
        AdmissionLocation admissionLocation = bedManagementService.getLayoutForWard(ward);

        assertTrue(admissionLocation.getBedLayouts().size() == 6);
    }

    private AdmissionLocation getWard(List<AdmissionLocation> admissionLocationList, String wardName) {
        for (AdmissionLocation admissionLocation : admissionLocationList) {
            if(admissionLocation.getWard().getName().equals(wardName))
                return admissionLocation;
        }
        return null;
    }
}
