package org.openmrs.module.emrapi.disposition.actions;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.LocationService;
import org.openmrs.module.emrapi.TestUtils;
import org.openmrs.module.emrapi.adt.AdtAction;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.emrapi.test.AuthenticatedUserTestHelper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class TransferToSpecificLocationDispositionActionTest extends AuthenticatedUserTestHelper {

    private TransferToSpecificLocationDispositionAction action;
    private AdtService adtService;
    private LocationService locationService;
    private DispositionService dispositionService;
    private DispositionDescriptor dispositionDescriptor;
    private VisitDomainWrapper visitDomainWrapper;
    private Concept dispositionObsGroupConcept = new Concept();


    @Before
    public void setUp() throws Exception {
        locationService = mock(LocationService.class);
        adtService = mock(AdtService.class);
        dispositionService = mock(DispositionService.class);
        dispositionDescriptor = mock(DispositionDescriptor.class);
        visitDomainWrapper = mock(VisitDomainWrapper.class);

        when(dispositionService.getDispositionDescriptor()).thenReturn(dispositionDescriptor);
        when(adtService.wrap(any(Visit.class))).thenReturn(visitDomainWrapper);

        action = new TransferToSpecificLocationDispositionAction();
        action.setLocationService(locationService);
        action.setAdtService(adtService);
        action.setDispositionService(dispositionService);
    }

    @Test
    public void testActionShouldCreateTransferAction() throws Exception {

        final Location toLocation = new Location();
        final Visit visit = new Visit();
        final Encounter encounter = new Encounter();
        final Date encounterDate = (new DateTime(2013, 05, 13, 20, 26)).toDate();
        encounter.setVisit(visit);
        encounter.addProvider(new EncounterRole(), new Provider());
        encounter.setEncounterDatetime(encounterDate);

        final Obs dispositionObsGroup = new Obs();
        dispositionObsGroup.setConcept(dispositionObsGroupConcept);
        encounter.addObs(dispositionObsGroup);

        Location anotherLocation = new Location();
        when(visitDomainWrapper.isAdmitted(encounterDate)).thenReturn(true);
        when(visitDomainWrapper.getInpatientLocation(encounterDate)).thenReturn(anotherLocation);
        when(dispositionDescriptor.getInternalTransferLocation(dispositionObsGroup, locationService)).thenReturn(toLocation);

        action.action(new EncounterDomainWrapper(encounter), dispositionObsGroup, null);
        verify(adtService).createAdtEncounterFor(argThat(new ArgumentMatcher<AdtAction>() {
            @Override
            public boolean matches(Object argument) {
                AdtAction actual = (AdtAction) argument;
                return actual.getVisit().equals(visit) &&
                        actual.getLocation().equals(toLocation) &&
                        TestUtils.sameProviders(actual.getProviders(), encounter.getProvidersByRoles()) &&
                        actual.getActionDatetime().equals(encounterDate) &&
                        actual.getType().equals(AdtAction.Type.TRANSFER);
            }
        }));
    }

    @Test
    public void testActionShouldDoNothingIfAlreadyAdmittedToTransferLocation() throws Exception  {

        final Location toLocation = new Location();
        final Visit visit = new Visit();
        final Encounter encounter = new Encounter();
        final Date encounterDate = (new DateTime(2013, 05, 13, 20, 26)).toDate();
        encounter.setVisit(visit);
        encounter.addProvider(new EncounterRole(), new Provider());
        encounter.setEncounterDatetime(encounterDate);

        final Obs dispositionObsGroup = new Obs();
        dispositionObsGroup.setConcept(dispositionObsGroupConcept);
        encounter.addObs(dispositionObsGroup);

        when(visitDomainWrapper.isAdmitted(encounterDate)).thenReturn(true);
        when(visitDomainWrapper.getInpatientLocation(encounterDate)).thenReturn(toLocation);  // current location the same as transfer location
        when(dispositionDescriptor.getInternalTransferLocation(dispositionObsGroup, locationService)).thenReturn(toLocation);

        action.action(new EncounterDomainWrapper(encounter), dispositionObsGroup, null);
        verify(adtService, never()).createAdtEncounterFor(any(AdtAction.class));
    }

    @Test
    public void testActionShouldCreateTransferEncounterIfPatientNotAdmitted() throws Exception {

        final Location toLocation = new Location();
        final Visit visit = new Visit();
        final Encounter encounter = new Encounter();
        final Date encounterDate = (new DateTime(2013, 05, 13, 20, 26)).toDate();
        encounter.setVisit(visit);
        encounter.addProvider(new EncounterRole(), new Provider());
        encounter.setEncounterDatetime(encounterDate);

        final Obs dispositionObsGroup = new Obs();
        dispositionObsGroup.setConcept(dispositionObsGroupConcept);
        encounter.addObs(dispositionObsGroup);

        when(visitDomainWrapper.isAdmitted(encounterDate)).thenReturn(false);
        when(dispositionDescriptor.getInternalTransferLocation(dispositionObsGroup, locationService)).thenReturn(toLocation);

        action.action(new EncounterDomainWrapper(encounter), dispositionObsGroup, null);

        verify(adtService).createAdtEncounterFor(argThat(new ArgumentMatcher<AdtAction>() {
            @Override
            public boolean matches(Object argument) {
                AdtAction actual = (AdtAction) argument;
                return actual.getVisit().equals(visit) &&
                        actual.getLocation().equals(toLocation) &&
                        TestUtils.sameProviders(actual.getProviders(), encounter.getProvidersByRoles()) &&
                        actual.getActionDatetime().equals(encounterDate) &&
                        actual.getType().equals(AdtAction.Type.TRANSFER);
            }
        }));

    }

}
