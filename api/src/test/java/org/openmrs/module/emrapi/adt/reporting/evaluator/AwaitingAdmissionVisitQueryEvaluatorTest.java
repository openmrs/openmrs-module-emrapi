package org.openmrs.module.emrapi.adt.reporting.evaluator;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.reporting.query.AwaitingAdmissionVisitQuery;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.context.VisitEvaluationContext;
import org.openmrs.module.reporting.query.visit.VisitIdSet;
import org.openmrs.module.reporting.query.visit.VisitQueryResult;
import org.openmrs.module.reporting.query.visit.service.VisitQueryService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class AwaitingAdmissionVisitQueryEvaluatorTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EmrConceptService emrConceptService;

    @Autowired
    private DispositionService dispositionService;

    @Autowired
    private VisitQueryService visitQueryService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    TestDataManager testDataManager;

    private DispositionDescriptor dispositionDescriptor;

    private AwaitingAdmissionVisitQuery query;

    private Patient patient;

    private Concept admitToHospital;

    @Before
    public void setup() throws Exception {
        executeDataSet("baseTestDataset.xml");
        dispositionDescriptor = ContextSensitiveMetadataTestUtils.setupDispositionDescriptor(conceptService, dispositionService);
        ContextSensitiveMetadataTestUtils.setupAdmissionDecisionConcept(conceptService, emrApiProperties);
        query = new AwaitingAdmissionVisitQuery();
        admitToHospital = emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital");
        patient = testDataManager.randomPatient().birthdate("2010-01-01").save();
    }

    @Test
    public void shouldFindVisitAwaitingAdmission() throws Exception {

        // a visit with a single visit note encounter with dispo = ADMIT
        Date now = new Date();
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(now)
                .save();
        Encounter encounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(now)
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        Obs obs = testDataManager.obs()
                .person(patient)
                .encounter(encounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));

    }

    @Test
    public void shouldNotCountDispositionOnVoidedEncounter() throws Exception {

        // a visit with a single *voided* visit note encounter with dispo = ADMIT
        Date now = new Date();
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(now)
                .save();
        Encounter encounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(now)
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .voided(true)
                .dateVoided(new Date())
                .voidReason("test")
                .save();
        Obs obs = testDataManager.obs()
                .person(patient)
                .encounter(encounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));

    }

    @Test
    public void shouldNotFindVisitIfPatientAdmitted() throws Exception {

        Date visitDatetime = new DateTime(2014,2,2,9,0,0).toDate();
        Date visitNoteDatetime = new DateTime(2014,2,2,10,0,0).toDate();
        Date admitDatetime = new DateTime(2014,2,2,11,0,0).toDate();

        // a visit with a visit note encounter with dispo = ADMIT and an admission encounter
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(visitDatetime)
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(visitNoteDatetime)
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();
        Encounter admissionEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(admitDatetime)
                .encounterType(emrApiProperties.getAdmissionEncounterType())
                .visit(visit)
                .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldNotConsiderVoidedAdmissionEncounter() throws Exception {

        Date visitDatetime = new DateTime(2014,2,2,9,0,0).toDate();
        Date visitNoteDatetime = new DateTime(2014,2,2,10,0,0).toDate();
        Date admitDatetime = new DateTime(2014,2,2,11,0,0).toDate();

        // a visit with a visit note encounter with dispo = ADMIT and a *voided* admission encounter
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(visitDatetime)
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(visitNoteDatetime)
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();
        Encounter admissionEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(admitDatetime)
                .encounterType(emrApiProperties.getAdmissionEncounterType())
                .visit(visit)
                .voided(true)
                .dateVoided(new Date())
                .voidReason("test")
                .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }

    @Test
    public void shouldFindVisitEvenIfPatientHasMoreRecentVisitNoteWithoutAdmissionDisposition() throws Exception {

        Date visitDatetime = new DateTime(2014,2,2,9,0,0).toDate();
        Date firstVisitNoteDatetime = new DateTime(2014,2,2,10,0,0).toDate();
        Date secondVisitNoteDatetime = new DateTime(2014,2,2,11,0,0).toDate();

        // a visit with a visit note encounter with dispo = ADMIT and followed by a visit note with dispo = DEATH
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(visitDatetime)
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(firstVisitNoteDatetime)
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();
        Encounter secondVisitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(secondVisitNoteDatetime)
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(secondVisitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Death"))
                .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }

    @Test
    public void shouldNotFindVisitIfNoAdmitDisposition() throws Exception {

        Date visitDatetime = new DateTime(2014,2,2,9,0,0).toDate();
        Date visitNoteDatetime = new DateTime(2014,2,2,10,0,0).toDate();

        // a visit with a visit note with dispo = DEATH
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(visitDatetime)
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(visitNoteDatetime)
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Death"))
                .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldNotFindVisitIfAtAnotherLocation() throws Exception {

        Location visitLocation = testDataManager.location().name("Visit Location")
                .tag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS).save();
        Location queryLocation = testDataManager.location().name("Query Location")
                .tag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS).save();

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .location(visitLocation)
                .started(new Date())
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new Date())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();

        query.setLocation(queryLocation);
        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldFindVisitIfAtSameLocation() throws Exception {

        Location visitLocation = testDataManager.location().name("Visit Location")
                .tag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS).save();
        Location queryLocation = visitLocation;

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .location(visitLocation)
                .started(new Date())
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new Date())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();

        query.setLocation(queryLocation);
        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }

    @Test
    public void shouldNotReturnSameVisitTwice() throws Exception {

        // a visit with two visit note encounters with dispo = ADMIT
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(new Date())
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new Date())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();
        Encounter visitNoteEncounter2 = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new Date())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter2)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }


    @Test
    public void shouldNotFindVisitAwaitingAdmissionIfPatientNotInContext() throws Exception {

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(new Date())
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new Date())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();

        EvaluationContext context = new EvaluationContext();
        context.setBaseCohort(new Cohort(Collections.singleton(2)));

        VisitQueryResult result = visitQueryService.evaluate(query, context);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldNotFindVisitAwaitingAdmissionIfVisitNotInContext() throws Exception {

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(new Date())
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new Date())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();

        VisitEvaluationContext context = new VisitEvaluationContext();
        context.setBaseVisits(new VisitIdSet(10101));  // random visit id

        VisitQueryResult result = visitQueryService.evaluate(query, context);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldNotFindVisitWithDispositionOfAdmitIfFollowedByAdmissionDenialObs() throws Exception {

        // a visit with a dispo = ADMIT and DENY admit decision obs after it
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(new DateTime(2014,10,10,9,0,0).toDate())
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new DateTime(2014,10,10,10,0,0).toDate())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();
        Encounter visitNoteEncounter2 = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new DateTime(2014,10,10,11,0,0).toDate())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter2)
                .concept(emrApiProperties.getAdmissionDecisionConcept())
                .value(emrApiProperties.getDenyAdmissionConcept())
                .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldFindVisitWithDispositionOfAdmitIfFollowedByAdmissionDenialObsFollowedByAnotherAdmissionDisposition() throws Exception {

        // a visit with a dispo = ADMIT and DENY admit decision obs after it
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(new DateTime(2014,10,10,9,0,0).toDate())
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new DateTime(2014,10,10,10,0,0).toDate())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();
        Encounter visitNoteEncounter2 = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new DateTime(2014,10,10,11,0,0).toDate())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter2)
                .concept(emrApiProperties.getAdmissionDecisionConcept())
                .value(emrApiProperties.getDenyAdmissionConcept())
                .save();
        Encounter visitNoteEncounter3 = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new DateTime(2014,10,10,12,0,0).toDate())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter3)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }

    @Test
    public void shouldNotFindVisitWithDispositionOfAdmitIfFollowedByAdmissionDenialObsFollowedByAnotherAdmissionDispositionFollowedByAnotherAdmissionDenial() throws Exception {

        // a visit with a dispo = ADMIT and DENY admit decision obs after it
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(new DateTime(2014,10,10,9,0,0).toDate())
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new DateTime(2014,10,10,10,0,0).toDate())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();
        Encounter visitNoteEncounter2 = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new DateTime(2014,10,10,11,0,0).toDate())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter2)
                .concept(emrApiProperties.getAdmissionDecisionConcept())
                .value(emrApiProperties.getDenyAdmissionConcept())
                .save();
        Encounter visitNoteEncounter3 = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new DateTime(2014,10,10,12,0,0).toDate())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter3)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();
        Encounter visitNoteEncounter4 = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new DateTime(2014,10,10,13,0,0).toDate())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter4)
                .concept(emrApiProperties.getAdmissionDecisionConcept())
                .value(emrApiProperties.getDenyAdmissionConcept())
                .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldFindVisitWithDispositionOfAdmitIfFollowedByAdmissionDecisionThatIsNotDeny() throws Exception {

        // a visit with a dispo = ADMIT and DENY admit decision obs after it
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(new DateTime(2014,10,10,9,0,0).toDate())
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new DateTime(2014,10,10,10,0,0).toDate())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();
        Encounter visitNoteEncounter2 = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new DateTime(2014,10,10,11,0,0).toDate())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter2)
                .concept(emrApiProperties.getAdmissionDecisionConcept())
                .value(admitToHospital)
                .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }

    @Test
    public void shouldFindVisitWithDispositionOfAdmitIfPrecededByAdmissionDenialObs() throws Exception {

        // a visit with a dispo = ADMIT and DENY admit decision obs before it
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(new DateTime(2014,10,9,10,0,0).toDate())
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new DateTime(2014,10,10,10,0,0).toDate())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();
        Encounter visitNoteEncounter2 = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new DateTime(2014,10,9,10,0,0).toDate())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter2)
                .concept(emrApiProperties.getAdmissionDecisionConcept())
                .value(emrApiProperties.getDenyAdmissionConcept())
                .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }

    @Test
    public void shouldFindVisitWithDispositionOfAdmitIfFollowedByAdmissionDenialObsThatIsVoided() throws Exception {

        // a visit with a dispo = ADMIT and DENY admit decision obs after it
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(new DateTime(2014,10,9,10,0,0).toDate())
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new DateTime(2014,10,10,10,0,0).toDate())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();
        Encounter visitNoteEncounter2 = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new DateTime(2014,10,11,10,0,0).toDate())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter2)
                .concept(emrApiProperties.getAdmissionDecisionConcept())
                .value(emrApiProperties.getDenyAdmissionConcept())
                .voided(true)
                .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }


    @Test
    public void shouldNotFindVisitAwaitingAdmissionIfPatientIsDead() throws Exception {

        patient = testDataManager.randomPatient()
                .birthdate("2010-01-01")
                .dead(true)
                .deathDate(new Date())
                .causeOfDeath(conceptService.getConcept(3))   // a random concept, this doesn't matter
                .save();

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit = testDataManager.visit()
                .patient(patient)
                .visitType(emrApiProperties.getAtFacilityVisitType())
                .started(new Date())
                .save();
        Encounter visitNoteEncounter = testDataManager.encounter()
                .patient(patient)
                .encounterDatetime(new Date())
                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                .visit(visit)
                .save();
        testDataManager.obs()
                .person(patient)
                .encounter(visitNoteEncounter)
                .concept(dispositionDescriptor.getDispositionConcept())
                .value(admitToHospital)
                .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));
    }
}
