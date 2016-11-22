package org.openmrs.module.emrapi.visit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertThat;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.VisitService;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.contrib.testdata.builder.VisitBuilder;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class VisitDomainWrapperComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EmrConceptService emrConceptService;

    @Autowired
    private DispositionService dispositionService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    private TestDataManager testDataManager;

    @Autowired
    private DomainWrapperFactory factory;

    @Autowired
    private VisitService visitService;

    private DispositionDescriptor dispositionDescriptor;

    @Before
    public void setup() throws Exception {
        executeDataSet("baseTestDataset.xml");
        dispositionDescriptor = ContextSensitiveMetadataTestUtils.setupDispositionDescriptor(conceptService, dispositionService);
        ContextSensitiveMetadataTestUtils.setupAdmissionDecisionConcept(conceptService, emrApiProperties);
        ContextSensitiveMetadataTestUtils.setupDiagnosisMetadata(conceptService, emrApiProperties);
    }

    @Test
    public void testThatBeanCanHavePropertiesAutowired() throws Exception {
        VisitDomainWrapper visitDomainWrapper = factory.newVisitDomainWrapper();
        assertThat(visitDomainWrapper.emrApiProperties, notNullValue());
    }

    @Test
    public void isAwaitingAdmission_shouldReturnTrueIfVisitAwaitingAdmission() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        Location visitLocation = testDataManager.location().name("Visit Location")
                .tag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS).save();

        // a visit with a single visit note encounter with dispo = ADMIT
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .location(visitLocation)
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

        VisitDomainWrapper visitDomainWrapper = factory.newVisitDomainWrapper(visit);
        assertThat(visitDomainWrapper.isAwaitingAdmission(), is(true));
    }

    @Test
    public void isAwaitingAdmission_shouldReturnFalseIfVisitNotAwaitingAdmission() throws Exception {

        Patient patient = testDataManager.randomPatient().save();

        Location visitLocation = testDataManager.location().name("Visit Location")
                .tag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS).save();

        // a visit with a single *voided* visit note encounter with dispo = ADMIT
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .location(visitLocation)
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

        VisitDomainWrapper visitDomainWrapper = factory.newVisitDomainWrapper(visit);
        assertThat(visitDomainWrapper.isAwaitingAdmission(), is(false));
    }

    @Test
    public void getVisitAttribute_shouldReturnNullIfNoVisitAttributes() {
        Visit visit = visitService.getVisit(1); // from standard test dataset
        assertNull(factory.newVisitDomainWrapper(visit).getVisitAttribute("Visit Template"));
    }

    @Test
    public void getVisitTemplate_shouldReturnVisitAttributeMatchedByName() {
        Visit visit = visitService.getVisit(2); // from standard test dataset
         assertThat(factory.newVisitDomainWrapper(visit).getVisitAttribute("Visit Template").toString(), is("pedsInitialOutpatient"));
    }

    @Test
    public void getVisitTemplate_shouldReturnVisitAttributeMatchedByUuid() {
        Visit visit = visitService.getVisit(2); // from standard test dataset
        assertThat(factory.newVisitDomainWrapper(visit).getVisitAttribute("f7b07c80-27c3-49de-8830-cb9e3e805eeb").toString(), is("pedsInitialOutpatient"));
    }

    @Test
    public void getVisitTemplate_shouldReturnNullIfNoMatchingVisitAttributes() {
        Visit visit = visitService.getVisit(2); // from standard test dataset
        assertNull(factory.newVisitDomainWrapper(visit).getVisitAttribute("Non-existent attribute"));
    }
    
    protected List<Obs> createRandomDiagnosisObsGroups(int count, int diagnosisNamesCount, DiagnosisMetadata diagnosisMetadata) {
       assertThat(count, greaterThan(0));
       assertThat(diagnosisNamesCount, greaterThan(0));
       
       // Generating the names (as "Diagnosis 1", "Diagnosis 2", ... etc)
       List<String> diagnosisNames = new ArrayList<String>();
       for (int i = 1; i <= diagnosisNamesCount; i++) {
          diagnosisNames.add("Diagnosis " + i);
       }
       
       List<Obs> diagnoses = new ArrayList<Obs>();
       final Random rand = new Random();
       for (int i = 0; i < count; i++) {
          String diagnosisName = diagnosisNames.get(rand.nextInt(diagnosisNames.size()));
          Diagnosis.Order order = Diagnosis.Order.values()[rand.nextInt(Diagnosis.Order.values().length)];
          Diagnosis.Certainty certainty = Diagnosis.Certainty.values()[rand.nextInt(Diagnosis.Certainty.values().length)];
          
          diagnoses.add( diagnosisMetadata.buildDiagnosisObsGroup( new Diagnosis(new CodedOrFreeTextAnswer(diagnosisName), order, certainty) ) );
       }
       return diagnoses;
    }
    
    /**
     * @param count The number of random encounters to generate.
     * @param density The percentage of encounters containing diagnoses obs.
     * @param diagnosisInEncounter Average number of diagnoses (for encounters that are added diagnoses).
     * @param visitStartDate Encounters need to occur after the visit start date.
     * @param patient
     * @param encounterType
     * @param diagnosisList A sample of diagnosis obs groups to be randomly "recorded" within the encounters.
     * @return
     * @throws ParseException
     */
    protected List<Encounter> createRandomEncountersWithDiagnoses(int count, double density, int diagnosisInEncounter, Date visitStartDate, Patient patient, EncounterType encounterType, List<Obs> diagnosisList) throws ParseException {
       assertThat(density, allOf(greaterThan(0.),lessThanOrEqualTo(1.)));
       assertThat(count, greaterThan(0));
       
       List<Encounter> encounters = new ArrayList<Encounter>();
       
       // http://stackoverflow.com/a/11016689/321797
       long startMilli = visitStartDate.getTime();
       final long hourInMilli = 1000 * 60 * 60;
       final long yearInMilli = hourInMilli * 24 * 365 + 1000; // Have to account for the leap second!
       
       Random rand = new Random();
       
       for (int i = 0; i < count; i++) {
          Encounter e = testDataManager.encounter()
                         .patient(patient)
                         .encounterDatetime(new Date(startMilli + Math.round(yearInMilli * Math.random()))) // An encounter in 'startYear'
                         .encounterType(encounterType)
                         .get();
          
          if (Math.random() <= density) {
          // then we add at least 1 diagnosis
             int diagnosesCount = 1 + rand.nextInt(diagnosisInEncounter);
             long lastTime = e.getEncounterDatetime().getTime();
             for (int j = 0; j < diagnosesCount; j++) {
                Obs obs = Obs.newInstance( diagnosisList.get(rand.nextInt(diagnosisList.size())) );
                obs.setObsDatetime( new Date(lastTime + Math.round(2 * hourInMilli * Math.random())) );   // Obs in the encounter are recorded around 2 hours apart
                e.addObs(obs);
                lastTime = obs.getObsDatetime().getTime();
             }
          }
          encounters.add(e);
       }
       
       return encounters;
    }
    
    public enum Implementation {
       CURRENT,
       LEGACY
    }
    
    @Test
    public void getUniqueDiagnoses_shouldConvergeWithLegacyImpl() throws ParseException {
       
       /*
        * Setup
        */
       
       Patient patient = testDataManager.randomPatient().save();

       Location visitLocation = testDataManager.location().name("Visit Location")
               .tag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS).save();
       
       DiagnosisMetadata diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
       List<Obs> sampleDiagnoses = createRandomDiagnosisObsGroups(200, 125, diagnosisMetadata);
       
       final Date visitStartDate = new DateTime(2012, 1, 1, 0, 0, 0).toDate();
       VisitBuilder visitBuilder =
             testDataManager.visit()
                     .patient(patient)
                     .visitType(emrApiProperties.getAtFacilityVisitType())
                     .started(visitStartDate)
                     .location(visitLocation);
       
       // Adding a bunch of encounters to the test visit
       List<Encounter> encounters = createRandomEncountersWithDiagnoses(50, 0.15, 2, visitStartDate, patient, emrApiProperties.getVisitNoteEncounterType(), sampleDiagnoses);
       for (Encounter e : encounters) {
          visitBuilder.encounter(e);
       }
       VisitDomainWrapper visitDomainWrapper = factory.newVisitDomainWrapper( visitBuilder.save() );
       
       /*
        * Replay & Asserts
        */
       
       List<Diagnosis> diagnoses;
       List<Diagnosis> diagnosesLegacy;
       
       diagnoses = getUniqueDiagnoses(visitDomainWrapper, false, false, Implementation.CURRENT);
       diagnosesLegacy = getUniqueDiagnoses(visitDomainWrapper, false, false, Implementation.LEGACY);
       assertThatSameUniqueDiagnoses(diagnoses, diagnosesLegacy);
       
       diagnoses = getUniqueDiagnoses(visitDomainWrapper, true, false, Implementation.CURRENT);
       diagnosesLegacy = getUniqueDiagnoses(visitDomainWrapper, true, false, Implementation.LEGACY);
       assertThatSameUniqueDiagnoses(diagnoses, diagnosesLegacy);

       diagnoses = getUniqueDiagnoses(visitDomainWrapper, false, true, Implementation.CURRENT);
       diagnosesLegacy = getUniqueDiagnoses(visitDomainWrapper, false, true, Implementation.LEGACY);
       assertThatSameUniqueDiagnoses(diagnoses, diagnosesLegacy);

       diagnoses = getUniqueDiagnoses(visitDomainWrapper, true, true, Implementation.CURRENT);
       diagnosesLegacy = getUniqueDiagnoses(visitDomainWrapper, true, true, Implementation.LEGACY);
       assertThatSameUniqueDiagnoses(diagnoses, diagnosesLegacy);
    }
    
    /*
     * Wrapper to log execution time
     */
    private List<Diagnosis> getUniqueDiagnoses(VisitDomainWrapper visitDomainWrapper, Boolean primaryOnly, Boolean confirmedOnly, Implementation impl) {
       List<Diagnosis> res = null;
       String whichOne = "";
       long time0 = System.currentTimeMillis();
       switch (impl) {
       case CURRENT:
          res = visitDomainWrapper.getUniqueDiagnoses(primaryOnly, confirmedOnly);
          whichOne = impl.name();
          break;
       case LEGACY:
          res = visitDomainWrapper.getUniqueDiagnosesLegacy(primaryOnly, confirmedOnly);
          whichOne = impl.name();
          break;
       default:
          Assert.fail("Invalid implementation specified.");
       }
       long time1 = System.currentTimeMillis();
       System.out.println(whichOne + " getUniqueDiagnoses(..) | primaryOnly=" + primaryOnly.toString() + ", confirmedOnly=" + confirmedOnly.toString());
       System.out.println("time (msec): " + (time1 - time0));
       System.out.println();
       return res;
    }
    
    private static void assertThatSameUniqueDiagnoses(List<Diagnosis> actualDiagnoses, List<Diagnosis> expectedDiagnoses) {
       assertEquals(expectedDiagnoses.size(), actualDiagnoses.size());
       Set<CodedOrFreeTextAnswer> actualAnswers = new HashSet<CodedOrFreeTextAnswer>();
       Set<CodedOrFreeTextAnswer> expectedAnswers = new HashSet<CodedOrFreeTextAnswer>();
       // What matters is that inner 'CodedOrFreeTextAnswer' instances are reported as being the same sets 
       for (int i = 0; i < expectedDiagnoses.size(); i++) {
          actualAnswers.add(actualDiagnoses.get(i).getDiagnosis());
          expectedAnswers.add(expectedDiagnoses.get(i).getDiagnosis());
       }
       assertEquals(actualAnswers, expectedAnswers);
    }
}