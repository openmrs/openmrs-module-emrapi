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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.EmrApiConstants;

public class EmrApiVisitAssignmentHandlerTest {

    EmrApiVisitAssignmentHandler handler;

    VisitService visitService;

    AdtService adtService;
    
    AdministrationService adminService;
    
    EncounterTypetoVisitTypeMapper encounterTypetoVisitTypeMapper;
    
    EncounterType encounterType;

    @Before
    public void before() {
        handler = new EmrApiVisitAssignmentHandler();
        visitService = mock(VisitService.class);
        adminService = mock(AdministrationService.class);
        adtService = new AdtServiceImpl();
        
        handler.setVisitService(visitService);
        handler.setAdtService(adtService);
        handler.setAdministrationService(adminService);
        
        encounterTypetoVisitTypeMapper = new EncounterTypetoVisitTypeMapper();
        encounterTypetoVisitTypeMapper.setVisitService(visitService);
        encounterTypetoVisitTypeMapper.setAdminService(adminService);
    
        handler.setEncounterTypetoVisitTypeMapper(encounterTypetoVisitTypeMapper);
    
        encounterType = new EncounterType();
        encounterType.setId(1);
        encounterType.setUuid("61ae96f4-6afe-4351-b6f8-cd4fc383cce1");
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
    
    @Test
    public void testAssigningANewVisitWhenOneDoesNotExistWithSpecifiedGlobalPropertyForCurrentVisitStartingToday() {
        Patient patient = new Patient();
        Location location = new Location();
        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setLocation(location);
        location.addTag(new LocationTag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS, "Tag that supports visits"));
        encounter.setEncounterDatetime(new Date());
        encounter.setEncounterType(encounterType);
    
        VisitType visitType = new VisitType();
        visitType.setId(1);
        
        when(adminService.getGlobalProperty(EmrApiConstants.GP_VISIT_ASSIGNMENT_HANDLER_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAP)).thenReturn("default:1");
        when(visitService.getVisitType(1)).thenReturn(visitType);
    
        // do I need to do this when setting the mock
        encounterTypetoVisitTypeMapper.setAdminService(adminService);
        encounterTypetoVisitTypeMapper.setVisitService(visitService);
        handler.setEncounterTypetoVisitTypeMapper(encounterTypetoVisitTypeMapper);
        
        handler.beforeCreateEncounter(encounter);
        
        // there is a visit on the encounter
        Assert.assertNotNull(encounter.getVisit());
        Assert.assertTrue(DateUtils.isSameDay(encounter.getVisit().getStartDatetime(), encounter.getEncounterDatetime())); // no check for end date for a visit that is started today since it is still open
    }
    
    @Test
    public void testAssigningANewVisitWhenOneDoesNotExistWithSpecifiedGlobalPropertyForPastVisit() {
        Calendar cal = new GregorianCalendar();
        cal.set(2017, 1, 5); // January 5, 2017 a date in the past
        Patient patient = new Patient();
        Location location = new Location();
        location.addTag(new LocationTag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS, "Tag that supports visits"));
        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setLocation(location);
        encounter.setEncounterDatetime(cal.getTime());
        encounter.setEncounterType(encounterType);
        
        VisitType visitType = new VisitType();
        visitType.setId(1);
        
        when(adminService.getGlobalProperty(EmrApiConstants.GP_VISIT_ASSIGNMENT_HANDLER_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAP)).thenReturn("default:1");
        when(visitService.getVisitType(1)).thenReturn(visitType);
        
        // do I need to do this when setting the mock
        encounterTypetoVisitTypeMapper.setAdminService(adminService);
        encounterTypetoVisitTypeMapper.setVisitService(visitService);
        handler.setAdministrationService(adminService);
        handler.setVisitService(visitService);
        handler.setEncounterTypetoVisitTypeMapper(encounterTypetoVisitTypeMapper);
        
        handler.beforeCreateEncounter(encounter);
        
        // there is a visit on the encounter
        Assert.assertNotNull(encounter.getVisit());
        Assert.assertTrue(DateUtils.isSameDay(encounter.getVisit().getStartDatetime(), encounter.getEncounterDatetime()));
        Assert.assertTrue(DateUtils.isSameDay(encounter.getVisit().getStopDatetime(), encounter.getEncounterDatetime())); // has stop time since it is in the past
    }
    
    @Test
    public void testNotCreatingAVisitWithNoGlobalProperty() {
        Patient patient = new Patient();
        Location location = new Location();
        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setLocation(location);
        encounter.setEncounterDatetime(new Date());
        encounter.setEncounterType(encounterType);
        
        // set the mapping global property
        when(adminService.getGlobalProperty(EmrApiConstants.GP_VISIT_ASSIGNMENT_HANDLER_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAP)).thenReturn("");
        
        // do I need to do this when setting the mock
        encounterTypetoVisitTypeMapper.setAdminService(adminService);
        handler.setAdministrationService(adminService);
        handler.setEncounterTypetoVisitTypeMapper(encounterTypetoVisitTypeMapper);
        
        handler.beforeCreateEncounter(encounter);
        
        // there is a visit on the encounter
        Assert.assertNull(encounter.getVisit());
    }
}
