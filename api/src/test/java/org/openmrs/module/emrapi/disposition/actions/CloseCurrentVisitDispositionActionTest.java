package org.openmrs.module.emrapi.disposition.actions;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.emrapi.test.AuthenticatedUserTestHelper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;

import java.util.Date;
import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CloseCurrentVisitDispositionActionTest extends AuthenticatedUserTestHelper {

    private CloseCurrentVisitDispositionAction action;
    private AdtService adtService;
    private VisitService visitService;
    private DispositionService dispositionService;
    private DispositionDescriptor dispositionDescriptor;
    private VisitDomainWrapper visitDomainWrapper;


    @Before
    public void setUp() throws Exception {

        adtService = mock(AdtService.class);
        dispositionService = mock(DispositionService.class);
        visitService = mock(VisitService.class);
        dispositionDescriptor = mock(DispositionDescriptor.class);
        visitDomainWrapper = mock(VisitDomainWrapper.class);

        when(dispositionService.getDispositionDescriptor()).thenReturn(dispositionDescriptor);
        when(adtService.wrap(any(Visit.class))).thenReturn(visitDomainWrapper);

        action = new CloseCurrentVisitDispositionAction();
        action.setAdtService(adtService);
        action.setVisitService(visitService);

    }

    @Test
    public void shouldCloseActiveVisit() throws Exception {

        final Visit visit = new Visit();
        final Encounter encounter = new Encounter();
        final Date encounterDate = (new DateTime(2013, 05, 13, 20, 26)).toDate();
        encounter.setVisit(visit);
        encounter.addProvider(new EncounterRole(), new Provider());
        encounter.setEncounterDatetime(encounterDate);
        visit.addEncounter(encounter);

        when(visitDomainWrapper.isActive()).thenReturn(true);
        when(visitDomainWrapper.getVisit()).thenReturn(visit);
        when(visitDomainWrapper.getMostRecentEncounter()).thenReturn(encounter);

        action.action(new EncounterDomainWrapper(encounter), new Obs(), new HashMap<String, String[]>());

        verify(visitDomainWrapper).closeOnLastEncounterDatetime();
        verify(visitService).saveVisit(visit);
    }

    @Test
    public void shouldCloseActiveVisitIfSubsequentEncountersButOnSameDay() throws Exception {

        final Visit visit = new Visit();

        final Encounter encounter = new Encounter();
        final Date encounterDate = (new DateTime(2013, 05, 13, 20, 26)).toDate();
        encounter.setVisit(visit);
        encounter.addProvider(new EncounterRole(), new Provider());
        encounter.setEncounterDatetime(encounterDate);
        visit.addEncounter(encounter);

        final Encounter subsequentEncounter = new Encounter();
        final Date subsequentEncounterDate = (new DateTime(2013, 05, 13, 23, 23)).toDate();
        subsequentEncounter.setVisit(visit);
        subsequentEncounter.addProvider(new EncounterRole(), new Provider());
        subsequentEncounter.setEncounterDatetime(subsequentEncounterDate);
        visit.addEncounter(subsequentEncounter);

        when(visitDomainWrapper.isActive()).thenReturn(true);
        when(visitDomainWrapper.getVisit()).thenReturn(visit);
        when(visitDomainWrapper.getMostRecentEncounter()).thenReturn(subsequentEncounter);

        action.action(new EncounterDomainWrapper(encounter), new Obs(), new HashMap<String, String[]>());

        verify(visitDomainWrapper).closeOnLastEncounterDatetime();
        verify(visitService).saveVisit(visit);
    }

    @Test
    public void shouldNotCloseActiveVisitIfSubsequentEncountersOnAnotherDay() throws Exception {

        final Visit visit = new Visit();

        final Encounter encounter = new Encounter();
        final Date encounterDate = (new DateTime(2013, 05, 13, 20, 26)).toDate();
        encounter.setVisit(visit);
        encounter.addProvider(new EncounterRole(), new Provider());
        encounter.setEncounterDatetime(encounterDate);
        visit.addEncounter(encounter);

        final Encounter subsequentEncounter = new Encounter();
        final Date subsequentEncounterDate = (new DateTime(2013, 05, 14, 05, 05)).toDate();
        subsequentEncounter.setVisit(visit);
        subsequentEncounter.addProvider(new EncounterRole(), new Provider());
        subsequentEncounter.setEncounterDatetime(subsequentEncounterDate);
        visit.addEncounter(subsequentEncounter);

        when(visitDomainWrapper.isActive()).thenReturn(true);
        when(visitDomainWrapper.getVisit()).thenReturn(visit);
        when(visitDomainWrapper.getMostRecentEncounter()).thenReturn(subsequentEncounter);

        action.action(new EncounterDomainWrapper(encounter), new Obs(), new HashMap<String, String[]>());

        verify(visitDomainWrapper, never()).closeOnLastEncounterDatetime();
        verify(visitService, never()).saveVisit(visit);
    }

    @Test
    public void shouldNotCloseVisitThatIsNotActive() throws Exception {

        final Visit visit = new Visit();
        final Encounter encounter = new Encounter();
        final Date encounterDate = (new DateTime(2013, 05, 13, 20, 26)).toDate();
        encounter.setVisit(visit);
        encounter.addProvider(new EncounterRole(), new Provider());
        encounter.setEncounterDatetime(encounterDate);
        visit.addEncounter(encounter);

        when(visitDomainWrapper.isActive()).thenReturn(false);
        when(visitDomainWrapper.getVisit()).thenReturn(visit);

        action.action(new EncounterDomainWrapper(encounter), new Obs(), new HashMap<String, String[]>());

        verify(visitDomainWrapper, never()).closeOnLastEncounterDatetime();
        verify(visitService, never()).saveVisit(visit);

    }

}
