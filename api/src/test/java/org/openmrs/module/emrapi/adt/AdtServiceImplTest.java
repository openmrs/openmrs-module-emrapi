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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
    InpatientRequestSearchCriteria requestCriteria;
    List<InpatientRequest> requests;
    InpatientAdmissionSearchCriteria admissionCriteria;

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
        requestCriteria = new InpatientRequestSearchCriteria();
        admissionCriteria = new InpatientAdmissionSearchCriteria();
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

    private List<InpatientAdmission> assertNumAdmissions(InpatientAdmissionSearchCriteria criteria, int expected) {
        List<InpatientAdmission> admissions = adtService.getInpatientAdmissions(criteria);
        assertThat(admissions.size(), equalTo(expected));
        return admissions;
    }

    //*********** INPATIENT REQUEST TESTS *****************

    @Test
    public void shouldGetInpatientRequest() {
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        requests = adtService.getInpatientRequests(requestCriteria);
        assertThat(requests.size(), equalTo(1));
        assertThat(requests.get(0).getDispositionType(), equalTo(DispositionType.ADMIT));
        assertThat(requests.get(0).getDisposition(), equalTo(admitDisposition));
        assertThat(requests.get(0).getDispositionLocation(), equalTo(admissionLocation));
    }

    // Filter based on visit location

    @Test
    public void shouldGetAdmissionRequestForVisitLocation() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        requestCriteria.setVisitLocation(visitLocation);
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
    }

    @Test
    public void shouldGetAdmissionRequestForParentVisitLocation() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        requestCriteria.setVisitLocation(admissionLocation);
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
    }

    @Test
    public void shouldNotGetAdmissionRequestForDifferentVisitLocation() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        requestCriteria.setVisitLocation(otherVisitLocation);
        assertNumRequests(requestCriteria, 0);
    }

    // Filter based on Disposition Type

    @Test
    public void shouldGetInpatientRequestsBasedOnDispositionType() {
        assertNumRequests(requestCriteria, 0);

        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        requestCriteria.setDispositionTypes(Collections.singletonList(DispositionType.ADMIT));
        assertNumRequests(requestCriteria, 1);
        requestCriteria.setDispositionTypes(Collections.singletonList(DispositionType.TRANSFER));
        assertNumRequests(requestCriteria, 0);
        requestCriteria.setDispositionTypes(Collections.singletonList(DispositionType.DISCHARGE));
        assertNumRequests(requestCriteria, 0);
        requestCriteria.setDispositionTypes(Arrays.asList(DispositionType.ADMIT, DispositionType.TRANSFER));
        assertNumRequests(requestCriteria, 1);
        requestCriteria.setDispositionTypes(Arrays.asList(DispositionType.ADMIT, DispositionType.DISCHARGE));
        assertNumRequests(requestCriteria, 1);
        requestCriteria.setDispositionTypes(Arrays.asList(DispositionType.TRANSFER, DispositionType.DISCHARGE));
        assertNumRequests(requestCriteria, 0);
        requestCriteria.setDispositionTypes(null);

        createTransferRequest(DateUtils.addHours(visitDate, 4));
        assertNumRequests(requestCriteria, 1);
        requestCriteria.setDispositionTypes(Collections.singletonList(DispositionType.ADMIT));
        assertNumRequests(requestCriteria, 0);
        requestCriteria.setDispositionTypes(Collections.singletonList(DispositionType.TRANSFER));
        assertNumRequests(requestCriteria, 1);
        requestCriteria.setDispositionTypes(Collections.singletonList(DispositionType.DISCHARGE));
        assertNumRequests(requestCriteria, 0);
        requestCriteria.setDispositionTypes(Arrays.asList(DispositionType.ADMIT, DispositionType.TRANSFER));
        assertNumRequests(requestCriteria, 1);
        requestCriteria.setDispositionTypes(Arrays.asList(DispositionType.ADMIT, DispositionType.DISCHARGE));
        assertNumRequests(requestCriteria, 0);
        requestCriteria.setDispositionTypes(Arrays.asList(DispositionType.TRANSFER, DispositionType.DISCHARGE));
        assertNumRequests(requestCriteria, 1);
        requestCriteria.setDispositionTypes(null);

        createDischargeRequest(DateUtils.addHours(visitDate, 6), preAdmissionLocation);
        assertNumRequests(requestCriteria, 1);
        requestCriteria.setDispositionTypes(Collections.singletonList(DispositionType.ADMIT));
        assertNumRequests(requestCriteria, 0);
        requestCriteria.setDispositionTypes(Collections.singletonList(DispositionType.TRANSFER));
        assertNumRequests(requestCriteria, 0);
        requestCriteria.setDispositionTypes(Collections.singletonList(DispositionType.DISCHARGE));
        assertNumRequests(requestCriteria, 1);
        requestCriteria.setDispositionTypes(Arrays.asList(DispositionType.ADMIT, DispositionType.TRANSFER));
        assertNumRequests(requestCriteria, 0);
        requestCriteria.setDispositionTypes(Arrays.asList(DispositionType.ADMIT, DispositionType.DISCHARGE));
        assertNumRequests(requestCriteria, 1);
        requestCriteria.setDispositionTypes(Arrays.asList(DispositionType.TRANSFER, DispositionType.DISCHARGE));
        assertNumRequests(requestCriteria, 1);
    }

    // Filter based on disposition location

    @Test
    public void shouldGetInpatientRequestsBasedOnDispositionLocation() {
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        requestCriteria.addDispositionLocation(preAdmissionLocation);
        assertNumRequests(requestCriteria, 0);
        requestCriteria.addDispositionLocation(transferLocation);
        assertNumRequests(requestCriteria, 0);
        requestCriteria.addDispositionLocation(admissionLocation);
        assertNumRequests(requestCriteria, 1);
    }

    // Filter based on patient ids

    @Test
    public void shouldGetInpatientRequestsBasedOnPatient() {
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        requestCriteria.addPatientId(patient.getPatientId() + 1);
        assertNumRequests(requestCriteria, 0);
        requestCriteria.addPatientId(patient.getPatientId() + 2);
        assertNumRequests(requestCriteria, 0);
        requestCriteria.addPatientId(patient.getPatientId());
        assertNumRequests(requestCriteria, 1);
    }

    // Filter based on visit ids

    @Test
    public void shouldGetInpatientRequestsBasedOnVisit() {
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        requestCriteria.addVisitId(visit.getVisitId() + 1);
        assertNumRequests(requestCriteria, 0);
        requestCriteria.addVisitId(visit.getVisitId() + 2);
        assertNumRequests(requestCriteria, 0);
        requestCriteria.addVisitId(visit.getVisitId());
        assertNumRequests(requestCriteria, 1);
    }

    // Filter based on timeline of disposition obs and encounters within visit

    @Test
    public void shouldNotGetAdmissionRequestIfPatientHasBeenAdmitted() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        createAdmissionEncounter(DateUtils.addHours(visitDate, 3));
        assertNumRequests(requestCriteria, 0);
    }

    @Test
    public void shouldGetAdmissionRequestIfAdmissionEncounterIsVoided() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        Encounter e = createAdmissionEncounter(DateUtils.addHours(visitDate, 3));
        assertNumRequests(requestCriteria, 0);
        testDataManager.getEncounterService().voidEncounter(e, "Unknown");
        assertNumRequests(requestCriteria, 1);
    }

    @Test
    public void shouldOnlyReturnLatestDispositionRequestWithinAGivenVisit() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        createAdmissionRequest(DateUtils.addHours(visitDate, 3));
        assertNumRequests(requestCriteria, 1);
    }

    @Test
    public void shouldOnlyReturnAdmitIfItIsLaterThanDischarge() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        createDischargeRequest(DateUtils.addHours(visitDate, 3), preAdmissionLocation);
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 4));
        assertNumRequests(requestCriteria, 1);
    }

    @Test
    public void shouldGetAdmissionRequestIfAfterAdmissionEncounter() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        createAdmissionEncounter(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 4));
        assertNumRequests(requestCriteria, 1);
    }

    // Filter based on timeline of disposition obs and denial obs within visit

    @Test
    public void shouldNotGetAdmissionRequestIfPatientHasBeenDenied() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        createAdmissionDeniedEncounter(DateUtils.addHours(visitDate, 3));
        assertNumRequests(requestCriteria, 0);
    }

    @Test
    public void shouldGetAdmissionRequestIfPatientHasAnAdmissionRequestAfterADenial() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        createAdmissionDeniedEncounter(DateUtils.addHours(visitDate, 3));
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 4));
        assertNumRequests(requestCriteria, 1);
    }

    @Test
    public void shouldGetAdmissionRequestIfPatientHasAnAdmissionDecisionThatIsNotDeny() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        Encounter e = createAdmissionDeniedEncounter(DateUtils.addHours(visitDate, 3));
        assertNumRequests(requestCriteria, 0);
        for (Obs o : e.getAllObs()) {
            if (o.getConcept().equals(emrApiProperties.getAdmissionDecisionConcept())) {
                o.setValueCoded(emrApiProperties.getPatientDiedConcept());
                testDataManager.getObsService().saveObs(o, "Unknown");
            }
        }
        assertNumRequests(requestCriteria, 1);
    }

    @Test
    public void shouldGetAdmissionRequestWithDispositionOfAdmitIfPrecededByAdmissionDenialObs() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        createAdmissionDeniedEncounter(DateUtils.addHours(visitDate, 1));
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
    }

    @Test
    public void shouldGetAdmissionRequestWithDispositionOfAdmitIfFollowedByAdmissionDenialObsThatIsVoided() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        Encounter e = createAdmissionDeniedEncounter(DateUtils.addHours(visitDate, 3));
        assertNumRequests(requestCriteria, 0);
        for (Obs o : e.getAllObs()) {
            if (o.getConcept().equals(emrApiProperties.getAdmissionDecisionConcept())) {
                testDataManager.getObsService().voidObs(o, "Unknown");
            }
        }
        assertNumRequests(requestCriteria, 1);
    }

    // Filter out patients who have died or whose visit is ended

    @Test
    public void shouldNotGetAdmissionRequestIfPatientHasDied() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        patient.setDead(true);
        patient.setCauseOfDeathNonCoded("Unknown");
        testDataManager.getPatientService().savePatient(patient);
        assertNumRequests(requestCriteria, 0);
    }

    @Test
    public void shouldNotGetInpatientRequestsForEndedVisits() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        testDataManager.getVisitService().endVisit(visit, DateUtils.addHours(visitDate, 4));
        assertNumRequests(requestCriteria, 0);
    }

    // Filter out voided data

    @Test
    public void shouldNotGetAdmissionRequestIfPatientIsVoided() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(requestCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumRequests(requestCriteria, 1);
        testDataManager.getPatientService().voidPatient(patient, "Unknown");
        assertNumRequests(requestCriteria, 0);
    }

    @Test
    public void shouldNotGetAdmissionRequestIfEncounterIsVoided() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(requestCriteria, 0);
        Obs o = createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        requests = assertNumRequests(requestCriteria, 1);
        testDataManager.getEncounterService().voidEncounter(o.getEncounter(), "Unknown");
        assertNumRequests(requestCriteria, 0);
    }

    @Test
    public void shouldNotGetAdmissionRequestIfObsIsVoided() {
        requestCriteria.addDispositionType(DispositionType.ADMIT);
        assertNumRequests(requestCriteria, 0);
        Obs o = createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        requests = assertNumRequests(requestCriteria, 1);
        testDataManager.getObsService().voidObs(o, "Unknown");
        assertNumRequests(requestCriteria, 0);
    }

    //*********** INPATIENT ADMISSION TESTS *****************

    @Test
    public void shouldNotGetAdmissionIfNoAdtEncounters() {
        assertNumAdmissions(admissionCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        assertNumAdmissions(admissionCriteria, 0);
    }

    @Test
    public void shouldGetAdmissionIfAdmissionEncounters() {
        assertNumAdmissions(admissionCriteria, 0);
        Encounter e = createAdmissionEncounter(DateUtils.addHours(visitDate, 2));
        List<InpatientAdmission> admissions = assertNumAdmissions(admissionCriteria, 1);
        InpatientAdmission admission = admissions.get(0);
        assertThat(admission.getAdmissionEncounters().size(), equalTo(1));
        assertThat(admission.getTransferEncounters().size(), equalTo(0));
        assertThat(admission.getDischargeEncounters().size(), equalTo(0));
        assertThat(admission.getAdmissionEncounters().iterator().next(), equalTo(e));
    }

    @Test
    public void shouldGetAdmissionIfTransferEncounters() {
        assertNumAdmissions(admissionCriteria, 0);
        Encounter e = createTransferEncounter(DateUtils.addHours(visitDate, 2));
        List<InpatientAdmission> admissions = assertNumAdmissions(admissionCriteria, 1);
        InpatientAdmission admission = admissions.get(0);
        assertThat(admission.getAdmissionEncounters().size(), equalTo(0));
        assertThat(admission.getTransferEncounters().size(), equalTo(1));
        assertThat(admission.getDischargeEncounters().size(), equalTo(0));
        assertThat(admission.getTransferEncounters().iterator().next(), equalTo(e));
    }

    @Test
    public void shouldGetAdmissionIfDischargeEncounters() {
        admissionCriteria.setIncludeDischarged(true);
        assertNumAdmissions(admissionCriteria, 0);
        Encounter e = createDischarge(DateUtils.addHours(visitDate, 2), visitLocation);
        List<InpatientAdmission> admissions = assertNumAdmissions(admissionCriteria, 1);
        InpatientAdmission admission = admissions.get(0);
        assertThat(admission.getAdmissionEncounters().size(), equalTo(0));
        assertThat(admission.getTransferEncounters().size(), equalTo(0));
        assertThat(admission.getDischargeEncounters().size(), equalTo(1));
        assertThat(admission.getDischargeEncounters().iterator().next(), equalTo(e));
    }

    @Test
    public void shouldGetAdmissionForVisitLocation() {
        admissionCriteria.setVisitLocation(visitLocation);
        assertNumAdmissions(admissionCriteria, 0);
        createAdmissionEncounter(DateUtils.addHours(visitDate, 2));
        admissionCriteria.setVisitLocation(visitLocation);
        assertNumAdmissions(admissionCriteria, 1);
    }

    @Test
    public void shouldGetAdmissionForParentVisitLocation() {
        admissionCriteria.setVisitLocation(visitLocation);
        assertNumAdmissions(admissionCriteria, 0);
        createAdmissionEncounter(DateUtils.addHours(visitDate, 2));
        admissionCriteria.setVisitLocation(admissionLocation);
        assertNumAdmissions(admissionCriteria, 1);
    }

    @Test
    public void shouldNotGetAdmissionForDifferentVisitLocation() {
        admissionCriteria.setVisitLocation(visitLocation);
        assertNumAdmissions(admissionCriteria, 0);
        createAdmissionEncounter(DateUtils.addHours(visitDate, 2));
        admissionCriteria.setVisitLocation(otherVisitLocation);
        assertNumAdmissions(admissionCriteria, 0);
    }

    @Test
    public void shouldGetAdmissionForInpatientLocation() {
        admissionCriteria.addCurrentInpatientLocation(admissionLocation);
        assertNumAdmissions(admissionCriteria, 0);
        createAdmissionEncounter(DateUtils.addHours(visitDate, 2));
        assertNumAdmissions(admissionCriteria, 1);
        createTransferEncounter(DateUtils.addHours(visitDate, 4));
        assertNumAdmissions(admissionCriteria, 0);
        admissionCriteria.addCurrentInpatientLocation(transferLocation);
        assertNumAdmissions(admissionCriteria, 1);
    }

    @Test
    public void shouldGetAdmissionForPatient() {
        admissionCriteria.addPatientId(patient.getPatientId());
        assertNumAdmissions(admissionCriteria, 0);
        createAdmissionEncounter(DateUtils.addHours(visitDate, 2));
        assertNumAdmissions(admissionCriteria, 1);
        admissionCriteria.setPatientIds(Collections.singletonList(patient.getPatientId() + 1));
        assertNumAdmissions(admissionCriteria, 0);
    }

    @Test
    public void shouldGetAdmissionForVisit() {
        admissionCriteria.addVisitId(visit.getVisitId());
        assertNumAdmissions(admissionCriteria, 0);
        createAdmissionEncounter(DateUtils.addHours(visitDate, 2));
        assertNumAdmissions(admissionCriteria, 1);
        admissionCriteria.setVisitIds(Collections.singletonList(visit.getVisitId() + 1));
        assertNumAdmissions(admissionCriteria, 0);
    }

    @Test
    public void shouldGetAdmissionThatHasBeenDischarged() {
        admissionCriteria.addVisitId(visit.getVisitId());
        assertNumAdmissions(admissionCriteria, 0);
        createAdmissionEncounter(DateUtils.addHours(visitDate, 2));
        assertNumAdmissions(admissionCriteria, 1);
        createDischarge(DateUtils.addHours(visitDate, 4), admissionLocation);
        assertNumAdmissions(admissionCriteria, 0);
        admissionCriteria.setIncludeDischarged(true);
        assertNumAdmissions(admissionCriteria, 1);
    }

    @Test
    public void shouldNotGetAdmissionsForEndedVisits() {
        assertNumAdmissions(admissionCriteria, 0);
        createAdmissionEncounter(DateUtils.addHours(visitDate, 2));
        assertNumAdmissions(admissionCriteria, 1);
        testDataManager.getVisitService().endVisit(visit, DateUtils.addHours(visitDate, 4));
        assertNumAdmissions(admissionCriteria, 0);
    }

    @Test
    public void shouldGetInpatientRequestsAssociatedWithAdmission() {
        assertNumAdmissions(admissionCriteria, 0);
        createAdmissionRequest(DateUtils.addHours(visitDate, 2));
        createAdmissionEncounter(DateUtils.addHours(visitDate, 3));
        List<InpatientAdmission> l = assertNumAdmissions(admissionCriteria, 1);
        assertNull(l.get(0).getCurrentInpatientRequest());
        Obs transferRequest = createTransferRequest(DateUtils.addHours(visitDate, 4));
        l = assertNumAdmissions(admissionCriteria, 1);
        assertNotNull(l.get(0).getCurrentInpatientRequest());
        assertThat(l.get(0).getCurrentInpatientRequest().getDispositionObsGroup(), equalTo(transferRequest));
        Obs dischargeRequest = createDischargeRequest(DateUtils.addHours(visitDate, 5), admissionLocation);
        l = assertNumAdmissions(admissionCriteria, 1);
        assertNotNull(l.get(0).getCurrentInpatientRequest());
        assertThat(l.get(0).getCurrentInpatientRequest().getDispositionObsGroup(), equalTo(dischargeRequest));
        createTransferEncounter(DateUtils.addHours(visitDate, 6));
        l = assertNumAdmissions(admissionCriteria, 1);
        assertNull(l.get(0).getCurrentInpatientRequest());
    }

}
