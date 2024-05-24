package org.openmrs.module.emrapi.adt.reporting.evaluator;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
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
    private LocationService locationService;

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

    private static final Date OCT_9_10AM = new DateTime(2014,10,9,10,0,0).toDate();
    private static final Date OCT_10_9AM = new DateTime(2014,10,10,9,0,0).toDate();
    private static final Date OCT_10_10AM = new DateTime(2014,10,10,10,0,0).toDate();
    private static final Date OCT_10_11AM = new DateTime(2014,10,10,11,0,0).toDate();
    private static final Date OCT_10_12PM = new DateTime(2014,10,10,12,0,0).toDate();
    private static final Date OCT_10_1PM = new DateTime(2014,10,10,13,0,0).toDate();
    private static final Date OCT_11_10AM = new DateTime(2014,10,11,10,0,0).toDate();
    
    @Before
    public void setup() throws Exception {
        executeDataSet("baseTestDataset.xml");
        dispositionDescriptor = ContextSensitiveMetadataTestUtils.setupDispositionDescriptor(conceptService, dispositionService);
        ContextSensitiveMetadataTestUtils.setupAdmissionDecisionConcept(conceptService, emrApiProperties);
        query = new AwaitingAdmissionVisitQuery();
        admitToHospital = emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital");
        patient = testDataManager.randomPatient().birthdate("2010-01-01").save();
    }

    private Visit createFacilityVisit(Date date) {
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setVisitType(emrApiProperties.getAtFacilityVisitType());
        visit.setStartDatetime(date);
        return visit;
    }

    private Encounter addEncounter(Visit visit, EncounterType type, Date date) {
        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setEncounterDatetime(date);
        encounter.setEncounterType(type);
        encounter.setVisit(visit);
        visit.addEncounter(encounter);
        return encounter;
    }

    private void addDispositionObsGroup(Encounter encounter, Concept disposition) {
        Obs dispositionObs = new Obs();
        dispositionObs.setPerson(patient);
        dispositionObs.setEncounter(encounter);
        dispositionObs.setConcept(dispositionDescriptor.getDispositionConcept());
        dispositionObs.setValueCoded(disposition);
        Obs dispositionGroup = new Obs();
        dispositionGroup.setPerson(patient);
        dispositionGroup.setEncounter(encounter);
        dispositionGroup.setConcept(dispositionDescriptor.getDispositionSetConcept());
        dispositionGroup.addGroupMember(dispositionObs);
        encounter.addObs(dispositionGroup);
    }

    private Obs addAdmissionDecisionObs(Encounter encounter, Concept value) {
        Obs obs = new Obs();
        obs.setPerson(patient);
        obs.setEncounter(encounter);
        obs.setConcept(emrApiProperties.getAdmissionDecisionConcept());
        obs.setValueCoded(value);
        encounter.addObs(obs);
        return obs;
    }

    @Test
    public void shouldFindVisitAwaitingAdmission() throws Exception {
        // a visit with a single visit note encounter with dispo = ADMIT
        Date now = new Date();
        Visit visit = createFacilityVisit(now);
        Encounter encounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), now);
        addDispositionObsGroup(encounter, admitToHospital);
        Context.getVisitService().saveVisit(visit);

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }

    @Test
    public void shouldNotCountDispositionOnVoidedEncounter() throws Exception {
        // a visit with a single *voided* visit note encounter with dispo = ADMIT
        Date now = new Date();
        Visit visit = createFacilityVisit(now);
        Encounter encounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), now);
        encounter.setVoided(true);
        encounter.setDateVoided(new Date());
        encounter.setVoidReason("test");
        addDispositionObsGroup(encounter, admitToHospital);
        Context.getVisitService().saveVisit(visit);

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldNotFindVisitIfPatientAdmitted() throws Exception {
        Date visitDatetime = new DateTime(2014,2,2,9,0,0).toDate();
        Date visitNoteDatetime = new DateTime(2014,2,2,10,0,0).toDate();
        Date admitDatetime = new DateTime(2014,2,2,11,0,0).toDate();

        // a visit with a visit note encounter with dispo = ADMIT and an admission encounter
        Visit visit = createFacilityVisit(visitDatetime);
        Encounter encounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), visitNoteDatetime);
        addDispositionObsGroup(encounter, admitToHospital);
        addEncounter(visit, emrApiProperties.getAdmissionEncounterType(), admitDatetime);
        Context.getVisitService().saveVisit(visit);

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldNotConsiderVoidedAdmissionEncounter() throws Exception {
        Date visitDatetime = new DateTime(2014,2,2,9,0,0).toDate();
        Date visitNoteDatetime = new DateTime(2014,2,2,10,0,0).toDate();
        Date admitDatetime = new DateTime(2014,2,2,11,0,0).toDate();

        // a visit with a visit note encounter with dispo = ADMIT and an admission encounter
        Visit visit = createFacilityVisit(visitDatetime);
        Encounter encounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), visitNoteDatetime);
        addDispositionObsGroup(encounter, admitToHospital);
        Encounter admissionEncounter = addEncounter(visit, emrApiProperties.getAdmissionEncounterType(), admitDatetime);
        admissionEncounter.setVoided(true);
        admissionEncounter.setDateVoided(new Date());
        admissionEncounter.setVoidReason("test");
        Context.getVisitService().saveVisit(visit);

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
        Visit visit = createFacilityVisit(visitDatetime);
        Encounter visitNoteEncounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), firstVisitNoteDatetime);
        addDispositionObsGroup(visitNoteEncounter, admitToHospital);
        Encounter secondVisitNoteEncounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), secondVisitNoteDatetime);
        addDispositionObsGroup(secondVisitNoteEncounter, emrConceptService.getConcept("org.openmrs.module.emrapi:Death"));
        Context.getVisitService().saveVisit(visit);

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }

    @Test
    public void shouldNotFindVisitIfNoAdmitDisposition() throws Exception {
        Date visitDatetime = new DateTime(2014,2,2,9,0,0).toDate();
        Date visitNoteDatetime = new DateTime(2014,2,2,10,0,0).toDate();

        // a visit with a visit note with dispo = DEATH
        Visit visit = createFacilityVisit(visitDatetime);
        Encounter visitNoteEncounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), visitNoteDatetime);
        addDispositionObsGroup(visitNoteEncounter, emrConceptService.getConcept("org.openmrs.module.emrapi:Death"));
        Context.getVisitService().saveVisit(visit);
        
        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldNotFindVisitIfAtAnotherLocation() throws Exception {
        Location visitLocation = new Location();
        visitLocation.setName("Visit Location");
        visitLocation.addTag(locationService.getLocationTagByName(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS));
        locationService.saveLocation(visitLocation);
        Location queryLocation = new Location();
        queryLocation.setName("Query Location");
        queryLocation.addTag(locationService.getLocationTagByName(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS));
        locationService.saveLocation(queryLocation);
        
        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit = createFacilityVisit(new Date());
        visit.setLocation(visitLocation);

        Encounter visitNoteEncounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), new Date());
        addDispositionObsGroup(visitNoteEncounter, admitToHospital);
        Context.getVisitService().saveVisit(visit);

        query.setLocation(queryLocation);
        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldFindVisitIfAtSameLocation() throws Exception {
        Location visitLocation = new Location();
        visitLocation.setName("Visit Location");
        visitLocation.addTag(locationService.getLocationTagByName(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS));
        locationService.saveLocation(visitLocation);

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit = createFacilityVisit(new Date());
        visit.setLocation(visitLocation);

        Encounter visitNoteEncounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), new Date());
        addDispositionObsGroup(visitNoteEncounter, admitToHospital);
        Context.getVisitService().saveVisit(visit);

        query.setLocation(visitLocation);
        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }

    @Test
    public void shouldNotReturnSameVisitTwice() throws Exception {

        // a visit with two visit note encounters with dispo = ADMIT
        Visit visit = createFacilityVisit(new Date());

        Encounter visitNoteEncounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), new Date());
        addDispositionObsGroup(visitNoteEncounter, admitToHospital);
        Encounter visitNoteEncounter2 = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), new Date());
        addDispositionObsGroup(visitNoteEncounter2, admitToHospital);

        Context.getVisitService().saveVisit(visit);

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }

    @Test
    public void shouldNotFindVisitAwaitingAdmissionIfPatientNotInContext() throws Exception {
        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit = createFacilityVisit(new Date());

        Encounter visitNoteEncounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), new Date());
        addDispositionObsGroup(visitNoteEncounter, admitToHospital);
        Context.getVisitService().saveVisit(visit);

        EvaluationContext context = new EvaluationContext();
        context.setBaseCohort(new Cohort(Collections.singleton(2)));

        VisitQueryResult result = visitQueryService.evaluate(query, context);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldNotFindVisitAwaitingAdmissionIfVisitNotInContext() throws Exception {
        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit = createFacilityVisit(new Date());
        Encounter visitNoteEncounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), new Date());
        addDispositionObsGroup(visitNoteEncounter, admitToHospital);
        Context.getVisitService().saveVisit(visit);
        
        VisitEvaluationContext context = new VisitEvaluationContext();
        context.setBaseVisits(new VisitIdSet(10101));  // random visit id

        VisitQueryResult result = visitQueryService.evaluate(query, context);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldNotFindVisitWithDispositionOfAdmitIfFollowedByAdmissionDenialObs() throws Exception {
        // a visit with a dispo = ADMIT and DENY admit decision obs after it
        Visit visit = createFacilityVisit(OCT_10_9AM);

        Encounter visitNoteEncounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), OCT_10_10AM);
        addDispositionObsGroup(visitNoteEncounter, admitToHospital);
        Encounter visitNoteEncounter2 = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), OCT_10_11AM);
        addAdmissionDecisionObs(visitNoteEncounter2, emrApiProperties.getDenyAdmissionConcept());
        Context.getVisitService().saveVisit(visit);
        
        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldFindVisitWithDispositionOfAdmitIfFollowedByAdmissionDenialObsFollowedByAnotherAdmissionDisposition() throws Exception {
        // a visit with a dispo = ADMIT and DENY admit decision obs after it
        Visit visit = createFacilityVisit(OCT_10_9AM);
        Encounter visitNoteEncounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), OCT_10_10AM);
        addDispositionObsGroup(visitNoteEncounter, admitToHospital);
        Encounter visitNoteEncounter2 = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), OCT_10_11AM);
        addAdmissionDecisionObs(visitNoteEncounter2, emrApiProperties.getDenyAdmissionConcept());
        Encounter visitNoteEncounter3 = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), OCT_10_12PM);
        addDispositionObsGroup(visitNoteEncounter3, admitToHospital);
        Context.getVisitService().saveVisit(visit);

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }

    @Test
    public void shouldNotFindVisitWithDispositionOfAdmitIfFollowedByAdmissionDenialObsFollowedByAnotherAdmissionDispositionFollowedByAnotherAdmissionDenial() throws Exception {
        // a visit with a dispo = ADMIT and DENY admit decision obs after it
        Visit visit = createFacilityVisit(OCT_10_9AM);
        Encounter visitNoteEncounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), OCT_10_10AM);
        addDispositionObsGroup(visitNoteEncounter, admitToHospital);
        Encounter visitNoteEncounter2 = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), OCT_10_11AM);
        addAdmissionDecisionObs(visitNoteEncounter2, emrApiProperties.getDenyAdmissionConcept());
        Encounter visitNoteEncounter3 = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), OCT_10_12PM);
        addDispositionObsGroup(visitNoteEncounter3, admitToHospital);
        Encounter visitNoteEncounter4 = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), OCT_10_1PM);
        addAdmissionDecisionObs(visitNoteEncounter4, emrApiProperties.getDenyAdmissionConcept());
        Context.getVisitService().saveVisit(visit);

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));
    }

    @Test
    public void shouldFindVisitWithDispositionOfAdmitIfFollowedByAdmissionDecisionThatIsNotDeny() throws Exception {
        // a visit with a dispo = ADMIT and DENY admit decision obs after it
        Visit visit = createFacilityVisit(OCT_10_9AM);
        Encounter visitNoteEncounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), OCT_10_10AM);
        addAdmissionDecisionObs(visitNoteEncounter, emrApiProperties.getDenyAdmissionConcept());
        Encounter visitNoteEncounter2 = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), OCT_10_11AM);
        addAdmissionDecisionObs(visitNoteEncounter2, admitToHospital);
        Context.getVisitService().saveVisit(visit);

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }

    @Test
    public void shouldFindVisitWithDispositionOfAdmitIfPrecededByAdmissionDenialObs() throws Exception {
        // a visit with a dispo = ADMIT and DENY admit decision obs before it
        Visit visit = createFacilityVisit(OCT_10_9AM);
        Encounter visitNoteEncounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), OCT_10_10AM);
        addDispositionObsGroup(visitNoteEncounter, admitToHospital);
        Encounter visitNoteEncounter2 = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), OCT_9_10AM);
        addAdmissionDecisionObs(visitNoteEncounter2, emrApiProperties.getDenyAdmissionConcept());
        Context.getVisitService().saveVisit(visit);
        
        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(1));
        assertThat(result.getMemberIds().iterator().next(), is(visit.getId()));
    }

    @Test
    public void shouldFindVisitWithDispositionOfAdmitIfFollowedByAdmissionDenialObsThatIsVoided() throws Exception {
        // a visit with a dispo = ADMIT and DENY admit decision obs after it
        Visit visit = createFacilityVisit(OCT_10_9AM);
        Encounter visitNoteEncounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), OCT_10_10AM);
        addDispositionObsGroup(visitNoteEncounter, admitToHospital);
        Encounter visitNoteEncounter2 = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), OCT_11_10AM);
        Obs decision = addAdmissionDecisionObs(visitNoteEncounter2, emrApiProperties.getDenyAdmissionConcept());
        decision.setVoided(true);
        Context.getVisitService().saveVisit(visit);

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
        Visit visit = createFacilityVisit(new Date());
        Encounter visitNoteEncounter = addEncounter(visit, emrApiProperties.getVisitNoteEncounterType(), new Date());
        addDispositionObsGroup(visitNoteEncounter, admitToHospital);
        Context.getVisitService().saveVisit(visit);

        VisitQueryResult result = visitQueryService.evaluate(query, null);
        assertThat(result.getMemberIds().size(), is(0));
    }
}
