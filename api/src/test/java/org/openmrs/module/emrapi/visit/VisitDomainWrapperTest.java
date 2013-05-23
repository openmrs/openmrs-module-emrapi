package org.openmrs.module.emrapi.visit;


import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@PrepareForTest(Calendar.class)
@RunWith(PowerMockRunner.class)
public class VisitDomainWrapperTest {

    private VisitDomainWrapper visitDomainWrapper;
    private Visit visit;

    @Before
    public void setUp(){
        visit = mock(Visit.class);
        visitDomainWrapper = new VisitDomainWrapper(visit);
    }

    // this test was merged in when VisitSummary was merged into VisitDomainWrapper
    @Test
    public void test() throws Exception {
        EncounterType checkInEncounterType = new EncounterType();

        EmrApiProperties props = mock(EmrApiProperties.class);
        when(props.getCheckInEncounterType()).thenReturn(checkInEncounterType);

        Encounter checkIn = new Encounter();
        checkIn.setEncounterDatetime(DateUtils.addHours(new Date(), -2));
        checkIn.setEncounterType(checkInEncounterType);
        Encounter vitals = new Encounter();
        vitals.setEncounterDatetime(DateUtils.addHours(new Date(), -1));
        Encounter consult = new Encounter();
        consult.setEncounterDatetime(new Date());

        // per the hbm.xml file, visit.encounters are sorted by encounterDatetime desc
        Visit visit = new Visit();
        visit.setStartDatetime(checkIn.getEncounterDatetime());
        visit.setEncounters(new LinkedHashSet<Encounter>(3));
        visit.addEncounter(consult);
        visit.addEncounter(vitals);
        visit.addEncounter(checkIn);

        VisitDomainWrapper wrapper = new VisitDomainWrapper(visit, props);
        assertThat(wrapper.getCheckInEncounter(), is(checkIn));
        assertThat(wrapper.getLastEncounter(), is(consult));
    }

    @Test
    public void shouldReturnDifferenceInDaysBetweenCurrentDateAndStartDate(){
        Calendar startDate = Calendar.getInstance();
        startDate.add(DAY_OF_MONTH, -5);

        when(visit.getStartDatetime()).thenReturn(startDate.getTime());

        int days = visitDomainWrapper.getDifferenceInDaysBetweenCurrentDateAndStartDate();

        assertThat(days, is(5));
    }

    @Test
    public void shouldReturnDifferenceInDaysBetweenCurrentDateAndStartDateWhenTimeIsDifferent(){

        Calendar today = Calendar.getInstance();
        today.set(HOUR, 7);

        Calendar startDate = Calendar.getInstance();
        startDate.add(DAY_OF_MONTH, -5);
        startDate.set(HOUR, 9);

        PowerMockito.mockStatic(Calendar.class);
        when(Calendar.getInstance()).thenReturn(today);

        when(visit.getStartDatetime()).thenReturn(startDate.getTime());

        int days = visitDomainWrapper.getDifferenceInDaysBetweenCurrentDateAndStartDate();

        assertThat(days, is(5));

    }

    @Test
    public void test_isAdmitted_isFalseWhenNeverAdmitted() throws Exception {
        when(visit.getEncounters()).thenReturn(Collections.<Encounter>emptySet());
        visitDomainWrapper.setEmrApiProperties(mock(EmrApiProperties.class));

        assertFalse(visitDomainWrapper.isAdmitted());
    }

    @Test
    public void test_isAdmitted_isTrueWhenAlreadyAdmitted() throws Exception {
        EncounterType admitEncounterType = new EncounterType();

        EmrApiProperties props = mock(EmrApiProperties.class);
        when(props.getAdmissionEncounterType()).thenReturn(admitEncounterType);
        visitDomainWrapper.setEmrApiProperties(props);

        Encounter admit = new Encounter();
        admit.setEncounterType(admitEncounterType);

        Set<Encounter> encounters = new LinkedHashSet<Encounter>();
        encounters.add(admit);
        when(visit.getEncounters()).thenReturn(encounters);

        assertTrue(visitDomainWrapper.isAdmitted());
    }

