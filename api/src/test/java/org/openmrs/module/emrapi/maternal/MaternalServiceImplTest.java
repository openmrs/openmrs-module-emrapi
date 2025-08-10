package org.openmrs.module.emrapi.maternal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class MaternalServiceImplTest extends BaseModuleContextSensitiveTest {

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


    @BeforeEach
    public void setUp() {
        executeDataSet("baseTestDataset.xml");
        dispositionService.setDispositionConfig("testDispositionConfig.json"); // use demo disposition config from test resources
        ContextSensitiveMetadataTestUtils.setupDispositionDescriptor(conceptService, dispositionService);
        ContextSensitiveMetadataTestUtils.setupAdmissionDecisionConcept(conceptService, emrApiProperties);
    }


    @Test
    public void shouldGetChild() {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(null, null, false, false, false));

        assertThat(motherAndChildList.size(), equalTo(1));
        assertThat(motherAndChildList.get(0).getChild(), equalTo(child));
        assertThat(motherAndChildList.get(0).getMother(), equalTo(mother));
        assertNull(motherAndChildList.get(0).getChildAdmission());
    }
    @Test
    public void shouldGetChildByMother() {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Collections.singletonList(mother.getUuid()), null, false, false, false));

        assertThat(motherAndChildList.size(), equalTo(1));
        assertThat(motherAndChildList.get(0).getChild(), equalTo(child));
        assertThat(motherAndChildList.get(0).getMother(), equalTo(mother));
        assertNull(motherAndChildList.get(0).getChildAdmission());
    }

    @Test
    public void shouldNotGetChildByMotherIfMotherDoesNotHaveActiveVisitAndMotherHasActiveVisitSetTrue() {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).stopped(now).save();
        Visit childVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(child).started(now).save();

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Collections.singletonList(mother.getUuid()), null, true, false, false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetChildByMotherIfChildDoesNotHaveActiveVisitAndChildHasActiveVisitSetTrue() {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit childVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(child).started(now).stopped(now).save();

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Collections.singletonList(mother.getUuid()), null, false, true, false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetChildByMotherIfChildNotLinkedByMotherChildRelationship() {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Collections.singletonList(mother.getUuid()), null, false, false, false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetChildByMotherIfChildBornADayOrMoreBeforeVisitAndChildBornDuringMothersActiveVisitSetTrue() {
        Date now = new Date();
        Date oneDayAgo = new DateTime().minusDays(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(oneDayAgo).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(now).save();

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Collections.singletonList(mother.getUuid()), null, false, false, true));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    // this test will fail when run *exactly* at midnight, on the second
    @Test
    public void shouldGetChildByMotherIfChildBornBeforeVisitStartButSameDayAndChildBornDuringMothersActiveVisitSetTrue() {
        Date now = new Date();
        Date oneSecondAgo = new DateTime().minusSeconds(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(oneSecondAgo).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(now).save();

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Collections.singletonList(mother.getUuid()), null, false, false, true));

        assertThat(motherAndChildList.size(),equalTo(1));
        assertThat(motherAndChildList.get(0).getChild(),equalTo(child));
        assertThat(motherAndChildList.get(0).getMother(), equalTo(mother));
        assertNull(motherAndChildList.get(0).getChildAdmission());
    }
    
    @Test
    public void shouldGetChildByMotherIfChildBornInNextCalendarYearAfterVisitStart() {
        DateTime lastDayOfYear = new DateTime(1980, 12, 31, 0, 0);
        Date oneDayLater = lastDayOfYear.plusDays(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(oneDayLater).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(lastDayOfYear.toDate()).save();

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Collections.singletonList(mother.getUuid()), null, false, false, true));

        assertThat(motherAndChildList.size(),equalTo(1));
        assertThat(motherAndChildList.get(0).getChild(),equalTo(child));
        assertThat(motherAndChildList.get(0).getMother(), equalTo(mother));
        assertNull(motherAndChildList.get(0).getChildAdmission());
    }

    @Test
    public void shouldGetMultipleChildByMother() {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child1 = testDataManager.randomPatient().birthdate(now).save();
        Patient child2= testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship1 = new Relationship();
        motherChildRelationship1.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship1.setPersonA(mother);
        motherChildRelationship1.setPersonB(child1);
        personService.saveRelationship(motherChildRelationship1);

        Relationship motherChildRelationship2 = new Relationship();
        motherChildRelationship2.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship2.setPersonA(mother);
        motherChildRelationship2.setPersonB(child2);
        personService.saveRelationship(motherChildRelationship2);

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Collections.singletonList(mother.getUuid()), null, false, false, false));

        assertThat(motherAndChildList.size(), equalTo(2));
        List<Patient> childList = motherAndChildList.stream().map(MotherAndChild::getChild).collect(Collectors.toList());
        assertTrue(childList.contains(child1));
        assertTrue(childList.contains(child2));

        assertThat(motherAndChildList.get(0).getMother(), equalTo(mother));
        assertThat(motherAndChildList.get(1).getMother(), equalTo(mother));

        assertNull(motherAndChildList.get(0).getChildAdmission());
        assertNull(motherAndChildList.get(1).getChildAdmission());
    }

    @Test
    public void shouldNotGetChildByMotherIfMotherVoided() {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        patientService.voidPatient(mother, "test");

        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Collections.singletonList(mother.getUuid()), null, false, false, false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetChildByMotherIfChildVoided() {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();
        patientService.voidPatient(child, "test");

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Collections.singletonList(mother.getUuid()), null, false, false, false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetChildByMotherIfRelationshipVoided() {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);
        personService.voidRelationship(motherChildRelationship, "test");

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Collections.singletonList(mother.getUuid()), null, false, false, false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetChildByMotherIfMotherVisitVoidedAndMotherHasActiveVisitSetTrue() {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        visitService.voidVisit(motherVisit, "test");

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Collections.singletonList(mother.getUuid()), null, true, false, false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetChildByMotherIfChildVisitVoidedAndChildHasActiveVisitSetTrue() {
        Date now = new Date();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Visit childVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(child).started(now).save();
        visitService.voidVisit(childVisit, "test");

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Collections.singletonList(mother.getUuid()), null, false, true, false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldGetChildrenForMultipleMothers() {
        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate("2000-01-01").save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Patient otherMother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient otherChild = testDataManager.randomPatient().birthdate("2000-01-01").save();

        Relationship otherMotherChildRelationship = new Relationship();
        otherMotherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        otherMotherChildRelationship.setPersonA(otherMother);
        otherMotherChildRelationship.setPersonB(otherChild);
        personService.saveRelationship(otherMotherChildRelationship);

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Arrays.asList(mother.getUuid(),otherMother.getUuid()), null, false, false, false));

        assertThat(motherAndChildList.size(), equalTo(2));
        List<Patient> childList = motherAndChildList.stream().map(MotherAndChild::getChild).collect(Collectors.toList());
        assertTrue(childList.contains(child));
        assertTrue(childList.contains(otherChild));

        List<Patient> motherList = motherAndChildList.stream().map(MotherAndChild::getMother).collect(Collectors.toList());
        assertTrue(motherList.contains(mother));
        assertTrue(motherList.contains(otherMother));
    }

    @Test
    public void shouldGetChildrenForMultipleMothersMatchingByActiveVisitsAndChildBornDuringActiveVisit() {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit childVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(child).started(now).save();

        Patient otherMother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient otherChild = testDataManager.randomPatient().birthdate(now).save();

        Relationship otherMotherChildRelationship = new Relationship();
        otherMotherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        otherMotherChildRelationship.setPersonA(otherMother);
        otherMotherChildRelationship.setPersonB(otherChild);
        personService.saveRelationship(otherMotherChildRelationship);

        Visit otherMotherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(otherMother).started(oneHourAgo).save();
        Visit otherChildVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(otherChild).started(now).save();


        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Arrays.asList(mother.getUuid(),otherMother.getUuid()), null, true, true, true));

        assertThat(motherAndChildList.size(), equalTo(2));
        List<Patient> childList = motherAndChildList.stream().map(MotherAndChild::getChild).collect(Collectors.toList());
        assertTrue(childList.contains(child));
        assertTrue(childList.contains(otherChild));

        List<Patient> motherList = motherAndChildList.stream().map(MotherAndChild::getMother).collect(Collectors.toList());
        assertTrue(motherList.contains(mother));
        assertTrue(motherList.contains(otherMother));
    }

    @Test
    public void shouldGetChildByMotherWithInpatientAdmission() {
        Date now = new Date();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        // admit the child
        Encounter admission = testDataManager.encounter().encounterType(emrApiProperties.getAdmissionEncounterType()).encounterDatetime(now).patient(child).save();
        Visit childVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(child).started(now).encounter(admission).save();

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Collections.singletonList(mother.getUuid()), null, false, false, false));

        assertThat(motherAndChildList.size(), equalTo(1));
        assertThat(motherAndChildList.get(0).getChildAdmission().getVisit(), equalTo(childVisit));
        assertThat(motherAndChildList.get(0).getChildAdmission().getFirstAdmissionOrTransferEncounter(), equalTo(admission));
    }

    @Test
    public void getChildByMotherDoesNotFailWhenChildHaveTwoActiveVisits() {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit childVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(child).started(now).save();
        Visit anotherChildVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(child).started(now).save();

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(Collections.singletonList(mother.getUuid()), null, true, true, true));

        assertThat(motherAndChildList.size(), equalTo(1));
    }

    @Test
    public void shouldGetMother() {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(null, null, false, false, false));

        assertThat(motherAndChildList.size(), equalTo(1));
        assertThat(motherAndChildList.get(0).getChild(), equalTo(child));
        assertThat(motherAndChildList.get(0).getMother(), equalTo(mother));
        assertNull(motherAndChildList.get(0).getMotherAdmission());
    }

    @Test
    public void shouldGetMotherByChild() {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(null, Collections.singletonList(child.getUuid()), false, false, false));

        assertThat(motherAndChildList.size(), equalTo(1));
        assertThat(motherAndChildList.get(0).getChild(), equalTo(child));
        assertThat(motherAndChildList.get(0).getMother(), equalTo(mother));
        assertNull(motherAndChildList.get(0).getMotherAdmission());
    }

    @Test
    public void shouldNotGetMotherByChildIfMotherDoesNotHaveActiveVisitAndMotherHasActiveVisitSetTrue() {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).stopped(now).save();

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(null, Collections.singletonList(child.getUuid()), true, false, false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByChildIfChildDoesNotHaveActiveVisitAndChildHasActiveVisitSetTrue() {
        Date now = new Date();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Visit childVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(child).started(now).stopped(now).save();

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(null, Collections.singletonList(child.getUuid()), false, true,false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByChildIfChildNotLinkedByMotherChildRelationship() {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(null, Collections.singletonList(child.getUuid()), false, false, false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByChildIfMotherVoided() {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        patientService.voidPatient(mother, "test");

        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(null, Collections.singletonList(child.getUuid()), false, false, false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByChildIfChildVoided() {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();
        patientService.voidPatient(child, "test");

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(null, Collections.singletonList(child.getUuid()), false, false, false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByChildIfRelationshipVoided() {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);
        personService.voidRelationship(motherChildRelationship, "test");

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(null, Collections.singletonList(child.getUuid()), false, false,false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByChildIfMotherVisitVoidedAndMotherHasActiveVisitSetTrue() {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        visitService.voidVisit(motherVisit, "test");

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(null, Collections.singletonList(child.getUuid()), true, false, false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByChildIfChildVisitVoidedAndChildHasActiveVisitSetTrue() {
        Date now = new Date();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Visit childVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(child).started(now).save();
        visitService.voidVisit(childVisit, "test");

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(null, Collections.singletonList(child.getUuid()), false, true, false));

        assertThat(motherAndChildList.size(), equalTo(0));
    }

    @Test
    public void shouldGetMothersForMultipleChildren() {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Patient otherMother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient otherChild = testDataManager.randomPatient().birthdate(now).save();

        Relationship otherMotherChildRelationship = new Relationship();
        otherMotherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        otherMotherChildRelationship.setPersonA(otherMother);
        otherMotherChildRelationship.setPersonB(otherChild);
        personService.saveRelationship(otherMotherChildRelationship);

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(null, Arrays.asList(child.getUuid(),otherChild.getUuid()), false, false, false));

        assertThat(motherAndChildList.size(), equalTo(2));
        List<Patient> childList = motherAndChildList.stream().map(MotherAndChild::getChild).collect(Collectors.toList());
        assertTrue(childList.contains(child));
        assertTrue(childList.contains(otherChild));

        List<Patient> motherList = motherAndChildList.stream().map(MotherAndChild::getMother).collect(Collectors.toList());
        assertTrue(motherList.contains(mother));
        assertTrue(motherList.contains(otherMother));
    }

    @Test
    public void shouldGetMotherByChildWithInpatientAdmission() {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        // admit the mother
        Encounter admission = testDataManager.encounter().encounterType(emrApiProperties.getAdmissionEncounterType()).encounterDatetime(now).patient(mother).save();

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).encounter(admission).save();

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(null, Collections.singletonList(child.getUuid()), false, false, false));

        assertThat(motherAndChildList.size(), equalTo(1));
        assertThat(motherAndChildList.get(0).getMotherAdmission().getVisit(), equalTo(motherVisit));
        assertThat(motherAndChildList.get(0).getMotherAdmission().getFirstAdmissionOrTransferEncounter(), equalTo(admission));
    }

    @Test
    public void shouldOnlyGetMothersWithChildrenBornDuringActiveVisit() {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Patient otherMother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient otherChild = testDataManager.randomPatient().birthdate("2020-01-01").save();  // not during active visit

        Relationship otherMotherChildRelationship = new Relationship();
        otherMotherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        otherMotherChildRelationship.setPersonA(otherMother);
        otherMotherChildRelationship.setPersonB(otherChild);
        personService.saveRelationship(otherMotherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();
        Visit otherMotherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(otherMother).started(oneHourAgo).save();

        List<MotherAndChild> motherAndChildList = maternalService.getMothersAndChildren(new MothersAndChildrenSearchCriteria(null, null, false, false, true));

        assertThat(motherAndChildList.size(), equalTo(1));
        assertThat(motherAndChildList.get(0).getChild(), equalTo(child));
        assertThat(motherAndChildList.get(0).getMother(), equalTo(mother));
        assertNull(motherAndChildList.get(0).getMotherAdmission());
    }

}
