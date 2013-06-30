package org.openmrs.module.emrapi.encounter;


import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.adt.exception.EncounterDateAfterVisitStopDateException;
import org.openmrs.module.emrapi.adt.exception.EncounterDateBeforeVisitStartDateException;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openmrs.module.emrapi.TestUtils.isJustNow;

public class EncounterDomainWrapperTest {

    private EncounterDomainWrapper encounterDomainWrapper;
    private Encounter encounter;

    @Before
    public void setUp(){
        encounter = mock(Encounter.class);
        encounterDomainWrapper = new EncounterDomainWrapper(encounter);
    }


    @Test
    public void shouldReturnTrueWhenTheCurrentUserCreatedTheEncounter(){
        User currentUser = createUserWithUuid("585812f0-a860-11e2-9e96-0800200c9a66");

        User encounterCreator = createUserWithUuid("585812f0-a860-11e2-9e96-0800200c9a66");

        when(encounter.getCreator()).thenReturn(encounterCreator);

        assertTrue(encounterDomainWrapper.participatedInEncounter(currentUser));
    }


    @Test
    public void shouldReturnTrueWhenTheCurrentUserIsOneOfTheProviders(){
        User currentUser = createUserWithUuid("585812f0-a860-11e2-9e96-0800200c9a66");

        Person providerPerson = createPersonWithUuid("585812f0-a860-11e2-9e96-0800200c9a66");
        Person providerPerson1 = createPersonWithUuid("12345678-a860-11e2-9e96-0800200c9a66");
        Person providerPerson2 = createPersonWithUuid("87654321-a860-11e2-9e96-0800200c9a66");

        Set<EncounterProvider> encounterProviders = createListWithEncounterProviders(
                                                    providerPerson, providerPerson1, providerPerson2);

        when(encounter.getEncounterProviders()).thenReturn(encounterProviders);

        User encounterCreator = createUserWithUuid("aaaaaaaa-a860-11e2-9e96-0800200c9a66");
        when(encounter.getCreator()).thenReturn(encounterCreator);

        assertTrue(encounterDomainWrapper.participatedInEncounter(currentUser));
    }

    @Test
    public void shouldReturnFalseWhenTheCurrentUserDoesNotParticipateInTheEncounter(){
        User currentUser = createUserWithUuid("585812f0-a860-11e2-9e96-0800200c9a66");

        Person providerPerson = createPersonWithUuid("99999999-a860-11e2-9e96-0800200c9a66");
        Person providerPerson1 = createPersonWithUuid("12345678-a860-11e2-9e96-0800200c9a66");
        Person providerPerson2 = createPersonWithUuid("87654321-a860-11e2-9e96-0800200c9a66");

        Set<EncounterProvider> encounterProviders = createListWithEncounterProviders(providerPerson, providerPerson1, providerPerson2);

        when(encounter.getEncounterProviders()).thenReturn(encounterProviders);

        User encounterCreator = createUserWithUuid("aaaaaaaa-a860-11e2-9e96-0800200c9a66");

        when(encounter.getCreator()).thenReturn(encounterCreator);

        assertFalse(encounterDomainWrapper.participatedInEncounter(currentUser));
    }

    @Test
    public void shouldCloseVisit(){

        Visit visit = new Visit();
        visit.setStartDatetime(yesterday());

        when(encounter.getVisit()).thenReturn(visit);

        encounterDomainWrapper.closeVisit();
        Visit closedVisit = encounterDomainWrapper.getVisit();

        assertThat(closedVisit.getStopDatetime(), isJustNow());

    }

    @Test(expected = EncounterDateBeforeVisitStartDateException.class)
    public void test_attachToVisit_shouldFailIfEncounterDateBeforeVisitStartDate()
            throws Exception {

        Encounter encounter = new Encounter();
        encounter.setEncounterDatetime(new DateMidnight(2012,12,12).toDate());
        EncounterDomainWrapper  encounterWrapper = new EncounterDomainWrapper(encounter);

        Visit visit = new Visit();
        visit.setStartDatetime(new DateTime(2012, 12, 13, 10, 10, 10).toDate());
        visit.setStopDatetime(new DateTime(2012, 12, 15, 10, 10, 10).toDate());

        encounterWrapper.attachToVisit(visit);
    }

    @Test(expected = EncounterDateAfterVisitStopDateException.class)
    public void test_attachToVisit_shouldFailIfEncounterDateAfterVisitStopDate()
            throws Exception {

        Encounter encounter = new Encounter();
        encounter.setEncounterDatetime(new DateMidnight(2012,12,16).toDate());
        EncounterDomainWrapper  encounterWrapper = new EncounterDomainWrapper(encounter);

        Visit visit = new Visit();
        visit.setStartDatetime(new DateTime(2012, 12, 13, 10, 10, 10).toDate());
        visit.setStopDatetime(new DateTime(2012, 12, 15, 10, 10, 10).toDate());

        encounterWrapper.attachToVisit(visit);
    }

    @Test
    public void test_attachToVisit_shouldSetEncounterDatetimeToMidnightOfEncounterDate()
            throws Exception {

        Encounter encounter = new Encounter();
        encounter.setEncounterDatetime(new DateMidnight(2012,12,14).toDate());
        EncounterDomainWrapper  encounterWrapper = new EncounterDomainWrapper(encounter);

        Visit visit = new Visit();
        visit.setStartDatetime(new DateTime(2012, 12, 13, 10, 10, 10).toDate());
        visit.setStopDatetime(new DateTime(2012, 12, 15, 10, 10, 10).toDate());

        encounterWrapper.attachToVisit(visit);

        assertThat(encounter.getEncounterDatetime(), is(new DateMidnight(2012,12,14).toDate()));
        assertThat(encounter.getVisit(), is(visit));
    }

