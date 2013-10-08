package org.openmrs.module.emrapi.bedmanagement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.Patient;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class BedManagementServiceImplTest {

    BedManagementServiceImpl bedManagementService;
    @Mock
    BedManagementDAO bedManagementDAO;

    @Before
    public void setup() {
        bedManagementService = new BedManagementServiceImpl();
        bedManagementService.setDao(bedManagementDAO);
    }

    @Test
    public void should_get_layouts_for_ward() {
        String wardId = "123";

        Location location = new Location();
        location.setUuid(wardId);

        bedManagementService.getLayoutForWard(location);

        verify(bedManagementDAO).getLayoutForWard(location);
    }

    @Test
    public void should_be_able_to_assign_patient_to_bed() {
        Patient patient = new Patient();
        Bed bed = new Bed();
        bedManagementService.assignPatientToBed(patient, bed);

        verify(bedManagementDAO).assignPatientToBed(patient, bed);
    }
}
