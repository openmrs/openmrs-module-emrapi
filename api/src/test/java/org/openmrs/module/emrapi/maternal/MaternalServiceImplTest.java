package org.openmrs.module.emrapi.maternal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Relationship;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.VisitService;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.module.emrapi.EmrApiContextSensitiveTest;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.springframework.beans.factory.annotation.Autowired;

public class MaternalServiceImplTest extends EmrApiContextSensitiveTest {

    @Autowired
    private MaternalService maternalService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private PersonService personService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    private TestDataManager testDataManager;

    @Before
    public void setUp() throws Exception {
        executeDataSet("baseTestDataset.xml");
    }

    @Test
    public void shouldGetNewbornByMother() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(mother, null);

        assertThat(newborns.size(), equalTo(1));
        assertThat(newborns.get(0).getNewborn(), equalTo(baby));
        assertThat(newborns.get(0).getNewbornVisit(), equalTo(babyVisit));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfMotherDoesNotHaveActiveVisit() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(mother).started(oneHourAgo).stopped(now).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(mother, null);

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfNewbornDoesNotHaveActiveVisit() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby).started(now).stopped(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(mother, null);

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfNewbornNotLinkedByMotherChildRelationship() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(mother, null);

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfNewbornBornADayOrMoreBeforeVisit() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();
        Date oneDayAgo = new DateTime().minusDays(1).toDate();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(oneDayAgo).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(mother, null);

        assertThat(newborns.size(), equalTo(0));
    }

    // this test will fail when run *exactly* at midnight, on the second
    @Test
    public void shouldGetNewbornByMotherIfNewbornBornBeforeVisitStartButSameDay() throws Exception {
        Date now = new Date();
        Date oneSecondAgo = new DateTime().minusSeconds(1).toDate();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(oneSecondAgo).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(mother).started(now).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(mother, null);

        assertThat(newborns.size(),equalTo(1));
        assertThat(newborns.get(0).getNewborn(),equalTo(baby));
        assertThat(newborns.get(0).getNewbornVisit(),equalTo(babyVisit));
    }

    @Test
    public void shouldGetMultipleNewbornByMother() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby1 = testDataManager.randomPatient().birthdate(now).save();
        Patient baby2= testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship1 = new Relationship();
        motherChildRelationship1.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship1.setPersonA(mother);
        motherChildRelationship1.setPersonB(baby1);
        personService.saveRelationship(motherChildRelationship1);

        Relationship motherChildRelationship2 = new Relationship();
        motherChildRelationship2.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship2.setPersonA(mother);
        motherChildRelationship2.setPersonB(baby2);
        personService.saveRelationship(motherChildRelationship2);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(mother).started(oneHourAgo).save();
        Visit baby1Visit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby1).started(now).save();
        Visit baby2Visit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby2).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(mother, null);

        assertThat(newborns.size(), equalTo(2));
        List<Patient> babyList = newborns.stream().map(Newborn::getNewborn).collect(Collectors.toList());
        assertTrue(babyList.contains(baby1));
        assertTrue(babyList.contains(baby2));
        List<Visit> babyVisitList = newborns.stream().map(Newborn::getNewbornVisit).collect(Collectors.toList());
        assertTrue(babyVisitList.contains(baby1Visit));
        assertTrue(babyVisitList.contains(baby2Visit));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfMotherVoided() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        patientService.voidPatient(mother, "test");

        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(mother, null);

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfNewbornVoided() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();
        patientService.voidPatient(baby, "test");

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(mother, null);

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfRelationshipVoided() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);
        personService.voidRelationship(motherChildRelationship, "test");

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(mother, null);

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfMotherVisitVoided() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby).started(now).save();
        visitService.voidVisit(motherVisit, "test");

        List<Newborn> newborns = maternalService.getNewbornsByMother(mother, null);

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfBabyVisitVoided() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby).started(now).save();
        visitService.voidVisit(babyVisit, "test");

        List<Newborn> newborns = maternalService.getNewbornsByMother(mother, null);

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldGetNewbornByMotherIfVisitLocationsMatchQueriedVisitLocation() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();
        Location location = testDataManager.location().name("Test Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(mother).started(oneHourAgo).location(location).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby).started(now).location(location).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(mother, location);

        assertThat(newborns.size(), equalTo(1));
        assertThat(newborns.get(0).getNewborn(), equalTo(baby));
        assertThat(newborns.get(0).getNewbornVisit(), equalTo(babyVisit));
    }

    @Test
    public void shouldGetNewbornByMotherIfMotherVisitLocationDoesNotMatchQueriedVisitLocation() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();
        Location location = testDataManager.location().name("Test Location").save();
        Location anotherLocation = testDataManager.location().name("Another Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(mother).started(oneHourAgo).location(anotherLocation).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby).started(now).location(location).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(mother, location);

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldGetNewbornByMotherIfBabyVisitLocationDoesNotMatchQueriedVisitLocation() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();
        Location location = testDataManager.location().name("Test Location").save();
        Location anotherLocation = testDataManager.location().name("Another Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(mother).started(oneHourAgo).location(location).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).patient(baby).started(now).location(anotherLocation).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(mother, location);

        assertThat(newborns.size(), equalTo(0));
    }
}
