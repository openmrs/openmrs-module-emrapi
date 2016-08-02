package org.openmrs.module.emrapi.adt.reporting.evaluator;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.EncounterRole;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.contrib.testdata.TestDataManager;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.reporting.definition.MostRecentAdmissionRequestVisitDataDefinition;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.module.metadatamapping.api.MetadataMappingService;
import org.openmrs.module.reporting.data.visit.EvaluatedVisitData;
import org.openmrs.module.reporting.data.visit.service.VisitDataService;
import org.openmrs.module.reporting.evaluation.context.VisitEvaluationContext;
import org.openmrs.module.reporting.query.visit.VisitIdSet;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MostRecentAdmissionRequestVisitDataEvaluatorTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private VisitDataService visitDataService;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private DispositionService dispositionService;

    @Autowired
    private TestDataManager testDataManager;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    private EmrConceptService emrConceptService;

    private DispositionDescriptor dispositionDescriptor;

    private DiagnosisMetadata diagnosisMetadata;

    MostRecentAdmissionRequestVisitDataDefinition def;

    VisitEvaluationContext context;

    @Before
    public void setup() throws Exception {
        executeDataSet("baseTestDataset.xml");
        def = new MostRecentAdmissionRequestVisitDataDefinition();
        dispositionDescriptor = ContextSensitiveMetadataTestUtils.setupDispositionDescriptor(conceptService, dispositionService);
        diagnosisMetadata = ContextSensitiveMetadataTestUtils.setupDiagnosisMetadata(conceptService, emrApiProperties);
        context = new VisitEvaluationContext();
    }

    @Test
    public void shouldReturnMostRecentAdmissionRequestForVisit() throws Exception {

        EncounterRole encounterRole = testDataManager.getEncounterService().getEncounterRole(1);
        Patient patient = testDataManager.randomPatient().save();

        Concept firstCodedDiagnosisConcept = conceptService.getConcept(3);
        Concept secondCodedDiagnosisConcept = conceptService.getConcept(4);

        Provider mostRecentProvider = testDataManager.randomProvider().save();
        Location mostRecentLocation = testDataManager.getLocationService().getLocation(1);
        Date mostRecentDate = new DateTime(2014,1,1,12,0,0).toDate();

        Provider otherProvider = testDataManager.randomProvider().save();
        Location otherLocation = testDataManager.getLocationService().getLocation(2);
        Date otherDate = new DateTime(2014,1,1,11,0,0).toDate();

        Location mostRecentAdmissionLocation = testDataManager.getLocationService().getLocation(3);
        Location otherAdmissionLocation = testDataManager.getLocationService().getLocation(1);

        // a visit with two visit note encounter with dispo = ADMIT and some diagnoses
        Visit visit =
                testDataManager.visit()
                        .patient(patient)
                        .visitType(emrApiProperties.getAtFacilityVisitType())
                        .started(new Date())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(mostRecentDate)
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .provider(encounterRole, mostRecentProvider)
                                .location(mostRecentLocation)
                                .obs(testDataManager.obs()
                                        .concept(diagnosisMetadata.getDiagnosisSetConcept())
                                        .obs(testDataManager.obs()
                                                .concept(diagnosisMetadata.getCodedDiagnosisConcept())
                                                .value(firstCodedDiagnosisConcept)
                                                .get())
                                        .obs(testDataManager.obs()
                                                .concept(diagnosisMetadata.getDiagnosisOrderConcept())
                                                .value("Primary", "org.openmrs.module.emrapi")
                                                .get())
                                        .get())
                                .obs(testDataManager.obs()
                                        .concept(diagnosisMetadata.getDiagnosisSetConcept())
                                        .obs(testDataManager.obs()
                                                .concept(diagnosisMetadata.getCodedDiagnosisConcept())
                                                .value(secondCodedDiagnosisConcept)
                                                .get())
                                        .obs(testDataManager.obs()
                                                .concept(diagnosisMetadata.getDiagnosisOrderConcept())
                                                .value("Secondary", "org.openmrs.module.emrapi")
                                                .get())
                                        .get())
                                .obs(testDataManager.obs()
                                        .concept(dispositionDescriptor.getDispositionSetConcept())
                                        .obs(testDataManager.obs()
                                                .concept(dispositionDescriptor.getDispositionConcept())
                                                .value("Admit to hospital", "org.openmrs.module.emrapi")
                                                .get())
                                        .obs(testDataManager.obs()
                                                .concept(dispositionDescriptor.getAdmissionLocationConcept())
                                                .value("3")
                                                .get())
                                        .get())
                                .obs(testDataManager.obs()
                                        .concept(diagnosisMetadata.getDiagnosisSetConcept())
                                        .obs(testDataManager.obs()
                                                .concept(diagnosisMetadata.getNonCodedDiagnosisConcept())
                                                .value("some diagnosis")
                                                .get())
                                        .obs(testDataManager.obs()
                                                .concept(diagnosisMetadata.getDiagnosisOrderConcept())
                                                .value("Primary", "org.openmrs.module.emrapi")
                                                .get())
                                        .get())

                                .get())
                        .encounter(testDataManager.encounter()
                                .patient(patient)
                                .encounterDatetime(otherDate)
                                .encounterType(emrApiProperties.getVisitNoteEncounterType())
                                .provider(encounterRole, otherProvider)
                                .location(otherLocation)
                                .obs(testDataManager.obs()
                                        .concept(diagnosisMetadata.getDiagnosisSetConcept())
                                        .obs(testDataManager.obs()
                                                .concept(diagnosisMetadata.getCodedDiagnosisConcept())
                                                .value(secondCodedDiagnosisConcept)
                                                .get())
                                        .obs(testDataManager.obs()
                                                .concept(diagnosisMetadata.getDiagnosisOrderConcept())
                                                .value("Primary", "org.openmrs.module.emrapi")
                                                .get())
                                        .get())
                                .obs(testDataManager.obs()
                                    .concept(dispositionDescriptor.getDispositionSetConcept())
                                    .obs(testDataManager.obs()
                                            .concept(dispositionDescriptor.getDispositionConcept())
                                            .get())
                                    .obs(testDataManager.obs()
                                            .concept(dispositionDescriptor.getAdmissionLocationConcept())
                                            .value("1")
                                            .get())
                                    .get())
                                .get())
                        .save();

        new VisitIdSet(visit.getId());

        EvaluatedVisitData data = visitDataService.evaluate(def, context);

        assertThat(data.getData().size(), is(1));
        assertThat( (Date) ((Map<String,Object>) data.getData().get(visit.getId())).get("datetime"), is(mostRecentDate));
        assertThat( (Provider) ((Map<String,Object>) data.getData().get(visit.getId())).get("provider"), is(mostRecentProvider));
        assertThat( (Location) ((Map<String,Object>) data.getData().get(visit.getId())).get("fromLocation"), is(mostRecentLocation));
        assertThat( (Location) ((Map<String,Object>) data.getData().get(visit.getId())).get("toLocation"), is(mostRecentAdmissionLocation));

        List<Diagnosis> diagnoses = (List<Diagnosis>) ((Map<String,Object>) data.getData().get(visit.getId())).get("diagnoses");
        assertThat(diagnoses.size(), is(2));

        // hacky work-around for the fact the diagnoses could come back in either order
        if (diagnoses.get(0).getDiagnosis().getCodedAnswer() != null) {
            assertThat(diagnoses.get(0).getDiagnosis().getCodedAnswer(), is(firstCodedDiagnosisConcept));
            assertThat(diagnoses.get(1).getDiagnosis().getNonCodedAnswer(), is("some diagnosis"));

        }
        else {
            assertThat(diagnoses.get(1).getDiagnosis().getCodedAnswer(), is(firstCodedDiagnosisConcept));
            assertThat(diagnoses.get(0).getDiagnosis().getNonCodedAnswer(), is("some diagnosis"));
        }

    }


}
