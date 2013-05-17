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

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.api.LocationService;
import org.openmrs.module.emrapi.TestUtils;
import org.openmrs.module.emrapi.adt.Admission;
import org.openmrs.module.emrapi.adt.AdtService;
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
public class AdmitToSpecificLocationDispositionActionTest extends AuthenticatedUserTestHelper {

    private AdmitToSpecificLocationDispositionAction action;
    private AdtService adtService;
    private LocationService locationService;


    @Before
    public void setUp() throws Exception {
        locationService = mock(LocationService.class);
        adtService = mock(AdtService.class);

        action = new AdmitToSpecificLocationDispositionAction();
        action.setLocationService(locationService);
        action.setAdtService(adtService);
    }

    @Test
    public void testAction() throws Exception {
        Map<String, String[]> request = new HashMap<String, String[]>();
        request.put(AdmitToSpecificLocationDispositionAction.ADMISSION_LOCATION_PARAMETER, new String[] { "7" });
        request.put("something", new String[] { "unrelated" });

        final Location toLocation = new Location();
        when(locationService.getLocation(7)).thenReturn(toLocation);

        final Patient patient = new Patient();
        final Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.addProvider(new EncounterRole(), new Provider());

        action.action(new EncounterDomainWrapper(encounter), new Obs(), request);
        verify(adtService).admitPatient(argThat(new ArgumentMatcher<Admission>() {
            @Override
            public boolean matches(Object argument) {
                Admission actual = (Admission) argument;
                return actual.getPatient().equals(patient) && actual.getLocation().equals(toLocation) && TestUtils.sameProviders(actual.getProviders(), encounter.getProvidersByRoles());
            }
        }));

    }

}
