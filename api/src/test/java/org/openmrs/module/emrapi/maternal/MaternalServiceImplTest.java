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
        ContextSensitiveMetadataTestUtils.setupAdmissionDecisionConcept(conceptService, emrApiProperties);
    }

    @Test
    public void shouldGetChildByMother() throws Exception {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Collections.singletonList(mother.getUuid()), false, false, false));

        assertThat(children.size(), equalTo(1));
        assertThat(children.get(0).getChild(), equalTo(child));
        assertThat(children.get(0).getMother(), equalTo(mother));
        assertNull(children.get(0).getChildAdmission());
    }

    @Test
    public void shouldNotGetChildByMotherIfMotherDoesNotHaveActiveVisitAndMotherHasActiveVisitSetTrue() throws Exception {
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

        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Collections.singletonList(mother.getUuid()), true, false, false));

        assertThat(children.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetChildByMotherIfChildDoesNotHaveActiveVisitAndChildHasActiveVisitSetTrue() throws Exception {
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

        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Collections.singletonList(mother.getUuid()), false, true, false));

        assertThat(children.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetChildByMotherIfChildNotLinkedByMotherChildRelationship() throws Exception {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Collections.singletonList(mother.getUuid()), false, false, false));

        assertThat(children.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetChildByMotherIfChildBornADayOrMoreBeforeVisitAndChildBornDuringMothersActiveVisitSetTrue() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();
        Date oneDayAgo = new DateTime().minusDays(1).toDate();

        Location visitLocation = testDataManager.location().name("Visit Location").save();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(oneDayAgo).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        Visit motherVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(mother).started(oneHourAgo).save();

        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Collections.singletonList(mother.getUuid()), false, false, true));

        assertThat(children.size(), equalTo(0));
    }

    // this test will fail when run *exactly* at midnight, on the second
    @Test
    public void shouldGetChildByMotherIfChildBornBeforeVisitStartButSameDayAndChildBornDuringMothersActiveVisitSetTrue() throws Exception {
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

        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Collections.singletonList(mother.getUuid()), false, false, true));

        assertThat(children.size(),equalTo(1));
        assertThat(children.get(0).getChild(),equalTo(child));
        assertThat(children.get(0).getMother(), equalTo(mother));
        assertNull(children.get(0).getChildAdmission());
    }

    @Test
    public void shouldGetMultipleChildByMother() throws Exception {
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

        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Collections.singletonList(mother.getUuid()), false, false, false));

        assertThat(children.size(), equalTo(2));
        List<Patient> childList = children.stream().map(Child::getChild).collect(Collectors.toList());
        assertTrue(childList.contains(child1));
        assertTrue(childList.contains(child2));

        assertThat(children.get(0).getMother(), equalTo(mother));
        assertThat(children.get(1).getMother(), equalTo(mother));

        assertNull(children.get(0).getChildAdmission());
        assertNull(children.get(1).getChildAdmission());
    }

    @Test
    public void shouldNotGetChildByMotherIfMotherVoided() throws Exception {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        patientService.voidPatient(mother, "test");

        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Collections.singletonList(mother.getUuid()), false, false, false));

        assertThat(children.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetChildByMotherIfChildVoided() throws Exception {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();
        patientService.voidPatient(child, "test");

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Collections.singletonList(mother.getUuid()), false, false, false));

        assertThat(children.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetChildByMotherIfRelationshipVoided() throws Exception {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);
        personService.voidRelationship(motherChildRelationship, "test");

        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Collections.singletonList(mother.getUuid()), false, false, false));

        assertThat(children.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetChildByMotherIfMotherVisitVoidedAndMotherHasActiveVisitSetTrue() throws Exception {
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

        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Collections.singletonList(mother.getUuid()), true, false, false));

        assertThat(children.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetChildByMotherIfChildVisitVoidedAndChildHasActiveVisitSetTrue() throws Exception {
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

        Visit childVisit = testDataManager.visit().visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).patient(child).started(now).save();
        visitService.voidVisit(childVisit, "test");

        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Collections.singletonList(mother.getUuid()), false, true, false));

        assertThat(children.size(), equalTo(0));
    }

    @Test
    public void shouldGetChildsForMultipleMothers() throws Exception {
        Date now = new Date();
        Date oneHourAgo = new DateTime().minusHours(1).toDate();

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

        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Arrays.asList(mother.getUuid(),otherMother.getUuid()), false, false, false));

        assertThat(children.size(), equalTo(2));
        List<Patient> childList = children.stream().map(Child::getChild).collect(Collectors.toList());
        assertTrue(childList.contains(child));
        assertTrue(childList.contains(otherChild));

        List<Patient> motherList = children.stream().map(Child::getMother).collect(Collectors.toList());
        assertTrue(motherList.contains(mother));
        assertTrue(motherList.contains(otherMother));
    }

    @Test
    public void shouldGetChildsForMultipleMothersMatchingByActiveVisitsAndChildBornDuringActiveVisit() throws Exception {
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


        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Arrays.asList(mother.getUuid(),otherMother.getUuid()), true, true, true));

        assertThat(children.size(), equalTo(2));
        List<Patient> childList = children.stream().map(Child::getChild).collect(Collectors.toList());
        assertTrue(childList.contains(child));
        assertTrue(childList.contains(otherChild));

        List<Patient> motherList = children.stream().map(Child::getMother).collect(Collectors.toList());
        assertTrue(motherList.contains(mother));
        assertTrue(motherList.contains(otherMother));
    }

    @Test
    public void shouldGetChildByMotherWithInpatientAdmission() throws Exception {
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

        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Collections.singletonList(mother.getUuid()), false, false, false));

        assertThat(children.size(), equalTo(1));
        assertThat(children.get(0).getChildAdmission().getVisit(), equalTo(childVisit));
        assertThat(children.get(0).getChildAdmission().getFirstAdmissionOrTransferEncounter(), equalTo(admission));
    }

    @Test
    public void getChildByMotherDoesNotFailWhenChildHaveTwoActiveVisits() throws Exception {
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

        List<Child> children = maternalService.getChildrenByMother(new ChildSearchCriteria(Collections.singletonList(mother.getUuid()), true, true, true));

        assertThat(children.size(), equalTo(1));
    }


    @Test
    public void shouldGetMotherByChild() throws Exception {
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

        List<Mother> mothers = maternalService.getMothersByChild(new MotherSearchCriteria(Collections.singletonList(child.getUuid()), false, false));

        assertThat(mothers.size(), equalTo(1));
        assertThat(mothers.get(0).getChild(), equalTo(child));
        assertThat(mothers.get(0).getMother(), equalTo(mother));
        assertNull(mothers.get(0).getMotherAdmission());
    }

    @Test
    public void shouldNotGetMotherByChildIfMotherDoesNotHaveActiveVisitAndMotherHasActiveVisitSetTrue() throws Exception {
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

        List<Mother> mothers = maternalService.getMothersByChild(new MotherSearchCriteria(Collections.singletonList(child.getUuid()), true, false));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByChildIfChildDoesNotHaveActiveVisitAndChildHasActiveVisitSetTrue() throws Exception {
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

        List<Mother> mothers = maternalService.getMothersByChild(new MotherSearchCriteria(Collections.singletonList(child.getUuid()), false, true));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByChildIfChildNotLinkedByMotherChildRelationship() throws Exception {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        List<Mother> mothers = maternalService.getMothersByChild(new MotherSearchCriteria(Collections.singletonList(child.getUuid()), false, false));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByChildIfMotherVoided() throws Exception {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        patientService.voidPatient(mother, "test");

        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        List<Mother> mothers = maternalService.getMothersByChild(new MotherSearchCriteria(Collections.singletonList(child.getUuid()), false, false));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByChildIfChildVoided() throws Exception {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();
        patientService.voidPatient(child, "test");

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);

        List<Mother> mothers = maternalService.getMothersByChild(new MotherSearchCriteria(Collections.singletonList(child.getUuid()), false, false));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByChildIfRelationshipVoided() throws Exception {
        Date now = new Date();

        Patient mother = testDataManager.randomPatient().birthdate("1980-01-01").gender("F").save();
        Patient child = testDataManager.randomPatient().birthdate(now).save();

        Relationship motherChildRelationship = new Relationship();
        motherChildRelationship.setRelationshipType(emrApiProperties.getMotherChildRelationshipType());
        motherChildRelationship.setPersonA(mother);
        motherChildRelationship.setPersonB(child);
        personService.saveRelationship(motherChildRelationship);
        personService.voidRelationship(motherChildRelationship, "test");

        List<Mother> mothers = maternalService.getMothersByChild(new MotherSearchCriteria(Collections.singletonList(child.getUuid()), false, false));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByChildIfMotherVisitVoidedAndMotherHasActiveVisitSetTrue() throws Exception {
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

        List<Mother> mothers = maternalService.getMothersByChild(new MotherSearchCriteria(Collections.singletonList(child.getUuid()), true, false));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldNotGetMotherByChildIfChildVisitVoidedAndChildHasActiveVisitSetTrue() throws Exception {
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

        List<Mother> mothers = maternalService.getMothersByChild(new MotherSearchCriteria(Collections.singletonList(child.getUuid()), false, true));

        assertThat(mothers.size(), equalTo(0));
    }

    @Test
    public void shouldGetMothersForMultipleChildren() throws Exception {
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

        List<Mother> mothers = maternalService.getMothersByChild(new MotherSearchCriteria(Arrays.asList(child.getUuid(),otherChild.getUuid()), false, false));

        assertThat(mothers.size(), equalTo(2));
        List<Patient> childList = mothers.stream().map(Mother::getChild).collect(Collectors.toList());
        assertTrue(childList.contains(child));
        assertTrue(childList.contains(otherChild));

        List<Patient> motherList = mothers.stream().map(Mother::getMother).collect(Collectors.toList());
        assertTrue(motherList.contains(mother));
        assertTrue(motherList.contains(otherMother));
    }

    @Test
    public void shouldGetMotherByChildWithInpatientAdmission() throws Exception {
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

        List<Mother> mothers = maternalService.getMothersByChild(new MotherSearchCriteria(Collections.singletonList(child.getUuid()), false, false));

        assertThat(mothers.size(), equalTo(1));
        assertThat(mothers.get(0).getMotherAdmission().getVisit(), equalTo(motherVisit));
        assertThat(mothers.get(0).getMotherAdmission().getFirstAdmissionOrTransferEncounter(), equalTo(admission));
    }

}
