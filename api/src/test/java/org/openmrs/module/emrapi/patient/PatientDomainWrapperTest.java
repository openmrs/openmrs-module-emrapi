package org.openmrs.module.emrapi.patient;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;

public class PatientDomainWrapperTest {

    private PatientDomainWrapper patientDomainWrapper;
    private EmrApiProperties emrApiProperties;
    private Patient patient;
    private VisitService visitService;
    private DomainWrapperFactory domainWrapperFactory;

    @Before
    public void setUp() throws Exception {
        patient = new Patient();
        emrApiProperties = mock(EmrApiProperties.class);
        visitService = mock(VisitService.class);
        domainWrapperFactory = mock(DomainWrapperFactory.class);
        patientDomainWrapper = new PatientDomainWrapper(patient, emrApiProperties, mock(AdtService.class),
                visitService, mock(EncounterService.class), mock(DiagnosisService.class), domainWrapperFactory);
    }

    @Test
    public void shouldVerifyIfPatientIsUnknown() {

        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setPersonAttributeTypeId(10);
        personAttributeType.setName(EmrApiConstants.UNKNOWN_PATIENT_PERSON_ATTRIBUTE_TYPE_NAME);
        personAttributeType.setFormat("java.lang.String");

        PersonAttribute newAttribute = new PersonAttribute(personAttributeType, "true");
        patient.addAttribute(newAttribute);

        when(emrApiProperties.getUnknownPatientPersonAttributeType()).thenReturn(personAttributeType);

        assertTrue(patientDomainWrapper.isUnknownPatient());

    }

    @Test
    public void shouldVerifyIfPatientIsATest() {

        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setPersonAttributeTypeId(11);
        personAttributeType.setName("Test Patient");
        personAttributeType.setFormat("java.lang.Boolean");
        personAttributeType.setUuid(EmrApiConstants.TEST_PATIENT_ATTRIBUTE_UUID);

        PersonAttribute newAttribute = new PersonAttribute(personAttributeType, "true");

        patient.addAttribute(newAttribute);

        when(emrApiProperties.getTestPatientPersonAttributeType()).thenReturn(personAttributeType);

        assertTrue(patientDomainWrapper.isTestPatient());

    }

    @Test
    public void shouldVerifyIfPatientIsNotATest() {

        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setPersonAttributeTypeId(11);
        personAttributeType.setName("Test Patient");
        personAttributeType.setFormat("java.lang.Boolean");
        personAttributeType.setUuid(EmrApiConstants.TEST_PATIENT_ATTRIBUTE_UUID);

        PersonAttribute newAttribute = new PersonAttribute(personAttributeType, "false");

        patient.addAttribute(newAttribute);

        when(emrApiProperties.getTestPatientPersonAttributeType()).thenReturn(personAttributeType);

        assertFalse(patientDomainWrapper.isTestPatient());

    }

    @Test
    public void shouldCreateAListOfVisitDomainWrappersBasedOnVisitListFromVisitService() {
        when(visitService.getVisitsByPatient(patient, true, false)).thenReturn(asList(new Visit(), new Visit(), new Visit()));
        when(domainWrapperFactory.newVisitDomainWrapper(any(Visit.class))).thenReturn(new VisitDomainWrapper());

        List<VisitDomainWrapper> visitDomainWrappers = patientDomainWrapper.getAllVisitsUsingWrappers();

        assertThat(visitDomainWrappers.size(), is(3));
    }

    @Test
    public void shouldReturnFormattedName() {
        patient = mock(Patient.class);

        patientDomainWrapper = new PatientDomainWrapper(patient, emrApiProperties, mock(AdtService.class),
                visitService, mock(EncounterService.class), mock(DiagnosisService.class), mock(DomainWrapperFactory.class));

        Set<PersonName> personNames = new HashSet<PersonName>();

        PersonName personNamePreferred = createPreferredPersonName("John", "Dover");
        personNames.add(personNamePreferred);

        when(patient.getNames()).thenReturn(personNames);

        String formattedName = patientDomainWrapper.getFormattedName();

        assertThat(formattedName, is("Dover, John"));
    }