    @Test
    public void test_isAdmitted_isFalseWhenAdmittedAndDischarged() throws Exception {
        EncounterType admitEncounterType = new EncounterType();
        EncounterType dischargeEncounterType = new EncounterType();

        EmrApiProperties props = mock(EmrApiProperties.class);
        when(props.getAdmissionEncounterType()).thenReturn(admitEncounterType);
        when(props.getExitFromInpatientEncounterType()).thenReturn(dischargeEncounterType);
        visitDomainWrapper.setEmrApiProperties(props);

        Encounter admit = new Encounter();
        admit.setEncounterType(admitEncounterType);
        admit.setEncounterDatetime(DateUtils.addHours(new Date(), -2));

        Encounter discharge = new Encounter();
        discharge.setEncounterType(dischargeEncounterType);
        discharge.setEncounterDatetime(DateUtils.addHours(new Date(), -1));

        Set<Encounter> encounters = new LinkedHashSet<Encounter>();
        encounters.add(discharge);
        encounters.add(admit);
        when(visit.getEncounters()).thenReturn(encounters);

        assertFalse(visitDomainWrapper.isAdmitted());
    }

    @Test
    public void test_hasEncounterWithoutSubsequentEncounter_returnsFalseIfNoRelevantEncounters() throws Exception {
        when(visit.getEncounters()).thenReturn(new LinkedHashSet<Encounter>());
        assertFalse(visitDomainWrapper.hasEncounterWithoutSubsequentEncounter(new EncounterType(), new EncounterType()));
    }

    @Test
    public void test_hasEncounterWithoutSubsequentEncounter_returnsTrueIfOneEncounterOfCorrectType() throws Exception {
        EncounterType targetType = new EncounterType();

        Encounter encounter = new Encounter();
        encounter.setEncounterType(targetType);
        encounter.setEncounterDatetime(new Date());

        LinkedHashSet<Encounter> encounters = new LinkedHashSet<Encounter>();
        encounters.add(encounter);

        when(visit.getEncounters()).thenReturn(encounters);
        assertTrue(visitDomainWrapper.hasEncounterWithoutSubsequentEncounter(targetType, null));
    }

    @Test
    public void test_hasEncounterWithoutSubsequentEncounter_returnsTrueIfOneEncounterOfWrongType() throws Exception {
        Encounter encounter = new Encounter();
        encounter.setEncounterType(new EncounterType());
        encounter.setEncounterDatetime(new Date());

        LinkedHashSet<Encounter> encounters = new LinkedHashSet<Encounter>();
        encounters.add(encounter);

        when(visit.getEncounters()).thenReturn(encounters);
        assertFalse(visitDomainWrapper.hasEncounterWithoutSubsequentEncounter(new EncounterType(), null));
    }

    @Test
    public void test_hasEncounterWithoutSubsequentEncounter_returnsTrueIfMostRecentRelevantEncounterIsOfCorrectType() throws Exception {
        EncounterType lookForType = new EncounterType();
        EncounterType cancelType = new EncounterType();

        Encounter admit = new Encounter();
        admit.setEncounterType(lookForType);
        admit.setEncounterDatetime(DateUtils.addHours(new Date(), -3));

        Encounter discharge = new Encounter();
        discharge.setEncounterType(cancelType);
        discharge.setEncounterDatetime(DateUtils.addHours(new Date(), -2));

        Encounter admitAgain = new Encounter();
        admitAgain.setEncounterType(lookForType);
        admitAgain.setEncounterDatetime(DateUtils.addHours(new Date(), -1));

        Encounter anotherEncounter = new Encounter();
        anotherEncounter.setEncounterType(new EncounterType());
        anotherEncounter.setEncounterDatetime(new Date());

        Set<Encounter> encounters = new HashSet<Encounter>();
        encounters.add(admit);
        encounters.add(discharge);
        encounters.add(admitAgain);
        encounters.add(anotherEncounter);

        when(visit.getEncounters()).thenReturn(encounters);
        assertTrue(visitDomainWrapper.hasEncounterWithoutSubsequentEncounter(lookForType, cancelType));
    }

}
