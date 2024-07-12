package org.openmrs.module.emrapi.adt;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.contrib.testdata.builder.ObsBuilder;
import org.openmrs.module.emrapi.EmrApiContextSensitiveTest;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.disposition.DispositionType;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class AdtServiceImplTest extends EmrApiContextSensitiveTest {

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EmrConceptService emrConceptService;

    @Autowired
    private DispositionService dispositionService;

    @Autowired
    private AdtService adtService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    TestDataManager testDataManager;

    private DispositionDescriptor dispositionDescriptor;

    private Patient patient;
    private Visit visit;
    private Location visitLocation;
    private Location preAdmissionLocation;
    private Location admissionLocation;
    private Location transferLocation;
    private Location otherVisitLocation;
    private Concept admitDisposition;
    private Concept transferDisposition;
    private Concept dischargeDisposition;
    private Date visitDate;
    InpatientRequestSearchCriteria criteria;
    List<InpatientRequest> requests;

    @Before
    public void setup() throws Exception {
        executeDataSet("baseTestDataset.xml");
        dispositionDescriptor = ContextSensitiveMetadataTestUtils.setupDispositionDescriptor(conceptService, dispositionService);
        ContextSensitiveMetadataTestUtils.setupAdmissionDecisionConcept(conceptService, emrApiProperties);
        visitLocation = testDataManager.location().name("Hospital").tag("Visit Location").save();
        preAdmissionLocation = testDataManager.location().name("Pre-Admission").save();
        preAdmissionLocation.setParentLocation(visitLocation);
        testDataManager.getLocationService().saveLocation(preAdmissionLocation);
        admissionLocation = testDataManager.location().name("Admission Ward").tag("Admission Location").save();
        admissionLocation.setParentLocation(visitLocation);
        testDataManager.getLocationService().saveLocation(admissionLocation);
        transferLocation = testDataManager.location().name("Transfer Ward").tag("Admission Location").save();
        transferLocation.setParentLocation(visitLocation);
        otherVisitLocation = testDataManager.location().name("Other Hospital").tag("Visit Location").save();
        testDataManager.getLocationService().saveLocation(transferLocation);
        admitDisposition = emrConceptService.getConcept("org.openmrs.module.emrapi:Admit to hospital");
        transferDisposition = emrConceptService.getConcept("org.openmrs.module.emrapi:Transfer out of hospital");
        dischargeDisposition = emrConceptService.getConcept("org.openmrs.module.emrapi:Discharged");
        patient = testDataManager.randomPatient().birthdate("2010-01-01").save();
        visit = testDataManager.visit().patient(patient).visitType(emrApiProperties.getAtFacilityVisitType()).location(visitLocation).started("2020-10-30").save();
        visitDate = visit.getStartDatetime();
        criteria = new InpatientRequestSearchCriteria();
    }

    private Encounter createEncounter(EncounterType encounterType, Location location, Date date) {
        return testDataManager.encounter().patient(patient).visit(visit).encounterType(encounterType).encounterDatetime(date).location(location).save();
    }

    public Obs createAdmissionRequest(Date encounterDate) {
        Encounter e = createEncounter(emrApiProperties.getVisitNoteEncounterType(), preAdmissionLocation, encounterDate);
        ObsBuilder groupBuilder = testDataManager.obs().encounter(e).concept(dispositionDescriptor.getDispositionSetConcept());
        groupBuilder.member(testDataManager.obs().encounter(e).concept(dispositionDescriptor.getDispositionConcept()).value(admitDisposition).get());
        groupBuilder.member(testDataManager.obs().encounter(e).concept(dispositionDescriptor.getAdmissionLocationConcept()).value(admissionLocation.getLocationId().toString()).get());
        return groupBuilder.save();
    }

    public Encounter createAdmissionEncounter(Date encounterDate) {
        return createEncounter(emrApiProperties.getAdmissionEncounterType(), admissionLocation, encounterDate);
    }

    public Encounter createAdmissionDeniedEncounter(Date encounterDate) {
        Encounter e = createEncounter(emrApiProperties.getVisitNoteEncounterType(), preAdmissionLocation, encounterDate);
        e.addObs(testDataManager.obs().person(patient).concept(emrApiProperties.getAdmissionDecisionConcept()).value(emrApiProperties.getDenyAdmissionConcept()).get());
        return testDataManager.getEncounterService().saveEncounter(e);
    }

    public Obs createTransferRequest(Date encounterDate) {
        Encounter e = createEncounter(emrApiProperties.getVisitNoteEncounterType(), admissionLocation, encounterDate);
        ObsBuilder groupBuilder = testDataManager.obs().encounter(e).concept(dispositionDescriptor.getDispositionSetConcept());
        groupBuilder.member(testDataManager.obs().encounter(e).concept(dispositionDescriptor.getDispositionConcept()).value(transferDisposition).get());
        groupBuilder.member(testDataManager.obs().encounter(e).concept(dispositionDescriptor.getInternalTransferLocationConcept()).value(transferLocation.getLocationId().toString()).get());
        return groupBuilder.save();
    }

    public Encounter createTransferEncounter(Date encounterDate) {
        return createEncounter(emrApiProperties.getTransferWithinHospitalEncounterType(), transferLocation, encounterDate);
    }

    public Obs createDischargeRequest(Date encounterDate, Location currentLocation) {
        Encounter e = createEncounter(emrApiProperties.getVisitNoteEncounterType(), currentLocation, encounterDate);
        ObsBuilder groupBuilder = testDataManager.obs().encounter(e).concept(dispositionDescriptor.getDispositionSetConcept());
        groupBuilder.member(testDataManager.obs().encounter(e).concept(dispositionDescriptor.getDispositionConcept()).value(dischargeDisposition).get());
        return groupBuilder.save();
    }

    public Encounter createDischarge(Date encounterDate, Location currentLocation) {
        return createEncounter(emrApiProperties.getExitFromInpatientEncounterType(), currentLocation, encounterDate);
    }

    private List<InpatientRequest> assertNumRequests(InpatientRequestSearchCriteria criteria, int expected) {
        List<InpatientRequest> requests = adtService.getInpatientRequests(criteria);
        assertThat(requests.size(), equalTo(expected));
        return requests;
    }

    @Test
    public void shouldGetInpatientRequest() {
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        requests = adtService.getInpatientRequests(criteria);
        assertThat(requests.size(), equalTo(1));
        assertThat(requests.get(0).getDispositionType(), equalTo(DispositionType.ADMIT));
        assertThat(requests.get(0).getDisposition(), equalTo(admitDisposition));
        assertThat(requests.get(0).getDispositionLocation(), equalTo(admissionLocation));
    }

    // Filter based on visit location

    @Test
    public void shouldGetAdmissionRequestForVisitLocation() {
        criteria.addDispositionType(DispositionType.ADMIT);
        criteria.setVisitLocation(visitLocation);
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
    }

    @Test
    public void shouldGetAdmissionRequestForParentVisitLocation() {
        criteria.addDispositionType(DispositionType.ADMIT);
        criteria.setVisitLocation(admissionLocation);
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
    }

    @Test
    public void shouldNotGetAdmissionRequestForDifferentVisitLocation() {
        criteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        criteria.setVisitLocation(otherVisitLocation);
        assertNumRequests(criteria, 0);
    }

    // Filter based on Disposition Type

    @Test
    public void shouldGetInpatientRequestsBasedOnDispositionType() {
        assertNumRequests(criteria, 0);

        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        criteria.setDispositionTypes(Collections.singletonList(DispositionType.ADMIT));
        assertNumRequests(criteria, 1);
        criteria.setDispositionTypes(Collections.singletonList(DispositionType.TRANSFER));
        assertNumRequests(criteria, 0);
        criteria.setDispositionTypes(Collections.singletonList(DispositionType.DISCHARGE));
        assertNumRequests(criteria, 0);
        criteria.setDispositionTypes(Arrays.asList(DispositionType.ADMIT, DispositionType.TRANSFER));
        assertNumRequests(criteria, 1);
        criteria.setDispositionTypes(Arrays.asList(DispositionType.ADMIT, DispositionType.DISCHARGE));
        assertNumRequests(criteria, 1);
        criteria.setDispositionTypes(Arrays.asList(DispositionType.TRANSFER, DispositionType.DISCHARGE));
        assertNumRequests(criteria, 0);
        criteria.setDispositionTypes(null);

        createTransferRequest(DateUtils.addHours(visitDate, 4));
        assertNumRequests(criteria, 1);
        criteria.setDispositionTypes(Collections.singletonList(DispositionType.ADMIT));
        assertNumRequests(criteria, 0);
        criteria.setDispositionTypes(Collections.singletonList(DispositionType.TRANSFER));
        assertNumRequests(criteria, 1);
        criteria.setDispositionTypes(Collections.singletonList(DispositionType.DISCHARGE));
        assertNumRequests(criteria, 0);
        criteria.setDispositionTypes(Arrays.asList(DispositionType.ADMIT, DispositionType.TRANSFER));
        assertNumRequests(criteria, 1);
        criteria.setDispositionTypes(Arrays.asList(DispositionType.ADMIT, DispositionType.DISCHARGE));
        assertNumRequests(criteria, 0);
        criteria.setDispositionTypes(Arrays.asList(DispositionType.TRANSFER, DispositionType.DISCHARGE));
        assertNumRequests(criteria, 1);
        criteria.setDispositionTypes(null);

        createDischargeRequest(DateUtils.addHours(visitDate, 6), preAdmissionLocation);
        assertNumRequests(criteria, 1);
        criteria.setDispositionTypes(Collections.singletonList(DispositionType.ADMIT));
        assertNumRequests(criteria, 0);
        criteria.setDispositionTypes(Collections.singletonList(DispositionType.TRANSFER));
        assertNumRequests(criteria, 0);
        criteria.setDispositionTypes(Collections.singletonList(DispositionType.DISCHARGE));
        assertNumRequests(criteria, 1);
        criteria.setDispositionTypes(Arrays.asList(DispositionType.ADMIT, DispositionType.TRANSFER));
        assertNumRequests(criteria, 0);
        criteria.setDispositionTypes(Arrays.asList(DispositionType.ADMIT, DispositionType.DISCHARGE));
        assertNumRequests(criteria, 1);
        criteria.setDispositionTypes(Arrays.asList(DispositionType.TRANSFER, DispositionType.DISCHARGE));
        assertNumRequests(criteria, 1);
    }

    // Filter based on disposition location

    @Test
    public void shouldGetInpatientRequestsBasedOnDispositionLocation() {
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        criteria.addDispositionLocation(preAdmissionLocation);
        assertNumRequests(criteria, 0);
        criteria.addDispositionLocation(transferLocation);
        assertNumRequests(criteria, 0);
        criteria.addDispositionLocation(admissionLocation);
        assertNumRequests(criteria, 1);
    }

    // Filter based on patient ids

    @Test
    public void shouldGetInpatientRequestsBasedOnPatient() {
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        criteria.addPatientId(patient.getPatientId() + 1);
        assertNumRequests(criteria, 0);
        criteria.addPatientId(patient.getPatientId() + 2);
        assertNumRequests(criteria, 0);
        criteria.addPatientId(patient.getPatientId());
        assertNumRequests(criteria, 1);
    }

    // Filter based on visit ids

    @Test
    public void shouldGetInpatientRequestsBasedOnVisit() {
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        criteria.addVisitId(visit.getVisitId() + 1);
        assertNumRequests(criteria, 0);
        criteria.addVisitId(visit.getVisitId() + 2);
        assertNumRequests(criteria, 0);
        criteria.addVisitId(visit.getVisitId());
        assertNumRequests(criteria, 1);
    }

    // Filter based on timeline of disposition obs and encounters within visit

    @Test
    public void shouldNotGetAdmissionRequestIfPatientHasBeenAdmitted() {
        criteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        createAdmissionEncounter(DateUtils.addHours(visitDate, 3));
        assertNumRequests(criteria, 0);
    }

    @Test
    public void shouldGetAdmissionRequestIfAdmissionEncounterIsVoided() {
        criteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        Encounter e = createAdmissionEncounter(DateUtils.addHours(visitDate, 3));
        assertNumRequests(criteria, 0);
        testDataManager.getEncounterService().voidEncounter(e, "Unknown");
        assertNumRequests(criteria, 1);
    }

    @Test
    public void shouldOnlyReturnLatestDispositionRequestWithinAGivenVisit() {
        criteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        createAdmissionRequest(DateUtils.addHours(visitDate, 3));
        assertNumRequests(criteria, 1);
    }

    @Test
    public void shouldOnlyReturnAdmitIfItIsLaterThanDischarge() {
        criteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        createDischargeRequest(DateUtils.addHours(visitDate, 3), preAdmissionLocation);
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 4));
        assertNumRequests(criteria, 1);
    }

    @Test
    public void shouldGetAdmissionRequestIfAfterAdmissionEncounter() {
        criteria.addDispositionType(DispositionType.ADMIT);
        createAdmissionEncounter(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 4));
        assertNumRequests(criteria, 1);
    }

    // Filter based on timeline of disposition obs and denial obs within visit

    @Test
    public void shouldNotGetAdmissionRequestIfPatientHasBeenDenied() {
        criteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        createAdmissionDeniedEncounter(DateUtils.addHours(visitDate, 3));
        assertNumRequests(criteria, 0);
    }

    @Test
    public void shouldGetAdmissionRequestIfPatientHasAnAdmissionRequestAfterADenial() {
        criteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        createAdmissionDeniedEncounter(DateUtils.addHours(visitDate, 3));
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 4));
        assertNumRequests(criteria, 1);
    }

    @Test
    public void shouldGetAdmissionRequestIfPatientHasAnAdmissionDecisionThatIsNotDeny() {
        criteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        Encounter e = createAdmissionDeniedEncounter(DateUtils.addHours(visitDate, 3));
        assertNumRequests(criteria, 0);
        for (Obs o : e.getAllObs()) {
            if (o.getConcept().equals(emrApiProperties.getAdmissionDecisionConcept())) {
                o.setValueCoded(emrApiProperties.getPatientDiedConcept());
                testDataManager.getObsService().saveObs(o, "Unknown");
            }
        }
        assertNumRequests(criteria, 1);
    }

    @Test
    public void shouldGetAdmissionRequestWithDispositionOfAdmitIfPrecededByAdmissionDenialObs() {
        criteria.addDispositionType(DispositionType.ADMIT);
        createAdmissionDeniedEncounter(DateUtils.addHours(visitDate, 1));
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
    }

    @Test
    public void shouldGetAdmissionRequestWithDispositionOfAdmitIfFollowedByAdmissionDenialObsThatIsVoided() {
        criteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        Encounter e = createAdmissionDeniedEncounter(DateUtils.addHours(visitDate, 3));
        assertNumRequests(criteria, 0);
        for (Obs o : e.getAllObs()) {
            if (o.getConcept().equals(emrApiProperties.getAdmissionDecisionConcept())) {
                testDataManager.getObsService().voidObs(o, "Unknown");
            }
        }
        assertNumRequests(criteria, 1);
    }

    // Filter out patients who have died or whose visit is ended

    @Test
    public void shouldNotGetAdmissionRequestIfPatientHasDied() {
        criteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        patient.setDead(true);
        patient.setCauseOfDeathNonCoded("Unknown");
        testDataManager.getPatientService().savePatient(patient);
        assertNumRequests(criteria, 0);
    }

    @Test
    public void shouldGetInpatientRequestsForEndedVisits() {
        criteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        testDataManager.getVisitService().endVisit(visit, DateUtils.addHours(visitDate, 4));
        assertNumRequests(criteria, 0);
    }

    // Filter out voided data

    @Test
    public void shouldNotGetAdmissionRequestIfPatientIsVoided() {
        criteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(criteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(criteria, 1);
        testDataManager.getPatientService().voidPatient(patient, "Unknown");
        assertNumRequests(criteria, 0);
    }

    @Test
    public void shouldNotGetAdmissionRequestIfEncounterIsVoided() {
        criteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(criteria, 0);
        Obs o = createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        requests = assertNumRequests(criteria, 1);
        testDataManager.getEncounterService().voidEncounter(o.getEncounter(), "Unknown");
        assertNumRequests(criteria, 0);
    }

    @Test
    public void shouldNotGetAdmissionRequestIfObsIsVoided() {
        criteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(criteria, 0);
        Obs o = createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        requests = assertNumRequests(criteria, 1);
        testDataManager.getObsService().voidObs(o, "Unknown");
        assertNumRequests(criteria, 0);
    }
}
