package org.openmrs.module.emrapi.visit;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

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
    private VisitDomainWrapperFactory factory;

    private DispositionDescriptor dispositionDescriptor;

    @Before
    public void setup() throws Exception {
        executeDataSet("baseTestDataset.xml");
        dispositionDescriptor = ContextSensitiveMetadataTestUtils.setupDispositionDescriptor(conceptService, dispositionService);
        ContextSensitiveMetadataTestUtils.setupAdmissionDecisionConcept(conceptService, emrApiProperties);

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
}
