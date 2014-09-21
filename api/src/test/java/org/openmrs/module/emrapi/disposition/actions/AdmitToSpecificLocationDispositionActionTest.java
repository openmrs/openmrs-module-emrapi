/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

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
import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 */
public class AdmitToSpecificLocationDispositionActionTest extends AuthenticatedUserTestHelper {

    private AdmitToSpecificLocationDispositionAction action;
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
        visitDomainWrapper = mock(VisitDomainWrapper.class);;

        when(dispositionService.getDispositionDescriptor()).thenReturn(dispositionDescriptor);
        when(adtService.wrap(any(Visit.class))).thenReturn(visitDomainWrapper);

        action = new AdmitToSpecificLocationDispositionAction();
        action.setLocationService(locationService);
        action.setAdtService(adtService);
        action.setDispositionService(dispositionService);
    }

    @Test
    public void testAction() throws Exception {

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
        when(dispositionDescriptor.getAdmissionLocation(dispositionObsGroup, locationService)).thenReturn(toLocation);

        action.action(new EncounterDomainWrapper(encounter), dispositionObsGroup, null);

        verify(adtService).createAdtEncounterFor(argThat(new ArgumentMatcher<AdtAction>() {
            @Override
            public boolean matches(Object argument) {
                AdtAction actual = (AdtAction) argument;
                return actual.getVisit().equals(visit) &&
                        actual.getLocation().equals(toLocation) &&
                        TestUtils.sameProviders(actual.getProviders(), encounter.getProvidersByRoles()) &&
                        actual.getActionDatetime().equals(encounterDate) &&
                        actual.getType().equals(AdtAction.Type.ADMISSION);
            }
        }));

    }

    @Test
    public void testActionWhenAlreadyAdmitted() throws Exception {

        final Visit visit = new Visit();
        final Encounter encounter = new Encounter();
        final Date encounterDate = (new DateTime(2013, 05, 13, 20, 26)).toDate();
        encounter.setVisit(visit);
        encounter.addProvider(new EncounterRole(), new Provider());
        encounter.setEncounterDatetime(encounterDate);

        when(visitDomainWrapper.isAdmitted(encounterDate)).thenReturn(false);

        action.action(new EncounterDomainWrapper(encounter), new Obs(), new HashMap<String, String[]>());

        verify(adtService, never()).createAdtEncounterFor(any(AdtAction.class));
    }

}
