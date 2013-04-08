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

package org.openmrs.module.emrapi.adt;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class EmrApiVisitAssignmentHandlerTest {

    EmrApiVisitAssignmentHandler handler;

    VisitService visitService;

    AdtService adtService;

    @Before
    public void before() {
        handler = new EmrApiVisitAssignmentHandler();
        visitService = mock(VisitService.class);
        adtService = new AdtServiceImpl();
        handler.setVisitService(visitService);
        handler.setAdtService(adtService);
    }

    @Ignore("TEMP HACK: disable this while we decide whether or not we want this functionality")
    @Test(expected = IllegalStateException.class)
    public void testThrowsExceptionIfNoSuitableVisitExists() throws Exception {
        Patient patient = new Patient();
        Location location = new Location();

        Visit notSuitable = new Visit();
        notSuitable.setPatient(patient);
        notSuitable.setStartDatetime(DateUtils.addDays(new Date(), -7));
        notSuitable.setStopDatetime(DateUtils.addDays(new Date(), -6));
        notSuitable.setLocation(location);

        when(
                visitService.getVisits(any(Collection.class), any(Collection.class), any(Collection.class),
                        any(Collection.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class), any(Map.class),
                        anyBoolean(), anyBoolean())).thenReturn(Collections.singletonList(notSuitable));

        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setLocation(location);

        handler.beforeCreateEncounter(encounter);
    }

    @Test
    public void testAssigningASuitableVisitWhenOneExists() throws Exception {
        Patient patient = new Patient();
        Location location = new Location();

        Visit notSuitable = new Visit();
        notSuitable.setPatient(patient);
        notSuitable.setStartDatetime(DateUtils.addDays(new Date(), -7));
        notSuitable.setStopDatetime(DateUtils.addDays(new Date(), -6));
        notSuitable.setLocation(location);

        Visit suitable = new Visit();
        suitable.setPatient(patient);
        suitable.setStartDatetime(DateUtils.addDays(new Date(), -1));
        suitable.setLocation(location);

        when(
                visitService.getVisits(any(Collection.class), any(Collection.class), any(Collection.class),
                        any(Collection.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class), any(Map.class),
                        anyBoolean(), anyBoolean())).thenReturn(Arrays.asList(notSuitable, suitable));

        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setLocation(location);

        handler.beforeCreateEncounter(encounter);

        Assert.assertThat(encounter.getVisit(), is(suitable));
        Assert.assertThat(suitable.getEncounters(), contains(encounter));
    }

}