    @Test
    public void shouldReturnPersonNameWhenThereAreTwoNamesAndOneOfThemIsPreferred() {
        patient = mock(Patient.class);

        patientDomainWrapper = new PatientDomainWrapper(patient, emrApiProperties, mock(AdtService.class),
                visitService, mock(EncounterService.class), mock(DiagnosisService.class), mock(DomainWrapperFactory.class));

        Set<PersonName> personNames = new HashSet<PersonName>();

        PersonName personNamePreferred = createPreferredPersonName("mario", "neissi");
        personNames.add(personNamePreferred);

        PersonName personNameNonPreferred = createNonPreferredPersonName("Ana", "emerson");
        personNames.add(personNameNonPreferred);

        when(patient.getNames()).thenReturn(personNames);
        PersonName returnedName = patientDomainWrapper.getPersonName();

        assertSame(personNamePreferred, returnedName);

    }

    @Test
    public void shouldReturnPersonNameWhenThereAreTwoNamesAndNoneOfThemIsPreferred() {
        patient = mock(Patient.class);

        patientDomainWrapper = new PatientDomainWrapper(patient, emrApiProperties, mock(AdtService.class),
                visitService, mock(EncounterService.class), mock(DiagnosisService.class), mock(DomainWrapperFactory.class));

        Set<PersonName> personNames = new HashSet<PersonName>();

        PersonName personNamePreferred = createNonPreferredPersonName("mario", "neissi");
        personNames.add(personNamePreferred);

        PersonName personNameNonPreferred = createNonPreferredPersonName("Ana", "emerson");
        personNames.add(personNameNonPreferred);

        when(patient.getNames()).thenReturn(personNames);
        PersonName returnedName = patientDomainWrapper.getPersonName();

        assertNotNull(returnedName);

    }

    @Test
    public void shouldCalculateCorrectAgeInMonthsForDeceasedPatient() {
        patient.setDead(true);

        Calendar cal = Calendar.getInstance();
        cal.set(2012, 11, 4);
        patient.setBirthdate(cal.getTime());

        cal.set(2013, 2, 1);
        patient.setDeathDate(cal.getTime());

        assertThat(patientDomainWrapper.getAgeInMonths(), is(2));
    }

    @Test
    public void shouldCalculateCorrectAgeInDaysForDeceasedPatient() {
        patient.setDead(true);

        Calendar cal = Calendar.getInstance();
        cal.set(2013, 1, 26);
        patient.setBirthdate(cal.getTime());

        cal.set(2013, 2, 1);
        patient.setDeathDate(cal.getTime());

        assertThat(patientDomainWrapper.getAgeInDays(), is(3));
    }

    @Test
    public void shouldReturnExtraPatientIdentifiers() {

        PatientIdentifierType pit1 = new PatientIdentifierType();
        PatientIdentifierType pit2 = new PatientIdentifierType();
        PatientIdentifierType pit3 = new PatientIdentifierType();

        PatientIdentifier identifier1 = new PatientIdentifier();
        identifier1.setIdentifierType(pit1);
        PatientIdentifier identifier2 = new PatientIdentifier();
        identifier2.setIdentifierType(pit2);
        PatientIdentifier identifier3 = new PatientIdentifier();
        identifier3.setIdentifierType(pit3);

        patient.addIdentifier(identifier1);
        patient.addIdentifier(identifier2);
        patient.addIdentifier(identifier3);

        when(emrApiProperties.getExtraPatientIdentifierTypes()).thenReturn(Arrays.asList(pit1, pit2));

        List<PatientIdentifier> identifiers = patientDomainWrapper.getExtraIdentifiers();

        assertThat(identifiers.size(), is(2));
        assertThat(identifiers, hasItems(identifier1, identifier2));

    }

    @Test
    public void shouldReturnExtraPatientIdentifiersRestrictedByLocation() {

        Location parentLocation = new Location();
        Location childLocation1 = new Location();
        Location childLocation2 = new Location();

        parentLocation.addChildLocation(childLocation1);
        parentLocation.addChildLocation(childLocation2);

        PatientIdentifierType pit = new PatientIdentifierType();
        pit.setLocationBehavior(PatientIdentifierType.LocationBehavior.REQUIRED);

        PatientIdentifier identifier1 = new PatientIdentifier();
        identifier1.setIdentifierType(pit);
        identifier1.setLocation(parentLocation);

        PatientIdentifier identifier2 = new PatientIdentifier();
        identifier2.setIdentifierType(pit);
        identifier2.setLocation(childLocation1);

        PatientIdentifier identifier3 = new PatientIdentifier();
        identifier3.setIdentifierType(pit);
        identifier3.setLocation(childLocation2);

        patient.addIdentifier(identifier1);
        patient.addIdentifier(identifier2);
        patient.addIdentifier(identifier3);

        when(emrApiProperties.getExtraPatientIdentifierTypes()).thenReturn(Collections.singletonList(pit));

        List<PatientIdentifier> identifiers = patientDomainWrapper.getExtraIdentifiers(childLocation2);

        assertThat(identifiers.size(), is(2));
        assertThat(identifiers, hasItems(identifier1, identifier3));  // should not have the identifier at the other child locations

    }