    @Test
    public void test_attachToVisit_shouldSetEncounterDatetimeToVisitStartDate()
            throws Exception {

        Encounter encounter = new Encounter();
        encounter.setEncounterDatetime(new DateMidnight(2012,12,13).toDate());
        EncounterDomainWrapper  encounterWrapper = new EncounterDomainWrapper(encounter);

        Visit visit = new Visit();
        visit.setStartDatetime(new DateTime(2012, 12, 13, 10, 10, 10).toDate());
        visit.setStopDatetime(new DateTime(2012, 12, 15, 10, 10, 10).toDate());

        encounterWrapper.attachToVisit(visit);

        assertThat(encounter.getEncounterDatetime(), is(visit.getStartDatetime()));
        assertThat(encounter.getVisit(), is(visit));
    }

    @Test
    public void test_attachToVisit_shouldSetEncounterDatetimeToMidnightOfEncounterDateForOpenVisitIfNotToday()
            throws Exception {

        Encounter encounter = new Encounter();
        encounter.setEncounterDatetime(new DateMidnight(2012,12,14).toDate());
        EncounterDomainWrapper  encounterWrapper = new EncounterDomainWrapper(encounter);

        Visit visit = new Visit();
        visit.setStartDatetime(new DateTime(2012, 12, 13, 10, 10, 10).toDate());

        encounterWrapper.attachToVisit(visit);

        assertThat(encounter.getEncounterDatetime(), is(new DateMidnight(2012,12,14).toDate()));
        assertThat(encounter.getVisit(), is(visit));
    }

    @Test
    public void test_attachToVisit_shouldSetEncounterDatetimeToVisitStartDateForOpenVisitIfNotToday()
            throws Exception {

        Encounter encounter = new Encounter();
        encounter.setEncounterDatetime(new DateMidnight(2012,12,13).toDate());
        EncounterDomainWrapper  encounterWrapper = new EncounterDomainWrapper(encounter);

        Visit visit = new Visit();
        visit.setStartDatetime(new DateTime(2012, 12, 13, 10, 10, 10).toDate());

        encounterWrapper.attachToVisit(visit);

        assertThat(encounter.getEncounterDatetime(), is(visit.getStartDatetime()));
        assertThat(encounter.getVisit(), is(visit));
    }

    @Test
    public void test_attachToVisit_shouldSetStartDateToVisitStartDateOnCurrentDayForClosedVisit()
            throws Exception {

        DateTime currentDateTime = new DateTime();
        DateMidnight currentDate = currentDateTime.toDateMidnight();

        Encounter encounter = new Encounter();
        encounter.setEncounterDatetime(currentDate.toDate());
        EncounterDomainWrapper  encounterWrapper = new EncounterDomainWrapper(encounter);

        Visit visit = new Visit();
        visit.setStartDatetime(currentDateTime.minus(1000).toDate());   // i guess this test will fail if run in the first minute of the day
        visit.setStopDatetime(currentDateTime.plus(1000).toDate());

        encounterWrapper.attachToVisit(visit);

        assertThat(encounter.getEncounterDatetime(), is(visit.getStartDatetime()));
        assertThat(encounter.getVisit(), is(visit));
    }

    @Test
    public void test_attachToVisit_shouldSetStartDateToCurrentDatetimeForOpenVisit()
            throws Exception {

        DateTime currentDate = new DateTime().withTime(0,0,0,0);

        Encounter encounter = new Encounter();
        encounter.setEncounterDatetime(currentDate.toDate());
        EncounterDomainWrapper  encounterWrapper = new EncounterDomainWrapper(encounter);

        Visit visit = new Visit();
        visit.setStartDatetime(currentDate.toDateMidnight().toDate());

        Date shouldBeLessThanEncounterDatetime = new Date();
        encounterWrapper.attachToVisit(visit);
        Date shouldBeGreaterThanEncounterDatetime = new Date();

        assertThat(encounter.getEncounterDatetime(), greaterThanOrEqualTo(shouldBeLessThanEncounterDatetime));
        assertThat(encounter.getEncounterDatetime(), lessThanOrEqualTo(shouldBeGreaterThanEncounterDatetime));
        assertThat(encounter.getVisit(), is(visit));
    }

    private Date yesterday() {
        Calendar startVisitDate = Calendar.getInstance();
        startVisitDate.add(Calendar.DAY_OF_MONTH, -1);
        return startVisitDate.getTime();
    }

    private Set<EncounterProvider> createListWithEncounterProviders(Person... persons) {

        Set<EncounterProvider> encounterProviders = new HashSet<EncounterProvider>();

        for (Person person : persons) {
            EncounterProvider encounterProvider = createEncounterProvider(person);
            encounterProviders.add(encounterProvider);
        }

        return encounterProviders;
    }

    private EncounterProvider createEncounterProvider(Person providerPerson) {
        Provider provider = new Provider();
        provider.setPerson(providerPerson);

        EncounterProvider encounterProvider = new EncounterProvider();
        encounterProvider.setProvider(provider);
        return encounterProvider;
    }

    private User createUserWithUuid(String uuid) {
        Person person = createPersonWithUuid(uuid);

        User encounterCreator = new User(person);
        encounterCreator.setUuid(uuid);
        return encounterCreator;
    }

    private Person createPersonWithUuid(String uuid) {
        Person person = new Person();
        person.setUuid(uuid);
        return person;
    }

}
