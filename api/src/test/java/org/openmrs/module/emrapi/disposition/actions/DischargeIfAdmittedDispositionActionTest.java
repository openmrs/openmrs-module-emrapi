package org.openmrs.module.emrapi.disposition.actions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.TestUtils;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.Discharge;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.emrapi.test.AuthenticatedUserTestHelper;

import java.util.Date;
import java.util.HashMap;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 *
 */
public class DischargeIfAdmittedDispositionActionTest extends AuthenticatedUserTestHelper {

    private DischargeIfAdmittedDispositionAction action;
    private AdtService adtService;
    private EmrApiProperties emrApiProperties;
    private EncounterType admissionEncounterType = new EncounterType();
    private EncounterType dischargeEncounterType = new EncounterType();

    @Before
    public void setUp() throws Exception {
        adtService = mock(AdtService.class);

        emrApiProperties = mock(EmrApiProperties.class);
        when(emrApiProperties.getAdmissionEncounterType()).thenReturn(admissionEncounterType);
        when(emrApiProperties.getDischargeEncounterType()).thenReturn(dischargeEncounterType);

        action = new DischargeIfAdmittedDispositionAction();
        action.setAdtService(adtService);
        action.setEmrApiProperties(emrApiProperties);
    }

    @Test
    public void testDoesNothingIfNotAdmitted() throws Exception {
        Encounter beingCreated = new Encounter();
        beingCreated.setVisit(new Visit());

        action.action(new EncounterDomainWrapper(beingCreated), new Obs(), new HashMap<String, String[]>());

        verifyZeroInteractions(adtService);
    }

    @Test
    public void testDischargesIfAdmitted() throws Exception {
        Encounter admission = new Encounter();
        admission.setEncounterDatetime(new Date());
        admission.setEncounterType(admissionEncounterType);

        Visit visit = new Visit();
        visit.addEncounter(admission);

        final Encounter beingCreated = new Encounter();
        beingCreated.setVisit(visit);
        beingCreated.setLocation(new Location());
        beingCreated.addProvider(new EncounterRole(), new Provider());

        action.action(new EncounterDomainWrapper(beingCreated), new Obs(), new HashMap<String, String[]>());

        verify(adtService).dischargePatient(argThat(new ArgumentMatcher<Discharge>() {
            @Override
            public boolean matches(Object argument) {
                Discharge actual = (Discharge) argument;
                return actual.getLocation().equals(beingCreated.getLocation()) &&
                        actual.getVisit().equals(beingCreated.getVisit()) &&
                        TestUtils.sameProviders(actual.getProviders(), beingCreated.getProvidersByRoles());
            }
        }));
    }

}