    @Test
    public void shouldReturnExtraPatientIdentifiersShouldNotRestrictLocationsForTypesWhereLocationIsNotRequired() {

        Location parentLocation = new Location();
        Location childLocation1 = new Location();
        Location childLocation2 = new Location();

        parentLocation.addChildLocation(childLocation1);
        parentLocation.addChildLocation(childLocation2);

        PatientIdentifierType pit = new PatientIdentifierType();

        PatientIdentifier identifier1 = new PatientIdentifier();
        identifier1.setIdentifierType(pit);
        identifier1.setLocation(parentLocation);

        PatientIdentifier identifier2 = new PatientIdentifier();
        identifier2.setIdentifierType(pit);
        identifier2.setLocation(childLocation1);

        PatientIdentifier identifier3 = new PatientIdentifier();
        identifier3.setIdentifierType(pit);
        identifier3.setLocation(childLocation2);

        patient.addIdentifier(identifier1);
        patient.addIdentifier(identifier2);
        patient.addIdentifier(identifier3);

        when(emrApiProperties.getExtraPatientIdentifierTypes()).thenReturn(Collections.singletonList(pit));

        List<PatientIdentifier> identifiers = patientDomainWrapper.getExtraIdentifiers(childLocation2);

        assertThat(identifiers.size(), is(3));
        assertThat(identifiers, hasItems(identifier1, identifier2, identifier3));  // should not have the identifier at the other child locations

    }

    @Test
    public void shouldReturnExtraPatientIdentifiersMappedByType() {

        PatientIdentifierType pit1 = new PatientIdentifierType();
        PatientIdentifierType pit2 = new PatientIdentifierType();
        PatientIdentifierType pit3 = new PatientIdentifierType();

        PatientIdentifier identifier1 = new PatientIdentifier();
        identifier1.setId(1);
        identifier1.setIdentifier("1");
        identifier1.setIdentifierType(pit1);

        PatientIdentifier identifier2 = new PatientIdentifier();
        identifier2.setId(2);
        identifier2.setIdentifier("2");
        identifier2.setIdentifierType(pit2);

        PatientIdentifier identifier3 = new PatientIdentifier();
        identifier3.setId(3);
        identifier3.setIdentifier("3");
        identifier3.setIdentifierType(pit2);

        PatientIdentifier identifier4 = new PatientIdentifier();
        identifier4.setId(4);
        identifier4.setIdentifier("4");
        identifier4.setIdentifierType(pit3);

        patient.addIdentifier(identifier1);
        patient.addIdentifier(identifier2);
        patient.addIdentifier(identifier3);
        patient.addIdentifier(identifier4);

        when(emrApiProperties.getExtraPatientIdentifierTypes()).thenReturn(Arrays.asList(pit1, pit2));

        Map<PatientIdentifierType, List<PatientIdentifier>> identifierMap = patientDomainWrapper.getExtraIdentifiersMappedByType();

        assertThat(identifierMap.keySet().size(), is(2));
        assertTrue(identifierMap.containsKey(pit1));
        assertTrue(identifierMap.containsKey(pit2));

        assertThat(identifierMap.get(pit1).size(), is(1));
        assertThat(identifierMap.get(pit1), hasItem(identifier1));

        assertThat(identifierMap.get(pit2).size(), is(2));
        assertThat(identifierMap.get(pit2), hasItems(identifier2, identifier3));
    }


    private PersonName createPreferredPersonName(String givenName, String familyName) {
        PersonName personNamePreferred = createPersonName(givenName, familyName, true);
        return personNamePreferred;
    }

    private PersonName createNonPreferredPersonName(String givenName, String familyName) {
        PersonName personNameNonPreferred = createPersonName(givenName, familyName, false);
        return personNameNonPreferred;
    }

    private PersonName createPersonName(String givenName, String familyName, boolean preferred) {
        PersonName personNameNonPreferred = new PersonName();
        personNameNonPreferred.setGivenName(givenName);
        personNameNonPreferred.setFamilyName(familyName);
        personNameNonPreferred.setPreferred(preferred);
        return personNameNonPreferred;
    }


}
