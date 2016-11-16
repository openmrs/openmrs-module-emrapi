package org.openmrs.module.emrapi.visit;


import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.disposition.Disposition;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.disposition.DispositionType;
import org.openmrs.module.emrapi.test.MockMetadataTestUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.it.modular.hamcrest.date.DateMatchers.within;

@PrepareForTest(Calendar.class)
@RunWith(PowerMockRunner.class)
public class VisitDomainWrapperTest {

    private VisitDomainWrapper visitDomainWrapper;
    private Visit visit;
    private EmrApiProperties emrApiProperties;
    private DispositionService dispositionService;
    private ConceptService conceptService;
    private DiagnosisMetadata diagnosisMetadata;
    private DispositionDescriptor dispositionDescriptor;

    @Before
    public void setUp(){
        visit = mock(Visit.class);
        emrApiProperties = mock(EmrApiProperties.class);
        dispositionService = mock(DispositionService.class);
        conceptService = mock(ConceptService.class);
        MockMetadataTestUtil.setupMockConceptService(conceptService, emrApiProperties);
        diagnosisMetadata = MockMetadataTestUtil.setupDiagnosisMetadata(emrApiProperties, conceptService);
        dispositionDescriptor = MockMetadataTestUtil.setupDispositionDescriptor(conceptService);
        when(dispositionService.getDispositionDescriptor()).thenReturn(dispositionDescriptor);
        when(dispositionService.dispositionsSupported()).thenReturn(true);
        visitDomainWrapper = new VisitDomainWrapper(visit, emrApiProperties, dispositionService);
    }

