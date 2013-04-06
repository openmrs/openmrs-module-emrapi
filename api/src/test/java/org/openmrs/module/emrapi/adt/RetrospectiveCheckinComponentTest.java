package org.openmrs.module.emrapi.adt;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

// Probably delete this test class, and its associated resources
@Ignore("This was never completed, and it isn't possible to easily migrate from the EMR module due to PIH-specific data")
@RunWith(SpringJUnit4ClassRunner.class)
public class RetrospectiveCheckinComponentTest extends BaseModuleContextSensitiveTest {

    /*
    @Autowired
    private AdtService adtService;
    @Autowired
    private EmrProperties emrProperties;

    @Autowired
    private PatientService patientService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private ProviderService providerService;
    @Autowired
    private ConceptService conceptService;
    @Autowired
    private VisitService visitService;

    private Patient patient;
    private Location location;
    private Provider clerk;
    private Obs paymentReason;
    private Obs paymentAmount;
    private Obs paymentReceipt;
    private Date checkinDate;

    @Before
    public void setupDatabase() throws Exception {
        executeDataSet("retrospectiveCheckinComponentTestDataset.xml");

        patient = patientService.getPatient(2);
        location = locationService.getLocation(2);
        clerk = providerService.getProvider(1);
        paymentReason = createPaymentReasonObservation();
        paymentAmount = createPaymentAmountObservation(50);
        paymentReceipt = createPaymentReceiptObservation("123456");
    }

    @Test
    public void createRetrospectiveCheckinWithinNewVisit() {
        checkinDate = generateDateFor(2011, 07, 25, 10, 39);

        Encounter checkinEncounter = adtService.createCheckinInRetrospective(patient, location, clerk, paymentReason, paymentAmount, paymentReceipt, checkinDate);
        Visit visit = checkinEncounter.getVisit();

        assertCheckinEncounter(checkinEncounter);
        assertThat(visit.getStartDatetime(), is(checkinDate));
    }

    private Obs createPaymentAmountObservation(double amount) {
        Obs paymentAmount = new Obs();
        paymentAmount.setConcept(emrProperties.getPaymentAmountConcept());
        paymentAmount.setValueNumeric(amount);
        return paymentAmount;
    }

    private Obs createPaymentReasonObservation() {
        Obs paymentReason = new Obs();
        paymentReason.setConcept(emrProperties.getPaymentReasonsConcept());
        paymentReason.setValueCoded(conceptService.getConcept(16));
        return paymentReason;
    }

    private Date generateDateFor(int year, int month, int day, int hour, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minutes);
        return calendar.getTime();
    }

    private Obs createPaymentReceiptObservation(String receiptNumber) {
        Obs pr = new Obs();
        pr.setConcept(emrProperties.getPaymentReceiptNumberConcept());
        pr.setValueText(receiptNumber);

        return pr;
    }

    private void assertCheckinEncounter(Encounter checkinEncounter) {
        assertThat(checkinEncounter.getPatient(), is(patient));
        assertThat(checkinEncounter.getLocation(), is(location));
        assertThat(checkinEncounter.getProvidersByRole(emrProperties.getCheckInClerkEncounterRole()), containsInAnyOrder(clerk));
        assertThat(checkinEncounter.getObs(), containsInAnyOrder(paymentReason, paymentAmount, paymentReceipt));
        assertThat(checkinEncounter.getAllObs().size(), is(1));
        assertThat(checkinEncounter.getAllObs().iterator().next().getGroupMembers(), containsInAnyOrder(paymentReason, paymentAmount, paymentReceipt));
        assertThat(checkinEncounter.getEncounterDatetime(), is(checkinDate));
    }
    */
}
