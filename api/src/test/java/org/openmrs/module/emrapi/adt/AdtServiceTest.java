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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.TestUtils;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.serialization.SerializationException;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.openmrs.module.emrapi.TestUtils.hasProviders;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class AdtServiceTest {

    private AdtServiceImpl service;

    VisitService mockVisitService;
    EncounterService mockEncounterService;
    ProviderService mockProviderService;
    PatientService mockPatientService;
    EmrApiProperties emrApiProperties;

    private Person personForCurrentUser;
    private Provider providerForCurrentUser;

    private EncounterRole checkInClerkEncounterRole;
    private EncounterType checkInEncounterType;
    private EncounterType admissionEncounterType;
    private EncounterType dischargeEncounterType;
    private EncounterType transferWithinHospitalEncounterType;
    private VisitType atFacilityVisitType;
    private LocationTag supportsVisits;
    private LocationTag supportsAdmissions;
    private Location mirebalaisHospital;
    private Location outpatientDepartment;
    private Location inpatientDepartment;
    private Location radiologyDepartment;
    private PersonAttributeType unknownPatientPersonAttributeType;
    private PatientIdentifierType paperRecordIdentifierType;

    @Before
    public void setup() {
        personForCurrentUser = new Person();
        personForCurrentUser.addName(new PersonName("Current", "User", "Person"));

        User authenticatedUser = new User();
        authenticatedUser.setPerson(personForCurrentUser);
        mockStatic(Context.class);
        when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);

        providerForCurrentUser = new Provider();
        providerForCurrentUser.setPerson(personForCurrentUser);
        mockProviderService = mock(ProviderService.class);
        when(mockProviderService.getProvidersByPerson(personForCurrentUser, false)).thenReturn(Collections.singletonList(providerForCurrentUser));

        mockVisitService = mock(VisitService.class);
        mockEncounterService = mock(EncounterService.class);
        mockPatientService = mock(PatientService.class);

        checkInClerkEncounterRole = new EncounterRole();
        checkInEncounterType = new EncounterType();
        admissionEncounterType = new EncounterType();
        dischargeEncounterType = new EncounterType();
        transferWithinHospitalEncounterType = new EncounterType();
        atFacilityVisitType = new VisitType();

        supportsVisits = new LocationTag();
        supportsVisits.setName(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS);

        supportsAdmissions = new LocationTag();
        supportsAdmissions.setName(EmrApiConstants.LOCATION_TAG_SUPPORTS_ADMISSION);

        outpatientDepartment = new Location();
        radiologyDepartment = new Location();
        inpatientDepartment = new Location();
        inpatientDepartment.addTag(supportsAdmissions);

        mirebalaisHospital = new Location();
        mirebalaisHospital.addTag(supportsVisits);
        mirebalaisHospital.addChildLocation(outpatientDepartment);
        mirebalaisHospital.addChildLocation(inpatientDepartment);

        unknownPatientPersonAttributeType = new PersonAttributeType();
        unknownPatientPersonAttributeType.setId(1);
        unknownPatientPersonAttributeType.setPersonAttributeTypeId(10);
        unknownPatientPersonAttributeType.setName(EmrApiConstants.UNKNOWN_PATIENT_PERSON_ATTRIBUTE_TYPE_NAME);
        unknownPatientPersonAttributeType.setFormat("java.lang.String");

        paperRecordIdentifierType = new PatientIdentifierType();

        emrApiProperties = mock(EmrApiProperties.class);
        when(emrApiProperties.getVisitExpireHours()).thenReturn(10);
        when(emrApiProperties.getCheckInEncounterType()).thenReturn(checkInEncounterType);
        when(emrApiProperties.getAdmissionEncounterType()).thenReturn(admissionEncounterType);
        when(emrApiProperties.getExitFromInpatientEncounterType()).thenReturn(dischargeEncounterType);
        when(emrApiProperties.getTransferWithinHospitalEncounterType()).thenReturn(transferWithinHospitalEncounterType);
        when(emrApiProperties.getAtFacilityVisitType()).thenReturn(atFacilityVisitType);
        when(emrApiProperties.getCheckInClerkEncounterRole()).thenReturn(checkInClerkEncounterRole);
        when(emrApiProperties.getUnknownPatientPersonAttributeType()).thenReturn(unknownPatientPersonAttributeType);

        AdtServiceImpl service = new AdtServiceImpl();
        service.setPatientService(mockPatientService);
        service.setVisitService(mockVisitService);
        service.setEncounterService(mockEncounterService);
        service.setProviderService(mockProviderService);
        service.setEmrApiProperties(emrApiProperties);
        this.service = service;
    }


    @Test
    public void testThatRecentVisitIsActive() throws Exception {
        Visit visit = new Visit();
        visit.setStartDatetime(new Date());

        Assert.assertThat(service.isActive(visit), is(true));
    }

    @Test
    public void testThatOldVisitIsNotActive() throws Exception {
        Visit visit = new Visit();
        visit.setStartDatetime(DateUtils.addDays(new Date(), -7));

        Assert.assertThat(service.isActive(visit), is(false));
    }

    @Test
    public void testThatOldVisitWithRecentEncounterIsActive() throws Exception {
        Encounter encounter = new Encounter();
        encounter.setEncounterType(checkInEncounterType);
        encounter.setEncounterDatetime(new Date());

        Visit visit = new Visit();
        visit.setStartDatetime(DateUtils.addDays(new Date(), -7));
        visit.addEncounter(encounter);

        Assert.assertThat(service.isActive(visit), is(true));
    }

    @Test
    public void testEnsureActiveVisitCreatesNewVisit() throws Exception {
        final Patient patient = new Patient();

        when(mockVisitService.getVisitsByPatient(patient)).thenReturn(new ArrayList<Visit>());

        service.ensureActiveVisit(patient, outpatientDepartment);

        verify(mockVisitService).saveVisit(argThat(new ArgumentMatcher<Visit>() {
            @Override
            public boolean matches(Object o) {
                Visit actual = (Visit) o;
                assertThat(actual.getVisitType(), is(atFacilityVisitType));
                assertThat(actual.getPatient(), is(patient));
                assertThat(actual.getLocation(), is(mirebalaisHospital));
                assertThat(actual.getStartDatetime(), TestUtils.isJustNow());
                return true;
            }
        }));
    }

    @Test
    public void testEnsureActiveVisitFindsRecentVisit() throws Exception {
        final Patient patient = new Patient();

        Visit recentVisit = new Visit();
        recentVisit.setLocation(mirebalaisHospital);
        recentVisit.setStartDatetime(DateUtils.addHours(new Date(), -1));

        when(mockVisitService.getVisitsByPatient(patient)).thenReturn(Collections.singletonList(recentVisit));

        assertThat(service.ensureActiveVisit(patient, outpatientDepartment), is(recentVisit));

        verify(mockVisitService, times(0)).saveVisit(any(Visit.class));
    }

    @Test
    public void testEnsureActiveVisitDoesNotFindOldVisit() throws Exception {
        final Patient patient = new Patient();

        final Visit oldVisit = new Visit();
        oldVisit.setLocation(mirebalaisHospital);
        oldVisit.setStartDatetime(DateUtils.addDays(new Date(), -7));

        when(mockVisitService.getVisitsByPatient(patient)).thenReturn(Collections.singletonList(oldVisit));

        final Visit created = service.ensureActiveVisit(patient, outpatientDepartment);
        assertNotNull(created);
        assertNotSame(oldVisit, created);

        // should be called once to save oldVisit (having stopped it)
        verify(mockVisitService).saveVisit(argThat(new ArgumentMatcher<Visit>() {
            @Override
            public boolean matches(Object o) {
                Visit actual = (Visit) o;
                if (actual == oldVisit) {
                    // no encounters, so closed at the moment it started
                    assertThat(actual.getStopDatetime(), is(oldVisit.getStartDatetime()));
                    return true;
                } else {
                    return false;
                }
            }
        }));

        // should be called once to create a new visit
        verify(mockVisitService).saveVisit(argThat(new ArgumentMatcher<Visit>() {
            @Override
            public boolean matches(Object o) {
                Visit actual = (Visit) o;
                if (actual != oldVisit) {
                    assertSame(created, actual);
                    assertThat(actual.getVisitType(), is(atFacilityVisitType));
                    assertThat(actual.getPatient(), is(patient));
                    assertThat(actual.getLocation(), is(mirebalaisHospital));
                    assertThat(actual.getStartDatetime(), TestUtils.isJustNow());
                    return true;
                } else {
                    return false;
                }
            }
        }));
    }

    @Test
    public void test_checkInPatient_forNewVisit() throws Exception {
        final Patient patient = new Patient();

        when(mockVisitService.getVisitsByPatient(patient)).thenReturn(new ArrayList<Visit>());

        service.checkInPatient(patient, outpatientDepartment, null, null, null, false);

        verify(mockVisitService).saveVisit(argThat(new ArgumentMatcher<Visit>() {
            @Override
            public boolean matches(Object o) {
                Visit actual = (Visit) o;
                assertThat(actual.getVisitType(), is(atFacilityVisitType));
                assertThat(actual.getPatient(), is(patient));
                assertThat(actual.getLocation(), is(mirebalaisHospital));
                assertThat(actual.getStartDatetime(), TestUtils.isJustNow());
                return true;
            }
        }));

        verify(mockEncounterService).saveEncounter(argThat(new ArgumentMatcher<Encounter>() {
            @Override
            public boolean matches(Object o) {
                Encounter actual = (Encounter) o;
                assertThat(actual.getEncounterType(), is(checkInEncounterType));
                assertThat(actual.getPatient(), is(patient));
                assertThat(actual.getLocation(), is(outpatientDepartment));
                assertThat(actual.getEncounterDatetime(), TestUtils.isJustNow());
                assertThat(actual.getProvidersByRoles().size(), is(1));
                assertThat(actual.getProvidersByRole(checkInClerkEncounterRole).iterator().next(), is(providerForCurrentUser));
                return true;
            }
        }));
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void shouldGetAllVisitSummariesOfAllActiveVisit() throws Exception {
        final Visit visit1 = new Visit();
        visit1.setStartDatetime(DateUtils.addHours(new Date(), -2));
        visit1.setLocation(mirebalaisHospital);

        final Visit visit2 = new Visit();
        visit2.setStartDatetime(DateUtils.addHours(new Date(), -1));
        visit2.setLocation(outpatientDepartment);

        Visit visit3 = new Visit();
        visit3.setStartDatetime(DateUtils.addDays(new Date(), -10));
        visit3.setStopDatetime(DateUtils.addDays(new Date(), -8));
        visit3.setLocation(mirebalaisHospital);

        Set<Location> expectedLocations = new HashSet<Location>();
        expectedLocations.add(mirebalaisHospital);
        expectedLocations.add(outpatientDepartment);
        expectedLocations.add(inpatientDepartment);

        when(
                mockVisitService.getVisits(any(Collection.class), any(Collection.class), eq(expectedLocations),
                        any(Collection.class), any(Date.class), any(Date.class), any(Date.class), any(Date.class), any(Map.class),
                        any(Boolean.class), any(Boolean.class))).thenReturn(Arrays.asList(visit1, visit2, visit3));

        List<VisitDomainWrapper> activeVisitSummaries = service.getActiveVisits(mirebalaisHospital);

        assertThat(activeVisitSummaries, TestUtils.isCollectionOfExactlyElementsWithProperties("visit", visit1, visit2));
    }

    @Test
    public void shouldCloseInactiveVisitWithLastEncounterDate() {
        Visit visit = new Visit();
        visit.setStartDatetime(DateUtils.addHours(new Date(), -14));

        Encounter encounter1 = new Encounter();
        encounter1.setEncounterType(checkInEncounterType);
        encounter1.setEncounterDatetime(DateUtils.addHours(new Date(), -14));
        visit.addEncounter(encounter1);

        Date stopDatetime = DateUtils.addHours(new Date(), -14);
        Encounter encounter2 = new Encounter();
        encounter2.setEncounterType(checkInEncounterType);
        encounter2.setEncounterDatetime(stopDatetime);
        visit.addEncounter(encounter2);

        when(mockVisitService.getVisitsByPatient(null)).thenReturn(Collections.singletonList(visit));

        service.getActiveVisit(null, null);

        assertThat(visit.getStopDatetime(), is(stopDatetime));
    }

    @Test
    public void shouldNotCloseVisitWithAnAdmissionEncounter() {
        Visit visit = new Visit();
        visit.setStartDatetime(DateUtils.addHours(new Date(), -14));

        Encounter encounter1 = new Encounter();
        encounter1.setEncounterType(admissionEncounterType);
        encounter1.setEncounterDatetime(DateUtils.addHours(new Date(), -14));
        visit.addEncounter(encounter1);

        when(mockVisitService.getVisits(null, null, null, null, null, null, null, null, null, false, false))
                .thenReturn(Collections.singletonList(visit));

        service.closeInactiveVisits();

        assertNull(visit.getStopDatetime());
    }

    @Test
    public void shouldCloseVisitWithAdmissionAndDischargeEncounters() {
        Visit visit = new Visit();
        visit.setStartDatetime(DateUtils.addHours(new Date(), -14));

        Encounter encounter1 = new Encounter();
        encounter1.setEncounterType(admissionEncounterType);
        encounter1.setEncounterDatetime(DateUtils.addHours(new Date(), -14));

        Encounter encounter2 = new Encounter();
        encounter2.setEncounterType(dischargeEncounterType);
        encounter2.setEncounterDatetime(DateUtils.addHours(new Date(), -13));

        // the underlying API returns these sorted in reverse chronological order (using hibernate), so we emulate that
        visit.setEncounters(new LinkedHashSet<Encounter>());
        visit.addEncounter(encounter2);
        visit.addEncounter(encounter1);

        when(mockVisitService.getVisits(null, null, null, null, null, null, null, null, null, false, false))
                .thenReturn(Collections.singletonList(visit));

        service.closeInactiveVisits();

        assertThat(visit.getStopDatetime(), is(encounter2.getEncounterDatetime()));
    }

    @Test
    public void shouldCloseInactiveVisitWithStartDateIfNoEncounters() {
        Visit visit = new Visit();
        Date startDatetime = DateUtils.addHours(new Date(), -14);
        visit.setStartDatetime(startDatetime);

        when(mockVisitService.getVisitsByPatient(null)).thenReturn(Collections.singletonList(visit));

        service.getActiveVisit(null, null);

        assertThat(visit.getStopDatetime(), is(startDatetime));
    }

    @Test
    public void shouldCloseAnyInactiveButOpenVisits() {
        Visit old1 = new Visit();
        old1.setStartDatetime(DateUtils.addDays(new Date(), -2));

        Encounter oldEncounter = new Encounter();
        oldEncounter.setEncounterType(checkInEncounterType);
        oldEncounter.setEncounterDatetime(DateUtils.addHours(DateUtils.addDays(new Date(), -2), 6));

        Visit old2 = new Visit();
        old2.setStartDatetime(DateUtils.addDays(new Date(), -2));
        old2.addEncounter(oldEncounter);

        Visit new1 = new Visit();
        new1.setStartDatetime(DateUtils.addHours(new Date(), -2));

        when(mockVisitService.getVisits(anyCollection(), anyCollection(), anyCollection(), anyCollection(), any(Date.class), any(Date.class), any(Date.class), any(Date.class), anyMap(), anyBoolean(), anyBoolean())).thenReturn(Arrays.asList(old1, old2, new1));

        service.closeInactiveVisits();

        verify(mockVisitService).saveVisit(old1);
        verify(mockVisitService).saveVisit(old2);
        verify(mockVisitService, never()).saveVisit(new1);
        assertNull(new1.getStopDatetime());
        assertNotNull(old1.getStopDatetime());
        assertNotNull(old2.getStopDatetime());
    }

    @Test
    public void testOverlappingVisits() throws Exception {
        Patient patient = new Patient();
        VisitType visitType = new VisitType();

        Date now = new Date();
        Date tenDaysAgo = DateUtils.addDays(now, -10);
        Date nineDaysAgo = DateUtils.addDays(now, -9);
        Date eightDaysAgo = DateUtils.addDays(now, -8);
        Date sevenDaysAgo = DateUtils.addDays(now, -7);

        Visit visit1 = buildVisit(patient, visitType, mirebalaisHospital, tenDaysAgo, eightDaysAgo);
        Visit visit2 = buildVisit(patient, visitType, mirebalaisHospital, now, null);
        Visit visit3 = buildVisit(patient, visitType, null, tenDaysAgo, nineDaysAgo);
        Visit visit4 = buildVisit(patient, visitType, mirebalaisHospital, nineDaysAgo, sevenDaysAgo);

        assertThat(service.visitsOverlap(visit1, visit2), is(false));
        assertThat(service.visitsOverlap(visit1, visit3), is(false));
        assertThat(service.visitsOverlap(visit1, visit4), is(true));
    }

    @Test
    public void testMergePatientsJoinsOverlappingVisits() throws Exception {
        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Date now = new Date();
        Date tenDaysAgo = DateUtils.addDays(now, -10);
        Date nineDaysAgo = DateUtils.addDays(now, -9);
        Date eightDaysAgo = DateUtils.addDays(now, -8);
        Date sevenDaysAgo = DateUtils.addDays(now, -7);

        Visit first = buildVisit(notPreferred, null, mirebalaisHospital, tenDaysAgo, eightDaysAgo);
        Visit last = buildVisit(notPreferred, null, mirebalaisHospital, sevenDaysAgo, null);
        Visit middle = buildVisit(preferred, null, mirebalaisHospital, nineDaysAgo, now);
        Visit unrelated = buildVisit(preferred, null, null, tenDaysAgo, eightDaysAgo);
        Visit voided = buildVisit(notPreferred, null, mirebalaisHospital, tenDaysAgo, sevenDaysAgo);
        voided.setVoided(true);

        first.addEncounter(buildEncounter(notPreferred, tenDaysAgo));
        middle.addEncounter(buildEncounter(preferred, nineDaysAgo));

        when(mockVisitService.getVisitsByPatient(preferred, true, false)).thenReturn(Arrays.asList(middle, unrelated));
        when(mockVisitService.getVisitsByPatient(notPreferred, true, false)).thenReturn(Arrays.asList(first, voided, last));

        service.mergePatients(preferred, notPreferred);

        verify(mockVisitService).voidVisit(eq(first), anyString());
        verify(mockVisitService).voidVisit(eq(last), anyString());

        assertThat(middle.getStartDatetime(), is(tenDaysAgo));
        assertNull(middle.getStopDatetime());
        assertThat(middle.getEncounters().size(), is(2));
        for (Encounter e : middle.getEncounters()) {
            assertThat(e.getVisit(), is(middle));
            assertThat(e.getPatient(), is(middle.getPatient()));
        }

        verify(mockVisitService, times(2)).saveVisit(middle); // two visits merged in

        verify(mockVisitService, never()).saveVisit(unrelated);
        verify(mockVisitService, never()).saveVisit(voided);
        verify(mockVisitService, never()).voidVisit(eq(unrelated), anyString());
        verify(mockVisitService, never()).voidVisit(eq(voided), anyString());

        verify(mockPatientService).mergePatients(preferred, notPreferred);
    }

    @Test
    public void itShouldNotCopyUnknownAttributeWhenMergingAnUnknownPatientIntoAPermanentOne() throws SerializationException {
        Patient preferred = createPatientWithIdAs(10);

        Patient unknownPatient = createPatientWithIdAs(11);

        unknownPatient.addAttribute(new PersonAttribute(unknownPatientPersonAttributeType, "true"));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Patient preferred = (Patient) invocationOnMock.getArguments()[0];
                Patient unknownPatient = (Patient) invocationOnMock.getArguments()[1];

                PersonAttribute attribute = unknownPatient.getAttribute(unknownPatientPersonAttributeType);
                preferred.addAttribute(attribute);

                return preferred;
            }
        }).when(mockPatientService).mergePatients(preferred, unknownPatient);

        service.mergePatients(preferred, unknownPatient);

        assertNull(preferred.getAttribute(unknownPatientPersonAttributeType));

        verify(mockPatientService).savePatient(preferred);

    }

    private Patient createPatientWithIdAs(int id) {
        Patient preferred = new Patient();
        preferred.setId(id);
        preferred.setPatientId(id);
        return preferred;
    }

    @Test
    public void testMergePatientsDoesNotResultInOverlappingVisits() throws Exception {
        Patient preferred = new Patient();
        Patient notPreferred = new Patient();

        Date now = new Date();
        Date twelveDaysAgo = DateUtils.addDays(now, -12);
        Date elevenDaysAgo = DateUtils.addDays(now, -11);
        Date tenDaysAgo = DateUtils.addDays(now, -10);
        Date nineDaysAgo = DateUtils.addDays(now, -9);
        Date eightDaysAgo = DateUtils.addDays(now, -8);
        Date sevenDaysAgo = DateUtils.addDays(now, -7);

        //           ___nonPreferredVisit______________
        //           |                                |
        //  |                      |       |                      |
        //  |_firstPreferredVisit__|.......|_secondPreferredVisit_|
        //
        // 12       11            10       9          8           7

        Visit nonPreferredVisit = buildVisit(notPreferred, null, mirebalaisHospital, elevenDaysAgo, eightDaysAgo);
        nonPreferredVisit.addEncounter(buildEncounter(notPreferred, tenDaysAgo));

        Visit firstPreferredVisit = buildVisit(preferred, null, mirebalaisHospital, twelveDaysAgo, tenDaysAgo);
        firstPreferredVisit.addEncounter(buildEncounter(notPreferred, elevenDaysAgo));

        Visit secondPreferredVisit = buildVisit(preferred, null, mirebalaisHospital, nineDaysAgo, sevenDaysAgo);
        secondPreferredVisit.addEncounter(buildEncounter(notPreferred, eightDaysAgo));

        when(mockVisitService.getVisitsByPatient(notPreferred, true, false)).thenReturn(Arrays.asList(nonPreferredVisit));
        when(mockVisitService.getVisitsByPatient(preferred, true, false)).thenReturn(Arrays.asList(firstPreferredVisit, secondPreferredVisit));

        service.mergePatients(preferred, notPreferred);

        assertThat(firstPreferredVisit.getStartDatetime(), is(twelveDaysAgo));
        assertThat(firstPreferredVisit.getStopDatetime(), is(sevenDaysAgo));

        verify(mockVisitService).voidVisit(eq(nonPreferredVisit), anyString());
        verify(mockVisitService).voidVisit(eq(secondPreferredVisit), anyString());
        verify(mockVisitService, times(2)).saveVisit(firstPreferredVisit); // two visits merged in

        verify(mockPatientService).mergePatients(preferred, notPreferred);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldNotAllowMergingAnUnknownRecordIntoAPermanentOne() {
        Patient preferred = new Patient();
        Patient notPreferred = new Patient();
        preferred.addAttribute(new PersonAttribute(emrApiProperties.getUnknownPatientPersonAttributeType(), "true"));

        service.mergePatients(preferred, notPreferred);
    }

    @Test
    public void testThatMergingTwoUnknownRecordsResultsInAnUnknownRecord() {
        Patient preferred = new Patient();
        Patient notPreferred = new Patient();
        PersonAttribute preferredIsUnknownAttribute = new PersonAttribute(unknownPatientPersonAttributeType, "true");
        preferred.addAttribute(preferredIsUnknownAttribute);
        notPreferred.addAttribute(new PersonAttribute(emrApiProperties.getUnknownPatientPersonAttributeType(), "true"));

        service.mergePatients(preferred, notPreferred);

        assertThat(preferred.getAttribute(unknownPatientPersonAttributeType), is(preferredIsUnknownAttribute));
    }

    @Test
    public void test_admitPatient_ensuresActiveVisit() throws Exception {
        final Patient patient = new Patient();
        Admission admission = new Admission();
        admission.setPatient(patient);
        admission.setLocation(inpatientDepartment);
        admission.setProviders(buildProviderMap());

        service.admitPatient(admission);

        verify(mockVisitService).saveVisit(argThat(new ArgumentMatcher<Visit>() {
            @Override
            public boolean matches(Object o) {
                Visit actual = (Visit) o;
                assertThat(actual.getPatient(), is(patient));
                assertThat(actual.getLocation(), is(mirebalaisHospital));
                assertThat(actual.getStartDatetime(), TestUtils.isJustNow());
                return true;
            }
        }));
    }

    private Map<EncounterRole, Set<Provider>> buildProviderMap() {
        HashMap<EncounterRole, Set<Provider>> map = new HashMap<EncounterRole, Set<Provider>>();
        map.put(new EncounterRole(), Collections.singleton(new Provider()));
        return map;
    }

    @Test(expected = IllegalStateException.class)
    public void test_admitPatient_failsIfPatientIsAlreadyAdmitted() throws Exception {
        Patient patient = new Patient();

        Encounter admit = buildEncounter(patient, new Date());
        admit.setEncounterType(admissionEncounterType);
        Visit existing = buildVisit(patient, atFacilityVisitType, mirebalaisHospital, new Date(), null);
        existing.addEncounter(admit);

        when(mockVisitService.getVisitsByPatient(patient)).thenReturn(Arrays.asList(existing));

        Admission admission = new Admission();
        admission.setPatient(patient);
        admission.setLocation(inpatientDepartment);
        admission.setProviders(buildProviderMap());

        service.admitPatient(admission);
    }

    @Test
    public void test_admitPatient_createsEncounter() throws Exception {
        final Patient patient = new Patient();
        final Admission admission = new Admission();
        admission.setPatient(patient);
        admission.setLocation(inpatientDepartment);
        admission.setProviders(buildProviderMap());

        service.admitPatient(admission);

        verify(mockEncounterService).saveEncounter(argThat(new ArgumentMatcher<Encounter>() {
            @Override
            public boolean matches(Object o) {
                Encounter actual = (Encounter) o;
                assertThat(actual.getEncounterType(), is(admissionEncounterType));
                assertNotNull(actual.getVisit());
                assertThat(actual.getPatient(), is(patient));
                assertThat(actual.getLocation(), is(inpatientDepartment));
                assertThat(actual.getEncounterDatetime(), TestUtils.isJustNow());
                assertThat(actual, hasProviders(admission.getProviders()));
                return true;
            }
        }));
    }

    @Test(expected = IllegalStateException.class)
    public void test_dischargePatient_failsIfPatientIsNotAdmitted() throws Exception {
        Patient patient = new Patient();

        Visit existing = buildVisit(patient, atFacilityVisitType, mirebalaisHospital, new Date(), null);
        when(mockVisitService.getVisitsByPatient(patient)).thenReturn(Arrays.asList(existing));

        Discharge discharge = new Discharge();
        discharge.setVisit(existing);
        discharge.setLocation(inpatientDepartment);
        discharge.setProviders(buildProviderMap());

        service.dischargePatient(discharge);
    }

    @Test
    public void test_dischargePatient_createsEncounter() throws Exception {
        final Patient patient = new Patient();

        Encounter admit = buildEncounter(patient, new Date());
        admit.setEncounterType(admissionEncounterType);
        Visit existing = buildVisit(patient, atFacilityVisitType, mirebalaisHospital, new Date(), null);
        existing.addEncounter(admit);

        when(mockVisitService.getVisitsByPatient(patient)).thenReturn(Arrays.asList(existing));

        final Discharge discharge = new Discharge();
        discharge.setVisit(existing);
        discharge.setLocation(inpatientDepartment);
        discharge.setProviders(buildProviderMap());

        service.dischargePatient(discharge);

        verify(mockEncounterService).saveEncounter(argThat(new ArgumentMatcher<Encounter>() {
            @Override
            public boolean matches(Object o) {
                Encounter actual = (Encounter) o;
                assertThat(actual.getEncounterType(), is(dischargeEncounterType));
                assertNotNull(actual.getVisit());
                assertThat(actual.getPatient(), is(patient));
                assertThat(actual.getLocation(), is(inpatientDepartment));
                assertThat(actual.getEncounterDatetime(), TestUtils.isJustNow());
                assertThat(actual, hasProviders(discharge.getProviders()));
                return true;
            }
        }));
    }

    @Test
    public void test_transferPatient_createsAnEncounter() throws Exception {
        final Patient patient = new Patient();

        final Visit visit = buildVisit(patient, atFacilityVisitType, mirebalaisHospital, new Date(), null);
        when(mockVisitService.getVisitsByPatient(patient)).thenReturn(Arrays.asList(visit));

        final Transfer transfer = new Transfer();
        transfer.setVisit(visit);
        transfer.setToLocation(radiologyDepartment);
        transfer.setProviders(buildProviderMap());
        service.transferPatient(transfer);

        verify(mockEncounterService).saveEncounter(argThat(new ArgumentMatcher<Encounter>() {
            @Override
            public boolean matches(Object o) {
                Encounter actual = (Encounter) o;
                assertThat(actual.getEncounterType(), is(transferWithinHospitalEncounterType));
                assertThat(actual.getVisit(), is(visit));
                assertThat(actual.getPatient(), is(patient));
                assertThat(actual.getLocation(), is(radiologyDepartment));
                assertThat(actual.getEncounterDatetime(), TestUtils.isJustNow());
                assertThat(actual, hasProviders(transfer.getProviders()));
                return true;
            }
        }));
    }

    private Encounter buildEncounter(Patient patient, Date encounterDatetime) {
        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setEncounterDatetime(encounterDatetime);
        return encounter;
    }

    private Visit buildVisit(Patient patient, VisitType visitType, Location location, Date startDate, Date endDate) {
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setVisitType(visitType);
        visit.setLocation(location);
        visit.setStartDatetime(startDate);
        visit.setStopDatetime(endDate);
        return visit;
    }


}