    // this test was merged in when VisitSummary was merged into VisitDomainWrapper
    @Test
    public void shouldReturnMostRecentNonVoidedEncounterAndCheckInEncounter() throws Exception {
        EncounterType checkInEncounterType = new EncounterType();

        EmrApiProperties props = mock(EmrApiProperties.class);
        when(props.getCheckInEncounterType()).thenReturn(checkInEncounterType);

        Encounter checkIn = new Encounter();
        checkIn.setEncounterDatetime(DateUtils.addHours(new Date(), -3));
        checkIn.setEncounterType(checkInEncounterType);
        Encounter vitals = new Encounter();
        vitals.setEncounterDatetime(DateUtils.addHours(new Date(), -2));
        Encounter visitNote = new Encounter();
        visitNote.setEncounterDatetime(DateUtils.addHours(new Date(), -1));
        Encounter voided = new Encounter();
        voided.setVoided(true);
        visitNote.setEncounterDatetime(new Date());

        // per the hbm.xml file, visit.encounters are sorted by encounterDatetime desc
        Visit visit = new Visit();
        visit.setStartDatetime(checkIn.getEncounterDatetime());
        visit.setEncounters(new LinkedHashSet<Encounter>(4));
        visit.addEncounter(voided);
        visit.addEncounter(visitNote);
        visit.addEncounter(vitals);
        visit.addEncounter(checkIn);

        VisitDomainWrapper wrapper = new VisitDomainWrapper(visit, props);
        assertThat(wrapper.getMostRecentCheckInEncounter(), is(checkIn));
        assertThat(wrapper.getMostRecentEncounter(), is(visitNote));
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
    public void shouldNotBeAdmittedWhenNeverAdmitted() throws Exception {
        when(visit.getEncounters()).thenReturn(Collections.<Encounter>emptySet());
        visitDomainWrapper.setEmrApiProperties(mock(EmrApiProperties.class));

        assertFalse(visitDomainWrapper.isAdmitted());
    }

    @Test
    public void shouldBeAdmittedWhenAlreadyAdmitted() throws Exception {
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
    public void shouldNotBeAdmittedWhenAdmittedAndDischarged() throws Exception {
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
    public void shouldNotBeAdmittedWhenAdmissionIsVoided() throws Exception {
        EncounterType admitEncounterType = new EncounterType();

        EmrApiProperties props = mock(EmrApiProperties.class);
        when(props.getAdmissionEncounterType()).thenReturn(admitEncounterType);
        visitDomainWrapper.setEmrApiProperties(props);

        Encounter admit = new Encounter();
        admit.setEncounterType(admitEncounterType);
        admit.setEncounterDatetime(DateUtils.addHours(new Date(), -2));
        admit.setVoided(true);

        Set<Encounter> encounters = new LinkedHashSet<Encounter>();
        encounters.add(admit);
        when(visit.getEncounters()).thenReturn(encounters);

        assertFalse(visitDomainWrapper.isAdmitted());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldFailIfDateOutsideOfVisit() {
        Date now = new Date();
        when(visit.getStartDatetime()).thenReturn(DateUtils.addHours(new Date(), -3));
        visitDomainWrapper.isAdmitted(DateUtils.addHours(now, -4));
    }

    @Test
    public void shouldNotBeAdmittedIfTestDateBeforeAdmitDate() throws Exception {

        EncounterType admitEncounterType = new EncounterType();
        EncounterType dischargeEncounterType = new EncounterType();

        EmrApiProperties props = mock(EmrApiProperties.class);
        when(props.getAdmissionEncounterType()).thenReturn(admitEncounterType);
        when(props.getExitFromInpatientEncounterType()).thenReturn(dischargeEncounterType);
        visitDomainWrapper.setEmrApiProperties(props);

        when(visit.getStartDatetime()).thenReturn(DateUtils.addHours(new Date(), -5));

        Encounter admit = new Encounter();
        admit.setEncounterType(admitEncounterType);
        admit.setEncounterDatetime(DateUtils.addHours(new Date(), -3));

        Encounter discharge = new Encounter();
        discharge.setEncounterType(dischargeEncounterType);
        discharge.setEncounterDatetime(DateUtils.addHours(new Date(), -1));

        Set<Encounter> encounters = new LinkedHashSet<Encounter>();
        encounters.add(discharge);
        encounters.add(admit);
        when(visit.getEncounters()).thenReturn(encounters);

       assertFalse(visitDomainWrapper.isAdmitted(DateUtils.addHours(new Date(), -4)));
    }

    @Test
    public void shouldBeAdmittedIfTestDateBetweenAdmissionAndDischargeDate() throws Exception {

        EncounterType admitEncounterType = new EncounterType();
        EncounterType dischargeEncounterType = new EncounterType();

        EmrApiProperties props = mock(EmrApiProperties.class);
        when(props.getAdmissionEncounterType()).thenReturn(admitEncounterType);
        when(props.getExitFromInpatientEncounterType()).thenReturn(dischargeEncounterType);
        visitDomainWrapper.setEmrApiProperties(props);

        when(visit.getStartDatetime()).thenReturn(DateUtils.addHours(new Date(), -5));

        Encounter admit = new Encounter();
        admit.setEncounterType(admitEncounterType);
        admit.setEncounterDatetime(DateUtils.addHours(new Date(), -3));

        Encounter discharge = new Encounter();
        discharge.setEncounterType(dischargeEncounterType);
        discharge.setEncounterDatetime(DateUtils.addHours(new Date(), -1));

        Set<Encounter> encounters = new LinkedHashSet<Encounter>();
        encounters.add(discharge);
        encounters.add(admit);
        when(visit.getEncounters()).thenReturn(encounters);

        assertTrue(visitDomainWrapper.isAdmitted(DateUtils.addHours(new Date(), -2)));
    }

    @Test
    public void shouldNotAdmittedIfTestDateAfterAndDischargeDate() throws Exception {

        EncounterType admitEncounterType = new EncounterType();
        EncounterType dischargeEncounterType = new EncounterType();

        EmrApiProperties props = mock(EmrApiProperties.class);
        when(props.getAdmissionEncounterType()).thenReturn(admitEncounterType);
        when(props.getExitFromInpatientEncounterType()).thenReturn(dischargeEncounterType);
        visitDomainWrapper.setEmrApiProperties(props);

        when(visit.getStartDatetime()).thenReturn(DateUtils.addHours(new Date(), -5));

        Encounter admit = new Encounter();
        admit.setEncounterType(admitEncounterType);
        admit.setEncounterDatetime(DateUtils.addHours(new Date(), -3));

        Encounter discharge = new Encounter();
        discharge.setEncounterType(dischargeEncounterType);
        discharge.setEncounterDatetime(DateUtils.addHours(new Date(), -1));

        Set<Encounter> encounters = new LinkedHashSet<Encounter>();
        encounters.add(discharge);
        encounters.add(admit);
        when(visit.getEncounters()).thenReturn(encounters);

        assertFalse(visitDomainWrapper.isAdmitted(new Date()));
    }

   @Test
   public void shouldReturnCurrentLocationForAdmittedPatient() {

       EncounterType admitEncounterType = new EncounterType();
       EncounterType transferEncounterType = new EncounterType();

       Location icu = new Location();
       Location surgery = new Location();

       when(visit.getStartDatetime()).thenReturn(DateUtils.addHours(new Date(), -5));

       EmrApiProperties props = mock(EmrApiProperties.class);
       when(props.getAdmissionEncounterType()).thenReturn(admitEncounterType);
       when(props.getTransferWithinHospitalEncounterType()).thenReturn(transferEncounterType);
       visitDomainWrapper.setEmrApiProperties(props);

       Encounter admit = new Encounter();
       admit.setEncounterType(admitEncounterType);
       admit.setEncounterDatetime(DateUtils.addHours(new Date(), -3));
       admit.setLocation(icu);

       Encounter transfer = new Encounter();
       transfer.setEncounterType(transferEncounterType);
       transfer.setEncounterDatetime(DateUtils.addHours(new Date(), -1));
       transfer.setLocation(surgery);

       Set<Encounter> encounters = new LinkedHashSet<Encounter>();
       encounters.add(transfer);
       encounters.add(admit);
       when(visit.getEncounters()).thenReturn(encounters);

       assertThat(visitDomainWrapper.getInpatientLocation(DateUtils.addHours(new Date(), -2)), is(icu));
       assertThat(visitDomainWrapper.getInpatientLocation(new Date()), is(surgery));

   }

    @Test
    public void shouldReturnNullIfPatientNotAdmittedOnDate() {

        EncounterType admitEncounterType = new EncounterType();
        EncounterType transferEncounterType = new EncounterType();

        Location icu = new Location();

        when(visit.getStartDatetime()).thenReturn(DateUtils.addHours(new Date(), -5));

        EmrApiProperties props = mock(EmrApiProperties.class);
        when(props.getAdmissionEncounterType()).thenReturn(admitEncounterType);
        when(props.getTransferWithinHospitalEncounterType()).thenReturn(transferEncounterType);
        visitDomainWrapper.setEmrApiProperties(props);

        Encounter admit = new Encounter();
        admit.setEncounterType(admitEncounterType);
        admit.setEncounterDatetime(DateUtils.addHours(new Date(), -3));
        admit.setLocation(icu);

        Set<Encounter> encounters = new LinkedHashSet<Encounter>();
        encounters.add(admit);
        when(visit.getEncounters()).thenReturn(encounters);

        assertNull(visitDomainWrapper.getInpatientLocation(DateUtils.addHours(new Date(), -4)));
    }

    @Test
    public void shouldReturnNullIfPatientDischargedOnDate() {

        EncounterType admitEncounterType = new EncounterType();
        EncounterType dischargeEncounterType = new EncounterType();

        Location icu = new Location();
        when(visit.getStartDatetime()).thenReturn(DateUtils.addHours(new Date(), -5));

        EmrApiProperties props = mock(EmrApiProperties.class);
        when(props.getAdmissionEncounterType()).thenReturn(admitEncounterType);
        when(props.getExitFromInpatientEncounterType()).thenReturn(dischargeEncounterType);
        visitDomainWrapper.setEmrApiProperties(props);

        Encounter admit = new Encounter();
        admit.setEncounterType(admitEncounterType);
        admit.setEncounterDatetime(DateUtils.addHours(new Date(), -3));
        admit.setLocation(icu);

        Encounter discharge= new Encounter();
        discharge.setEncounterType(dischargeEncounterType);
        discharge.setEncounterDatetime(DateUtils.addHours(new Date(), -1));

        Set<Encounter> encounters = new LinkedHashSet<Encounter>();
        encounters.add(discharge);
        encounters.add(admit);
        when(visit.getEncounters()).thenReturn(encounters);

        assertNull(visitDomainWrapper.getInpatientLocation(new Date()));
    }



    @Test
    public void shouldNotHaveEncounterWithoutSubsequentEncounterIfNoRelevantEncounters() throws Exception {
        when(visit.getEncounters()).thenReturn(new LinkedHashSet<Encounter>());
        assertFalse(visitDomainWrapper.hasEncounterWithoutSubsequentEncounter(new EncounterType(), new EncounterType()));
    }

    @Test
    public void shouldHaveEncounterWithoutSubsequentEncounterIfOneEncounterOfCorrectType() throws Exception {
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
    public void shouldNotHaveEncounterWithoutSubsequentEncounterIfOneEncounterOfWrongType() throws Exception {
        Encounter encounter = new Encounter();
        encounter.setEncounterType(new EncounterType());
        encounter.setEncounterDatetime(new Date());

        LinkedHashSet<Encounter> encounters = new LinkedHashSet<Encounter>();
        encounters.add(encounter);

        when(visit.getEncounters()).thenReturn(encounters);
        assertFalse(visitDomainWrapper.hasEncounterWithoutSubsequentEncounter(new EncounterType(), null));
    }

    @Test
    public void shouldHaveEncounterWithoutSubsequentEncounterIfMostRecentRelevantEncounterIsOfCorrectType() throws Exception {
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

    @Test
    public void shouldUseTheStopDateOfTheVisitForEncounterStopDateRange() {
        DateTime visitEndDate = new DateTime(2013, 1, 15, 12, 12, 12);

        Visit visit = new Visit();
        visit.setStopDatetime(visitEndDate.toDate());

        VisitDomainWrapper wrapper = new VisitDomainWrapper(visit);

        assertThat(wrapper.getEncounterStopDateRange(), is(visitEndDate.toDate()));
    }

    @Test
    public void shouldReturnNowForStopDateRangeIfStopDateOfTheVisitIsNull() {
        Visit visit = new Visit();
        visit.setStopDatetime(null);

        VisitDomainWrapper wrapper = new VisitDomainWrapper(visit);

        assertThat(wrapper.getEncounterStopDateRange(), within(1, SECONDS, new Date()));
    }

    @Test
    public void shouldReturnOldestNonVoidedEncounter() throws Exception {
        Visit visit = new Visit();

        Encounter voidedEncounter = new Encounter();
        voidedEncounter.setId(0);
        voidedEncounter.setVoided(true);

        Encounter encounter1 = new Encounter();
        encounter1.setId(1);
        encounter1.setEncounterDatetime(DateUtils.addMinutes(new Date(), -1));

        Encounter encounter2 = new Encounter();
        encounter2.setId(2);
        encounter2.setEncounterDatetime(new Date());

        visit.addEncounter(voidedEncounter);
        visit.addEncounter(encounter2);
        visit.addEncounter(encounter1);

        assertThat(new VisitDomainWrapper(visit).getEarliestEncounter(), is(encounter1));
    }

    @Test
    public void shouldReturnNullOnMostRecentEncounterIfNoEncounters() throws Exception {
        assertThat(new VisitDomainWrapper(new Visit()).getMostRecentEncounter(), is(nullValue()));
    }

    @Test
    public void shouldCloseOnLastEncounterDate() throws Exception {

        Date startDate = new DateTime(2012,2,20,10,10).toDate();
        Date firstEncounterDate = new DateTime(2012,2,24,10,10).toDate();
        Date secondEncounterDate = new DateTime(2012,2,28,10,10).toDate();

        Visit visit = new Visit();
        visit.setStartDatetime(startDate);

        Encounter encounter = new Encounter();
        encounter.setEncounterDatetime(secondEncounterDate);

        Encounter anotherEncounter = new Encounter();
        anotherEncounter.setEncounterDatetime(firstEncounterDate);

        visit.addEncounter(encounter);
        visit.addEncounter(anotherEncounter);

        new VisitDomainWrapper(visit).closeOnLastEncounterDatetime();

        assertThat(visit.getStopDatetime(), is(secondEncounterDate));

    }

    @Test(expected = IllegalStateException.class)
    public void shouldFailIfNoEncounters() throws Exception {

        Date startDate = new DateTime(2012,2,20,10,10).toDate();

        Visit visit = new Visit();
        visit.setStartDatetime(startDate);

        new VisitDomainWrapper(visit).closeOnLastEncounterDatetime();
    }

    @Test
    public void shouldReturnDispositionFromObs() throws Exception {

        Encounter mostRecentEncounter = new Encounter();
        mostRecentEncounter.setEncounterDatetime(new DateTime(2012,12,12,12,12).toDate());

        Encounter secondMostRecentEncounter = new Encounter();
        secondMostRecentEncounter.setEncounterDatetime(new DateTime(2012,11,11,11,11).toDate());

        Encounter thirdMostRecentEncounter = new Encounter();
        thirdMostRecentEncounter.setEncounterDatetime(new DateTime(2012, 10, 10, 10,10).toDate());

        Obs mostRecentDispositionGroup = new Obs();
        mostRecentDispositionGroup.setConcept(dispositionDescriptor.getDispositionSetConcept());
        Obs mostRecentOtherObsGroup = new Obs();
        mostRecentOtherObsGroup.setConcept(new Concept());
        mostRecentEncounter.addObs(mostRecentDispositionGroup);
        mostRecentEncounter.addObs(mostRecentOtherObsGroup);

        Obs secondMostRecentDispositionGroup = new Obs();
        secondMostRecentDispositionGroup.setConcept(dispositionDescriptor.getDispositionSetConcept());
        secondMostRecentEncounter.addObs(secondMostRecentDispositionGroup);

        Obs thirdMostRecentDispositionObsGroup = new Obs();
        thirdMostRecentDispositionObsGroup.setConcept(dispositionDescriptor.getDispositionSetConcept());
        thirdMostRecentEncounter.addObs(thirdMostRecentDispositionObsGroup);

        Set<Encounter> encounters = new HashSet<Encounter>();
        encounters.add(secondMostRecentEncounter);
        encounters.add(mostRecentEncounter);
        encounters.add(thirdMostRecentEncounter);
        when(visit.getEncounters()).thenReturn(encounters);

        visitDomainWrapper.getMostRecentDisposition();
        verify(dispositionService).getDispositionFromObsGroup(mostRecentDispositionGroup);
    }

    @Test
    public void shouldReturnAllPrimaryDiagnosesFromVisit() {

        Encounter encounter = new Encounter();
        encounter.setEncounterDatetime(new DateTime(2012, 12, 12, 12, 12).toDate());

        Encounter encounter2 = new Encounter();
        encounter2.setEncounterDatetime(new DateTime(2012,11,11,11,11).toDate());

        Diagnosis primaryDiagnosis1 = new Diagnosis();
        Concept primaryDiagnosisConcept1 = new Concept();
        primaryDiagnosis1.setDiagnosis(new CodedOrFreeTextAnswer(primaryDiagnosisConcept1));
        primaryDiagnosis1.setOrder(Diagnosis.Order.PRIMARY);
        primaryDiagnosis1.setCertainty(Diagnosis.Certainty.CONFIRMED);
        encounter.addObs(diagnosisMetadata.buildDiagnosisObsGroup(primaryDiagnosis1));

        Diagnosis primaryDiagnosis2 = new Diagnosis();
        Concept primaryDiagnosisConcept2 = new Concept();
        primaryDiagnosis2.setDiagnosis(new CodedOrFreeTextAnswer(primaryDiagnosisConcept2));
        primaryDiagnosis2.setOrder(Diagnosis.Order.PRIMARY);
        primaryDiagnosis2.setCertainty(Diagnosis.Certainty.PRESUMED);
        encounter2.addObs(diagnosisMetadata.buildDiagnosisObsGroup(primaryDiagnosis2));

        Diagnosis secondaryDiagnosis = new Diagnosis();
        Concept secondaryDiagnosisConcept = new Concept();
        secondaryDiagnosis.setDiagnosis(new CodedOrFreeTextAnswer(secondaryDiagnosisConcept));
        secondaryDiagnosis.setOrder(Diagnosis.Order.SECONDARY);
        secondaryDiagnosis.setCertainty(Diagnosis.Certainty.PRESUMED);
        encounter2.addObs(diagnosisMetadata.buildDiagnosisObsGroup(secondaryDiagnosis));

        Set<Encounter> encounters = new HashSet<Encounter>();
        encounters.add(encounter);
        encounters.add(encounter2);
        when(visit.getEncounters()).thenReturn(encounters);

        List<Diagnosis> diagnoses = visitDomainWrapper.getPrimaryDiagnoses();

        assertThat(diagnoses.size(), is(2));
        assertThat(diagnoses, hasItem(primaryDiagnosis1));
        assertThat(diagnoses, hasItem(primaryDiagnosis2));

    }

	@Test
	public void shouldReturnUniqueDiagnoses() {

		Encounter encounter = new Encounter();
		encounter.setEncounterDatetime(new DateTime(2012, 12, 12, 12, 12).toDate());

		Set<Encounter> encounters = new HashSet<Encounter>();
		encounters.add(encounter);
		when(visit.getEncounters()).thenReturn(encounters);

		String diagnosis1 = "Diagnosis 1";
		String diagnosis2 = "Diagnosis 2";
		String diagnosis3 = "Diagnosis 3";

		// Only factor diagnosis into uniqueness
		addDiagnosis(encounter, new CodedOrFreeTextAnswer(diagnosis1), Diagnosis.Order.PRIMARY, Diagnosis.Certainty.CONFIRMED);
		addDiagnosis(encounter, new CodedOrFreeTextAnswer(diagnosis1), Diagnosis.Order.SECONDARY, Diagnosis.Certainty.PRESUMED);
		assertThat(visitDomainWrapper.getUniqueDiagnosesLegacy(false, false).size(), is(1));
		assertThat(visitDomainWrapper.getUniqueDiagnosesLegacy(true, false).size(), is(1));
		addDiagnosis(encounter, new CodedOrFreeTextAnswer(diagnosis2), Diagnosis.Order.PRIMARY, Diagnosis.Certainty.CONFIRMED);
		assertThat(visitDomainWrapper.getUniqueDiagnosesLegacy(false, false).size(), is(2));

		// Only return secondary or presumed if asked
		addDiagnosis(encounter, new CodedOrFreeTextAnswer(diagnosis3), Diagnosis.Order.SECONDARY, Diagnosis.Certainty.PRESUMED);
		assertThat(visitDomainWrapper.getUniqueDiagnosesLegacy(false, false).size(), is(3));
		assertThat(visitDomainWrapper.getUniqueDiagnosesLegacy(true, false).size(), is(2));
		assertThat(visitDomainWrapper.getUniqueDiagnosesLegacy(false, true).size(), is(2));
		assertThat(visitDomainWrapper.getUniqueDiagnosesLegacy(true, true).size(), is(2));
	}

    @Test
    public void shouldReturnAllDiagnosesFromMostRecentEncounterWithAdmitDisposition() {

        // create three encounters
        Encounter encounterWithTransferDisposition = new Encounter();
        encounterWithTransferDisposition.setEncounterDatetime(new DateTime(2012, 12, 12, 12, 12).toDate());

        Encounter mostRecentEncounterWithAdmitDisposition = new Encounter();
        mostRecentEncounterWithAdmitDisposition.setEncounterDatetime(new DateTime(2012, 11, 11, 11, 11).toDate());

        Encounter olderEncounterWithAdmitDisposition = new Encounter();
        olderEncounterWithAdmitDisposition.setEncounterDatetime(new DateTime(2012, 10, 10, 10, 10).toDate());

        // create two dispositions
        Disposition transfer = new Disposition();
        transfer.setType(DispositionType.TRANSFER);

        Disposition admit = new Disposition();
        admit.setType(DispositionType.ADMIT);

        // create three disposition obs groups
        Obs transferDispositionObsGroup = new Obs();
        transferDispositionObsGroup.setConcept(dispositionDescriptor.getDispositionSetConcept());
        when(dispositionService.getDispositionFromObsGroup(transferDispositionObsGroup)).thenReturn(transfer);

        Obs mostRecentAdmitDispositionObsGroup = new Obs();
        mostRecentAdmitDispositionObsGroup.setConcept(dispositionDescriptor.getDispositionSetConcept());
        when(dispositionService.getDispositionFromObsGroup(mostRecentAdmitDispositionObsGroup)).thenReturn(admit);

        Obs olderAdmitDispositionObsGroup = new Obs();
        olderAdmitDispositionObsGroup.setConcept(dispositionDescriptor.getDispositionSetConcept());
        when(dispositionService.getDispositionFromObsGroup(olderAdmitDispositionObsGroup)).thenReturn(admit);

        // create four diagnoses
        Diagnosis primaryDiagnosis1 = new Diagnosis();
        Concept primaryDiagnosisConcept1 = new Concept();
        primaryDiagnosis1.setDiagnosis(new CodedOrFreeTextAnswer(primaryDiagnosisConcept1));
        primaryDiagnosis1.setOrder(Diagnosis.Order.PRIMARY);
        primaryDiagnosis1.setCertainty(Diagnosis.Certainty.CONFIRMED);

        Diagnosis primaryDiagnosis2 = new Diagnosis();
        Concept primaryDiagnosisConcept2 = new Concept();
        primaryDiagnosis2.setDiagnosis(new CodedOrFreeTextAnswer(primaryDiagnosisConcept2));
        primaryDiagnosis2.setOrder(Diagnosis.Order.PRIMARY);
        primaryDiagnosis2.setCertainty(Diagnosis.Certainty.PRESUMED);

        Diagnosis primaryDiagnosis3 = new Diagnosis();
        Concept primaryDiagnosisConcept3 = new Concept();
        primaryDiagnosis3.setDiagnosis(new CodedOrFreeTextAnswer(primaryDiagnosisConcept3));
        primaryDiagnosis3.setOrder(Diagnosis.Order.PRIMARY);
        primaryDiagnosis3.setCertainty(Diagnosis.Certainty.PRESUMED);

        Diagnosis secondaryDiagnosis = new Diagnosis();
        Concept secondaryDiagnosisConcept = new Concept();
        secondaryDiagnosis.setDiagnosis(new CodedOrFreeTextAnswer(secondaryDiagnosisConcept));
        secondaryDiagnosis.setOrder(Diagnosis.Order.SECONDARY);
        secondaryDiagnosis.setCertainty(Diagnosis.Certainty.PRESUMED);

        // now build the three encounters from the component parts we have created
        encounterWithTransferDisposition.addObs(transferDispositionObsGroup);
        encounterWithTransferDisposition.addObs(diagnosisMetadata.buildDiagnosisObsGroup(primaryDiagnosis1));

        mostRecentEncounterWithAdmitDisposition.addObs(mostRecentAdmitDispositionObsGroup);
        mostRecentEncounterWithAdmitDisposition.addObs(diagnosisMetadata.buildDiagnosisObsGroup(primaryDiagnosis2));
        mostRecentEncounterWithAdmitDisposition.addObs(diagnosisMetadata.buildDiagnosisObsGroup(secondaryDiagnosis));

        olderEncounterWithAdmitDisposition.addObs(olderAdmitDispositionObsGroup);
        olderEncounterWithAdmitDisposition.addObs(diagnosisMetadata.buildDiagnosisObsGroup(primaryDiagnosis2));

        Set<Encounter> encounters = new HashSet<Encounter>();
        encounters.add(encounterWithTransferDisposition);
        encounters.add(mostRecentEncounterWithAdmitDisposition);
        encounters.add(olderEncounterWithAdmitDisposition);
        when(visit.getEncounters()).thenReturn(encounters);

        List<Diagnosis> diagnoses = visitDomainWrapper.getDiagnosesFromMostRecentDispositionByType(DispositionType.ADMIT);

        // should only contain the diagnoses from the mostRecentEncounterWithAdmitDisposition
        assertThat(diagnoses.size(), is(2));
        assertThat(diagnoses, hasItem(secondaryDiagnosis));
        assertThat(diagnoses, hasItem(primaryDiagnosis2));

    }

    @Test
    public void getDiagnosesFromMostRecentDispositionShouldNotFailIfDispositionHasNoType() {

        Encounter encounterWithDisposition = new Encounter();
        encounterWithDisposition.setEncounterDatetime(new DateTime(2012, 12, 12, 12, 12).toDate());

        Disposition dispositionWithoutType = new Disposition();

        Obs dispositionObsGroup = new Obs();
        dispositionObsGroup.setConcept(dispositionDescriptor.getDispositionSetConcept());
        when(dispositionService.getDispositionFromObsGroup(dispositionObsGroup)).thenReturn(dispositionWithoutType);

        encounterWithDisposition.addObs(dispositionObsGroup);

        Set<Encounter> encounters = new HashSet<Encounter>();
        encounters.add(encounterWithDisposition);
        when(visit.getEncounters()).thenReturn(encounters);

        List<Diagnosis> diagnoses = visitDomainWrapper.getDiagnosesFromMostRecentDispositionByType(DispositionType.ADMIT);

        // no assertions, just want to make sure we can do this without receiving NPE

    }



    @Test
    public void shouldReturnEmptyListIfNoDiagnoses() {

        Encounter encounter = new Encounter();
        encounter.setEncounterDatetime(new DateTime(2012, 12, 12, 12, 12).toDate());

        Set<Encounter> encounters = new HashSet<Encounter>();
        encounters.add(encounter);
        when(visit.getEncounters()).thenReturn(encounters);

        List<Diagnosis> diagnoses = visitDomainWrapper.getPrimaryDiagnoses();

        assertThat(diagnoses.size(), is(0));

    }

    @Test
    public void shouldReturnMostRecentNonVoidedVisitNote() throws Exception {

        EncounterType visitNoteEncounterType = new EncounterType();

        EmrApiProperties props = mock(EmrApiProperties.class);
        when(props.getVisitNoteEncounterType()).thenReturn(visitNoteEncounterType);

        Encounter visitNote1 = new Encounter();
        visitNote1.setEncounterDatetime(DateUtils.addHours(new Date(), -3));
        visitNote1.setEncounterType(visitNoteEncounterType);

        Encounter vitals = new Encounter();
        vitals.setEncounterDatetime(DateUtils.addHours(new Date(), -2));

        Encounter visitNote2 = new Encounter();
        visitNote2.setEncounterDatetime(DateUtils.addHours(new Date(), -1));
        visitNote2.setEncounterType(visitNoteEncounterType);

        Encounter visitNote3 = new Encounter();
        visitNote3.setEncounterDatetime(DateUtils.addHours(new Date(), 0));
        visitNote3.setEncounterType(visitNoteEncounterType);
        visitNote3.setVoided(true);


        // per the hbm.xml file, visit.encounters are sorted by encounterDatetime desc
        Visit visit = new Visit();
        visit.setStartDatetime(visitNote1.getEncounterDatetime());
        visit.addEncounter(visitNote1);
        visit.addEncounter(visitNote2);
        visit.addEncounter(visitNote3);
        visit.addEncounter(vitals);

        VisitDomainWrapper wrapper = new VisitDomainWrapper(visit, props);
        assertTrue(wrapper.hasVisitNote());
        assertThat(wrapper.getMostRecentVisitNote(), is(visitNote2));  // visitNote #3 is voided
    }

    @Test
    public void shouldReturnNullIfNoVisitNote() throws Exception {

        EncounterType visitNoteEncounterType = new EncounterType();

        EmrApiProperties props = mock(EmrApiProperties.class);
        when(props.getVisitNoteEncounterType()).thenReturn(visitNoteEncounterType);

        Encounter vitals = new Encounter();
        vitals.setEncounterDatetime(DateUtils.addHours(new Date(), -2));

        Encounter visitNote = new Encounter();
        visitNote.setEncounterDatetime(DateUtils.addHours(new Date(), 0));
        visitNote.setEncounterType(visitNoteEncounterType);
        visitNote.setVoided(true);   // note that this visitNote is voided


        // per the hbm.xml file, visit.encounters are sorted by encounterDatetime desc
        Visit visit = new Visit();
        visit.addEncounter(visitNote);
        visit.addEncounter(vitals);

        VisitDomainWrapper wrapper = new VisitDomainWrapper(visit, props);
        assertFalse(wrapper.hasVisitNote());
        assertNull(wrapper.getMostRecentVisitNote());

    }

    @Test
    public void shouldReturnMostRecentNonVoidedVisitNoteAtLocation() throws Exception {

        EncounterType visitNoteEncounterType = new EncounterType();
        Location location1 = new Location();
        Location location2 = new Location();
        Location otherLocation = new Location();

        EmrApiProperties props = mock(EmrApiProperties.class);
        when(props.getVisitNoteEncounterType()).thenReturn(visitNoteEncounterType);

        Encounter visitNote1 = new Encounter();
        visitNote1.setEncounterDatetime(DateUtils.addHours(new Date(), -3));
        visitNote1.setEncounterType(visitNoteEncounterType);
        visitNote1.setLocation(location1);

        Encounter visitNote2 = new Encounter();
        visitNote2.setEncounterDatetime(DateUtils.addHours(new Date(), -1));
        visitNote2.setEncounterType(visitNoteEncounterType);
        visitNote2.setLocation(location2);

        // per the hbm.xml file, visit.encounters are sorted by encounterDatetime desc
        Visit visit = new Visit();
        visit.setStartDatetime(visitNote1.getEncounterDatetime());
        visit.addEncounter(visitNote1);
        visit.addEncounter(visitNote2);

        VisitDomainWrapper wrapper = new VisitDomainWrapper(visit, props);

        assertThat(wrapper.getMostRecentVisitNoteAtLocation(location1), is(visitNote1));
        assertTrue(wrapper.hasVisitNoteAtLocation(location1));

        assertThat(wrapper.getMostRecentVisitNoteAtLocation(location2), is(visitNote2));
        assertTrue(wrapper.hasVisitNoteAtLocation(location2));

        assertNull(wrapper.getMostRecentVisitNoteAtLocation(otherLocation));
        assertFalse(wrapper.hasVisitNoteAtLocation(otherLocation));
    }

    // this test was merged in when VisitSummary was merged into VisitDomainWrapper
    @Test
    public void shouldReturnFirstNonVoidedCheckInEncounter() throws Exception {

        EncounterType checkInEncounterType = new EncounterType();

        EmrApiProperties props = mock(EmrApiProperties.class);
        when(props.getCheckInEncounterType()).thenReturn(checkInEncounterType);

        Encounter mostRecentCheckIn = new Encounter();
        mostRecentCheckIn.setEncounterDatetime(DateUtils.addHours(new Date(), -1));
        mostRecentCheckIn.setEncounterType(checkInEncounterType);

        Encounter firstVoidedCheckin = new Encounter();
        firstVoidedCheckin.setEncounterDatetime(DateUtils.addHours(new Date(), -3));
        firstVoidedCheckin.setEncounterType(checkInEncounterType);
        firstVoidedCheckin.setVoided(true);

        Encounter firstNonVoidedCheckin = new Encounter();
        firstNonVoidedCheckin.setEncounterDatetime(DateUtils.addHours(new Date(), -2));
        firstNonVoidedCheckin.setEncounterType(checkInEncounterType);

        Visit visit = new Visit();
        visit.setStartDatetime(firstVoidedCheckin.getEncounterDatetime());
        visit.addEncounter(mostRecentCheckIn);
        visit.addEncounter(firstVoidedCheckin);
        visit.addEncounter(firstNonVoidedCheckin);

        VisitDomainWrapper wrapper = new VisitDomainWrapper(visit, props);
        assertThat(wrapper.getEarliestCheckInEncounter(), is(firstNonVoidedCheckin));
    }

    @Test
    public void shouldReturnStartDateWithTimeComponentRemoved() {
        when(visit.getStartDatetime()).thenReturn(new Date());
        assertThat(visitDomainWrapper.getStartDate(), is(new DateMidnight().toDate()));
    }

    @Test
    public void shouldReturnStopDateWithTimeComponentRemoved() {
        when(visit.getStopDatetime()).thenReturn(new Date());
        assertThat(visitDomainWrapper.getStopDate(), is(new DateMidnight().toDate()));
    }

    @Test
    public void shouldNotFailIfStartDatetimeNull() {
        when(visit.getStartDatetime()).thenReturn(null);
        assertNull(visitDomainWrapper.getStartDate());
    }

    @Test
    public void shouldNotFailIfStopDatetimeNull() {
        when(visit.getStopDatetime()).thenReturn(null);
        assertNull(visitDomainWrapper.getStopDate());
    }

    private class ExpectedDiagnosis extends ArgumentMatcher<Diagnosis> {

        private Diagnosis expectedDiagnosis;

        public ExpectedDiagnosis(Diagnosis expectedRequest) {
            this.expectedDiagnosis = expectedRequest;
        }

        @Override
        public boolean matches(Object o) {

            Diagnosis actualDiagnosis = (Diagnosis) o;

            boolean match = true;

            match = match && actualDiagnosis.getDiagnosis().getCodedAnswer() == expectedDiagnosis.getDiagnosis().getCodedAnswer();
            match = match && actualDiagnosis.getCertainty() == expectedDiagnosis.getCertainty();
            match = match && actualDiagnosis.getOrder() == expectedDiagnosis.getOrder();

            return match;
        }
    }

	private void addDiagnosis(Encounter encounter, CodedOrFreeTextAnswer diagnosis, Diagnosis.Order order, Diagnosis.Certainty certainty) {
		Diagnosis primaryDiagnosis = new Diagnosis();
		primaryDiagnosis.setDiagnosis(diagnosis);
		primaryDiagnosis.setOrder(order);
		primaryDiagnosis.setCertainty(certainty);
		encounter.addObs(diagnosisMetadata.buildDiagnosisObsGroup(primaryDiagnosis));
	}

}
