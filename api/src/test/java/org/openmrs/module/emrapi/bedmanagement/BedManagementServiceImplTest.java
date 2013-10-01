package org.openmrs.module.emrapi.bedmanagement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
        bedManagementService.getLayoutForWard(wardId);

        verify(bedManagementDAO).getLayoutForWard(wardId);
    }
}
