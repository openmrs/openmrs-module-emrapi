package org.openmrs.module.emrapi.disposition.actions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.LocationService;
import org.openmrs.module.emrapi.TestUtils;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.Transfer;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.emrapi.test.AuthenticatedUserTestHelper;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class TransferToSpecificLocationDispositionActionTest extends AuthenticatedUserTestHelper {

    private TransferToSpecificLocationDispositionAction action;
    private AdtService adtService;
    private LocationService locationService;

    @Before
    public void setUp() throws Exception {
        locationService = mock(LocationService.class);
        adtService = mock(AdtService.class);

        action = new TransferToSpecificLocationDispositionAction();
        action.setLocationService(locationService);
        action.setAdtService(adtService);
    }

    @Test
    public void testAction() throws Exception {
        Map<String, String[]> request = new HashMap<String, String[]>();
        request.put(TransferToSpecificLocationDispositionAction.TRANSFER_LOCATION_PARAMETER, new String[] { "7" });
        request.put("something", new String[] { "unrelated" });

        final Location toLocation = new Location();
        when(locationService.getLocation(7)).thenReturn(toLocation);

        final Visit visit = new Visit();
        final Encounter encounter = new Encounter();
        encounter.setVisit(visit);
        encounter.addProvider(new EncounterRole(), new Provider());

        action.action(new EncounterDomainWrapper(encounter), new Obs(), request);
        verify(adtService).transferPatient(argThat(new ArgumentMatcher<Transfer>() {
            @Override
            public boolean matches(Object argument) {
                Transfer actual = (Transfer) argument;
                return actual.getVisit().equals(visit) && actual.getToLocation().equals(toLocation) && TestUtils.sameProviders(actual.getProviders(), encounter.getProvidersByRoles());
            }
        }));
    }

}
