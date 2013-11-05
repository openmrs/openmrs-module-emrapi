package org.openmrs.module.emrapi.disposition.actions;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.EmrApiProperties;
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
public class DischargeIfAdmittedDispositionActionTest extends AuthenticatedUserTestHelper {

    private DischargeIfAdmittedDispositionAction action;
    private AdtService adtService;
    private EmrApiProperties emrApiProperties;
    private DispositionDescriptor dispositionDescriptor;
    private DispositionService dispositionService;
    private VisitDomainWrapper visitDomainWrapper;
    private Concept dispositionObsGroupConcept = new Concept();;

    @Before
    public void setUp() throws Exception {
        adtService = mock(AdtService.class);

        emrApiProperties = mock(EmrApiProperties.class);
        dispositionService = mock(DispositionService.class);
        dispositionDescriptor = mock(DispositionDescriptor.class);
        visitDomainWrapper = mock(VisitDomainWrapper.class);

        when(dispositionService.getDispositionDescriptor()).thenReturn(dispositionDescriptor);
        when(adtService.wrap(any(Visit.class))).thenReturn(visitDomainWrapper);

        action = new DischargeIfAdmittedDispositionAction();
        action.setAdtService(adtService);
        action.setEmrApiProperties(emrApiProperties);
    }

    @Test
    public void testDischargesIfAdmitted() throws Exception {

        final Visit visit = new Visit();
        final Encounter encounter = new Encounter();
        final Date encounterDate = (new DateTime(2013, 05, 13, 20, 26)).toDate();
        encounter.setVisit(visit);
        encounter.addProvider(new EncounterRole(), new Provider());
        encounter.setEncounterDatetime(encounterDate);

        final Obs dispositionObsGroup = new Obs();
        dispositionObsGroup.setConcept(dispositionObsGroupConcept);

        // TODO note that we really want to only test if the patient is admitted at the encounter datetime, but we have to test against visitDomainWrapper.isAdmitted()
        // TODO for now because the "createAdtEncounterFor" method will throw an exception if isAdmitted() returns false; see https://minglehosting.thoughtworks.com/unicef/projects/pih_mirebalais/cards/938
        when(visitDomainWrapper.isAdmitted()).thenReturn(true);
        //when(visitDomainWrapper.isAdmitted(encounterDate)).thenReturn(true);

        action.action(new EncounterDomainWrapper(encounter), dispositionObsGroup, null);
        verify(adtService).createAdtEncounterFor(argThat(new ArgumentMatcher<AdtAction>() {
            @Override
            public boolean matches(Object argument) {
                AdtAction actual = (AdtAction) argument;
                return actual.getVisit().equals(visit) &&
                        TestUtils.sameProviders(actual.getProviders(), encounter.getProvidersByRoles()) &&
                        actual.getActionDatetime().equals(encounterDate) &&
                        actual.getType().equals(AdtAction.Type.DISCHARGE);
            }
        }));
    }

    @Test
    public void testDoesNotDischargesIfNotAdmitted() throws Exception {

        final Visit visit = new Visit();
        final Encounter encounter = new Encounter();
        final Date encounterDate = (new DateTime(2013, 05, 13, 20, 26)).toDate();
        encounter.setVisit(visit);
        encounter.addProvider(new EncounterRole(), new Provider());
        encounter.setEncounterDatetime(encounterDate);

        final Obs dispositionObsGroup = new Obs();
        dispositionObsGroup.setConcept(dispositionObsGroupConcept);

        when(visitDomainWrapper.isAdmitted(encounterDate)).thenReturn(false);

        action.action(new EncounterDomainWrapper(encounter), dispositionObsGroup, null);
        verify(adtService, never()).createAdtEncounterFor(any(AdtAction.class));
    }

}
