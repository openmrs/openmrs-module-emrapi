package org.openmrs.module.emrapi.adt.reporting.evaluator;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Location;
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

    @Before
    public void setup() throws Exception {
        executeDataSet("baseTestDataset.xml");
        dispositionDescriptor = ContextSensitiveMetadataTestUtils.setupDispositionDescriptor(conceptService, dispositionService);
        ContextSensitiveMetadataTestUtils.setupAdmissionDecisionConcept(conceptService, emrApiProperties);
        query = new AwaitingAdmissionVisitQuery();
    }

    @Test
    public void shouldFindVisitAwaitingAdmission() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit =
                testDataManager.visit()
                    .patient(patient)
                    .visitType(emrApiProperties.getAtFacilityVisitType())
                    .started(new Date())
                    .encounter(testDataManager.encounter()
                            .patient(patient)
                            .encounterDatetime(new Date())
                            .encounterType(emrApiProperties.getVisitNoteEncounterType())
                            .obs(testDataManager.obs()
                                    .concept(dispositionDescriptor.getDispositionConcept())
                                    .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                    .get())
                            .get())
                    .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));

    }

    @Test
    public void shouldNotCountDispositionOnVoidedEncounter() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        // a visit with a single *voided* visit note encounter with dispo = ADMIT
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new Date())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .voided(true)
                                .dateVoided(new Date())
                                .voidReason("test")
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));

    }

    @Test
    public void shouldNotFindVisitIfPatientAdmitted() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        Date visitDatetime = new DateTime(2014,2,2,9,0,0).toDate();
        Date visitNoteDatetime = new DateTime(2014,2,2,10,0,0).toDate();
        Date admitDatetime = new DateTime(2014,2,2,11,0,0).toDate();

        // a visit with a visit note encounter with dispo = ADMIT and an admission encounter
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(visitDatetime)
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(visitNoteDatetime)
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                    .concept(dispositionDescriptor.getDispositionConcept())
                                    .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                    .get())
                                .get())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(admitDatetime)
                                .encounterType(emrApiProperties.getAdmissionEncounterType())
                                .get())
                        .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));

    }

    @Test
    public void shouldNotConsiderVoidedAdmissionEncounter() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        Date visitDatetime = new DateTime(2014,2,2,9,0,0).toDate();
        Date visitNoteDatetime = new DateTime(2014,2,2,10,0,0).toDate();
        Date admitDatetime = new DateTime(2014,2,2,11,0,0).toDate();

        // a visit with a visit note encounter with dispo = ADMIT and a *voided* admission encounter
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(visitDatetime)
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(visitNoteDatetime)
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(admitDatetime)
                                .encounterType(emrApiProperties.getAdmissionEncounterType())
                                .voided(true)
                                .dateVoided(new Date())
                                    .voidReason("test")
                                .get())
                        .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));

    }


    @Test
    public void shouldFindVisitEvenIfPatientHasMoreRecentVisitNoteWithoutAdmissionDisposition() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        Date visitDatetime = new DateTime(2014,2,2,9,0,0).toDate();
        Date firstVisitNoteDatetime = new DateTime(2014,2,2,10,0,0).toDate();
        Date secondVisitNoteDatetime = new DateTime(2014,2,2,11,0,0).toDate();

        // a visit with a visit note encounter with dispo = ADMIT and followed by a visit note with dispo = DEATH
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(visitDatetime)
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(firstVisitNoteDatetime)
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(secondVisitNoteDatetime)
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                            .concept(dispositionDescriptor.getDispositionConcept())
                                            .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Death"))
                                            .get())
                                .get())
                        .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }

    @Test
    public void shouldNotFindVisitIfNoAdmitDisposition() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        Date visitDatetime = new DateTime(2014,2,2,9,0,0).toDate();
        Date visitNoteDatetime = new DateTime(2014,2,2,10,0,0).toDate();

        // a visit with a visit note with dispo = DEATH
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(visitDatetime)
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(visitNoteDatetime)
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Death"))
                                        .get())
                                .get())
                        .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldNotFindVisitIfAtAnotherLocation() throws Exception {

        Patient patient = testDataManager.randomPatient().save();
        Location visitLocation = testDataManager.location().name("Visit Location")
                .tag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS).save();
        Location queryLocation = testDataManager.location().name("Query Location")
                .tag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS).save();

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .location(visitLocation)
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new Date())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .save();

        query.setLocation(queryLocation);
        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldFindVisitIfAtSameLocation() throws Exception {

        Patient patient = testDataManager.randomPatient().save();
        Location visitLocation = testDataManager.location().name("Visit Location")
                .tag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS).save();
        Location queryLocation = visitLocation;

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .location(visitLocation)
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new Date())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .save();

        query.setLocation(queryLocation);
        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));

    }

    @Test
    public void shouldNotReturnSameVisitTwice() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        // a visit with two visit note encounters with dispo = ADMIT
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new Date())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new Date())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));

    }


    @Test
    public void shouldNotFindVisitAwaitingAdmissionIfPatientNotInContext() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new Date())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .save();

        EvaluationContext context = new EvaluationContext();
        context.setBaseCohort(new Cohort(Collections.singleton(2)));

        VisitQueryResult result = visitQueryService.evaluate(query, context);
        assertThat(result.getMemberIds().size(), is(0));

    }

    @Test
    public void shouldNotFindVisitAwaitingAdmissionIfVisitNotInContext() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new Date())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .save();

        VisitEvaluationContext context = new VisitEvaluationContext();
        context.setBaseVisits(new VisitIdSet(10101));  // random visit id

        VisitQueryResult result = visitQueryService.evaluate(query, context);
        assertThat(result.getMemberIds().size(), is(0));

    }

    @Test
    public void shouldNotFindVisitWithDispositionOfAdmitIfFollowedByAdmissionDenialObs() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        // a visit with a dispo = ADMIT and DENY admit decision obs after it
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new DateTime(2014,10,10,10,0,0).toDate())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new DateTime(2014,10,11,10,0,0).toDate())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(emrApiProperties.getAdmissionDecisionConcept())
                                        .value(emrApiProperties.getDenyAdmissionConcept())
                                        .get())
                                .get())
                        .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));

    }

    @Test
    public void shouldFindVisitWithDispositionOfAdmitIfFollowedByAdmissionDenialObsFollowedByAnotherAdmissionDisposition() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        // a visit with a dispo = ADMIT and DENY admit decision obs after it
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new DateTime(2014,10,10,10,0,0).toDate())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new DateTime(2014,10,11,10,0,0).toDate())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(emrApiProperties.getAdmissionDecisionConcept())
                                        .value(emrApiProperties.getDenyAdmissionConcept())
                                        .get())
                                .get())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new DateTime(2014,10,12,10,0,0).toDate())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));

    }

    @Test
    public void shouldNotFindVisitWithDispositionOfAdmitIfFollowedByAdmissionDenialObsFollowedByAnotherAdmissionDispositionFollowedByAnotherAdmissionDenial() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        // a visit with a dispo = ADMIT and DENY admit decision obs after it
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new DateTime(2014,10,10,10,0,0).toDate())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new DateTime(2014,10,11,10,0,0).toDate())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(emrApiProperties.getAdmissionDecisionConcept())
                                        .value(emrApiProperties.getDenyAdmissionConcept())
                                        .get())
                                .get())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new DateTime(2014,10,12,10,0,0).toDate())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new DateTime(2014,10,13,10,0,0).toDate())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(emrApiProperties.getAdmissionDecisionConcept())
                                        .value(emrApiProperties.getDenyAdmissionConcept())
                                        .get())
                                .get())
                        .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));

    }

    @Test
    public void shouldFindVisitWithDispositionOfAdmitIfFollowedByAdmissionDecisionThatIsNotDeny() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        // a visit with a dispo = ADMIT and DENY admit decision obs after it
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new DateTime(2014,10,10,10,0,0).toDate())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new DateTime(2014,10,11,10,0,0).toDate())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(emrApiProperties.getAdmissionDecisionConcept())
                                        .get())
                                .get())
                        .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));

    }

    @Test
    public void shouldFindVisitWithDispositionOfAdmitIfPrecededByAdmissionDenialObs() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        // a visit with a dispo = ADMIT and DENY admit decision obs before it
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new DateTime(2014,10,10,10,0,0).toDate())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new DateTime(2014,10,9,10,0,0).toDate())   // this time is *before* the admission request
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(emrApiProperties.getAdmissionDecisionConcept())
                                        .value(emrApiProperties.getDenyAdmissionConcept())
                                        .get())
                                .get())
                        .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));

    }

    @Test
    public void shouldFindVisitWithDispositionOfAdmitIfFollowedByAdmissionDenialObsThatIsVoided() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        // a visit with a dispo = ADMIT and DENY admit decision obs after it
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new DateTime(2014,10,10,10,0,0).toDate())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new DateTime(2014,10,11,10,0,0).toDate())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(emrApiProperties.getAdmissionDecisionConcept())
                                        .value(emrApiProperties.getDenyAdmissionConcept())
                                        .voided(true)
                                        .get())
                                .get())
                        .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));

    }


    @Test
    public void shouldNotFindVisitAwaitingAdmissionIfPatientIsDead() throws Exception {

        Patient patient = testDataManager.randomPatient()
                .dead(true)
                .deathDate(new Date())
                .causeOfDeath(conceptService.getConcept(3))   // a random concept, this doesn't matter
                .save();

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(new Date())
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionConcept())
                                        .value(emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital"))
                                        .get())
                                .get())
                        .save();

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));

    }


}
