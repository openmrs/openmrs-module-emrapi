package org.openmrs.module.emrapi.maternal;

import static org.hamcrest.CoreMatchers.equalTo;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Relationship;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.VisitService;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.module.emrapi.EmrApiContextSensitiveTest;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
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
    private ConceptService conceptService;

    @Autowired
    private DispositionService dispositionService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    private TestDataManager testDataManager;

    @Before
    public void setUp() throws Exception {
        executeDataSet("baseTestDataset.xml");
        ContextSensitiveMetadataTestUtils.setupDispositionDescriptor(conceptService, dispositionService);
    }

    @Test
    public void shouldGetNewbornByMother() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(Collections.singletonList(mother));

        assertThat(newborns.size(), equalTo(1));
        assertThat(newborns.get(0).getNewborn(), equalTo(baby));
        assertThat(newborns.get(0).getMother(), equalTo(mother));
        assertNull(newborns.get(0).getNewbornAdmission());
    }

    @Test
    public void shouldNotGetNewbornByMotherIfMotherDoesNotHaveActiveVisit() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).stopped(now).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(Collections.singletonList(mother));

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfNewbornDoesNotHaveActiveVisit() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).stopped(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(Collections.singletonList(mother));

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfNewbornNotLinkedByMotherChildRelationship() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(Collections.singletonList(mother));

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfNewbornBornADayOrMoreBeforeVisit() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();
        Date oneDayAgo = new DateTime().minusDays(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(oneDayAgo).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(Collections.singletonList(mother));

        assertThat(newborns.size(), equalTo(0));
    }

    // this test will fail when run *exactly* at midnight, on the second
    @Test
    public void shouldGetNewbornByMotherIfNewbornBornBeforeVisitStartButSameDay() throws Exception {
        Date now = new Date();
        Date oneSecondAgo = new DateTime().minusSeconds(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(oneSecondAgo).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(now).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(Collections.singletonList(mother));

        assertThat(newborns.size(),equalTo(1));
        assertThat(newborns.get(0).getNewborn(),equalTo(baby));
        assertThat(newborns.get(0).getMother(), equalTo(mother));
        assertNull(newborns.get(0).getNewbornAdmission());
    }

    @Test
    public void shouldGetMultipleNewbornByMother() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

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

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit baby1Visit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby1).started(now).save();
        Visit baby2Visit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby2).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(Collections.singletonList(mother));

        assertThat(newborns.size(), equalTo(2));
        List<Patient> babyList = newborns.stream().map(Newborn::getNewborn).collect(Collectors.toList());
        assertTrue(babyList.contains(baby1));
        assertTrue(babyList.contains(baby2));

        assertThat(newborns.get(0).getMother(), equalTo(mother));
        assertThat(newborns.get(1).getMother(), equalTo(mother));

        assertNull(newborns.get(0).getNewbornAdmission());
        assertNull(newborns.get(1).getNewbornAdmission());
    }

    @Test
    public void shouldNotGetNewbornByMotherIfMotherVoided() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        patientService.voidPatient(mother, "test");

        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(Collections.singletonList(mother));

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfNewbornVoided() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();
        patientService.voidPatient(baby, "test");

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(Collections.singletonList(mother));

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfRelationshipVoided() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);
        personService.voidRelationship(motherChildRelationship, "test");

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(Collections.singletonList(mother));

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfMotherVisitVoided() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();
        visitService.voidVisit(motherVisit, "test");

        List<Newborn> newborns = maternalService.getNewbornsByMother(Collections.singletonList(mother));

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfBabyVisitVoided() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();
        visitService.voidVisit(babyVisit, "test");

        List<Newborn> newborns = maternalService.getNewbornsByMother(Collections.singletonList(mother));

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetNewbornByMotherIfVisitLocationsDontMatch() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();
        Location otherVisitLocation = testDataManager.location().name("Other Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(otherVisitLocation).patient(baby).started(now).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(Collections.singletonList(mother));

        assertThat(newborns.size(), equalTo(0));
    }

    @Test
    public void shouldGetNewbornsForMultipleMothers() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        Patient otherMother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient otherBaby = testDataManager.randomPatient().birthdate(now).save();

        Relationship otherMotherChildRelationship = new Relationship();
        otherMotherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        otherMotherChildRelationship.setPersonA(otherMother);
        otherMotherChildRelationship.setPersonB(otherBaby);
        personService.saveRelationship(otherMotherChildRelationship);

        Visit otherMotherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(otherMother).started(oneHourAgo).save();
        Visit otherBabyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(otherBaby).started(now).save();


        List<Newborn> newborns = maternalService.getNewbornsByMother(Arrays.asList(mother, otherMother));

        assertThat(newborns.size(), equalTo(2));
        List<Patient> babyList = newborns.stream().map(Newborn::getNewborn).collect(Collectors.toList());
        assertTrue(babyList.contains(baby));
        assertTrue(babyList.contains(otherBaby));

        List<Patient> motherList = newborns.stream().map(Newborn::getMother).collect(Collectors.toList());
        assertTrue(motherList.contains(mother));
        assertTrue(motherList.contains(otherMother));
    }

    @Test
    public void shouldGetNewbornByMotherWithInpatientAdmission() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        // admit the baby
        Encounter admission = testDataManager.encounter().encounterType(emrApiProperties.getAdmissionEncounterType()).encounterDatetime(now).patient(baby).save();

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).encounter(admission).save();

        List<Newborn> newborns = maternalService.getNewbornsByMother(Collections.singletonList(mother));

        assertThat(newborns.size(), equalTo(1));
        assertThat(newborns.get(0).getNewbornAdmission().getVisit(), equalTo(babyVisit));
        assertThat(newborns.get(0).getNewbornAdmission().getFirstAdmissionOrTransferEncounter(), equalTo(admission));
    }

    @Test
    public void shouldGetMotherByNewborn() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        List<Mother> mothers = maternalService.getMothersByNewborn(Collections.singletonList(baby));

        assertThat(mothers.size(), equalTo(1));
        assertThat(mothers.get(0).getNewborn(), equalTo(baby));
        assertThat(mothers.get(0).getMother(), equalTo(mother));
        assertNull(mothers.get(0).getMotherAdmission());
    }

    @Test
    public void shouldNotGetMotherByNewbornIfMotherDoesNotHaveActiveVisit() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).stopped(now).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        List<Mother> mothers = maternalService.getMothersByNewborn(Collections.singletonList(baby));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByNewbornIfNewbornDoesNotHaveActiveVisit() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).stopped(now).save();

        List<Mother> mothers = maternalService.getMothersByNewborn(Collections.singletonList(baby));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByNewbornIfNewbornNotLinkedByMotherChildRelationship() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        List<Mother> mothers = maternalService.getMothersByNewborn(Collections.singletonList(baby));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByNewbornIfMotherVoided() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        patientService.voidPatient(mother, "test");

        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        List<Mother> mothers = maternalService.getMothersByNewborn(Collections.singletonList(baby));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByNewbornIfNewbornVoided() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();
        patientService.voidPatient(baby, "test");

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        List<Mother> mothers = maternalService.getMothersByNewborn(Collections.singletonList(baby));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByNewbornIfRelationshipVoided() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);
        personService.voidRelationship(motherChildRelationship, "test");

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        List<Mother> mothers = maternalService.getMothersByNewborn(Collections.singletonList(baby));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByNewbornIfMotherVisitVoided() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();
        visitService.voidVisit(motherVisit, "test");

        List<Mother> mothers = maternalService.getMothersByNewborn(Collections.singletonList(baby));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByNewbornIfBabyVisitVoided() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();
        visitService.voidVisit(babyVisit, "test");

        List<Mother> mothers = maternalService.getMothersByNewborn(Collections.singletonList(baby));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByNewbornIfVisitLocationsDontMatch() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();
        Location otherVisitLocation = testDataManager.location().name("Other Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(otherVisitLocation).patient(baby).started(now).save();

        List<Mother> mothers = maternalService.getMothersByNewborn(Collections.singletonList(baby));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldGetMothersForMultipleNewborns() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        Patient otherMother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient otherBaby = testDataManager.randomPatient().birthdate(now).save();

        Relationship otherMotherChildRelationship = new Relationship();
        otherMotherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        otherMotherChildRelationship.setPersonA(otherMother);
        otherMotherChildRelationship.setPersonB(otherBaby);
        personService.saveRelationship(otherMotherChildRelationship);

        Visit otherMotherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(otherMother).started(oneHourAgo).save();
        Visit otherBabyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(otherBaby).started(now).save();


        List<Mother> mothers = maternalService.getMothersByNewborn(Arrays.asList(baby, otherBaby));

        assertThat(mothers.size(), equalTo(2));
        List<Patient> babyList = mothers.stream().map(Mother::getNewborn).collect(Collectors.toList());
        assertTrue(babyList.contains(baby));
        assertTrue(babyList.contains(otherBaby));

        List<Patient> motherList = mothers.stream().map(Mother::getMother).collect(Collectors.toList());
        assertTrue(motherList.contains(mother));
        assertTrue(motherList.contains(otherMother));
    }

    @Test
    public void shouldGetMotherByNewbornWithInpatientAdmission() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient baby = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(baby);
        personService.saveRelationship(motherChildRelationship);

        // admit the mother
        Encounter admission = testDataManager.encounter().encounterType(emrApiProperties.getAdmissionEncounterType()).encounterDatetime(now).patient(mother).save();

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).encounter(admission).save();
        Visit babyVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(baby).started(now).save();

        List<Mother> mothers = maternalService.getMothersByNewborn(Collections.singletonList(baby));

        assertThat(mothers.size(), equalTo(1));
        assertThat(mothers.get(0).getMotherAdmission().getVisit(), equalTo(motherVisit));
        assertThat(mothers.get(0).getMotherAdmission().getFirstAdmissionOrTransferEncounter(), equalTo(admission));
    }

}
