package org.openmrs.module.emrapi.diagnosis;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.test.MockMetadataTestUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class DiagnosisServiceTest {

    private DiagnosisServiceImpl service;

    private EmrApiProperties emrApiProperties;
    private ConceptService conceptService;
    private ObsService obsService;
    private EncounterService encounterService;

    private Concept codedDiagnosis;
    private Concept primary;
    private Concept secondary;
    private Concept confirmed;
    private Concept presumed;
    private Concept diagnosisCertainty;
    private Concept diagnosisOrder;
    private Concept diagnosisGroupingConcept;

    @Before
    public void setUp() throws Exception {
        emrApiProperties = mock(EmrApiProperties.class);
        conceptService = mock(ConceptService.class);
        obsService = mock(ObsService.class);


        encounterService = mock(EncounterService.class);
        when(encounterService.saveEncounter(any(Encounter.class))).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return invocationOnMock.getArguments()[0];
            }
        });

        DiagnosisServiceImpl service = new DiagnosisServiceImpl();
        service.setEmrApiProperties(emrApiProperties);
        service.setObsService(obsService);
        service.setEncounterService(encounterService);
        this.service = service;

        ConceptMapType sameAs = new ConceptMapType();
        ConceptSource emrConceptSource = new ConceptSource();
        emrConceptSource.setName(EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
        codedDiagnosis = buildConcept(7, "Diagnosis (Coded)");
        primary = buildConcept(4, "Primary");
        primary.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(emrConceptSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_PRIMARY, null), sameAs));
        secondary = buildConcept(5, "Secondary");
        secondary.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(emrConceptSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_SECONDARY, null), sameAs));

        confirmed = buildConcept(11, "Confirmed");
        confirmed.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(emrConceptSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY_CONFIRMED, null), sameAs));

        presumed = buildConcept(12, "Presumed");
        presumed.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(emrConceptSource, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY_PRESUMED, null), sameAs));
        diagnosisCertainty = buildConcept(10, "Diagnosis Certainty");
        diagnosisCertainty.addAnswer(new ConceptAnswer(confirmed));
        diagnosisCertainty.addAnswer(new ConceptAnswer(presumed));

        diagnosisOrder = buildConcept(6, "Diagnosis Order");
        diagnosisOrder.addAnswer(new ConceptAnswer(primary));
        diagnosisOrder.addAnswer(new ConceptAnswer(secondary));
        diagnosisGroupingConcept = buildConcept(9, "Grouping for Diagnosis");
        diagnosisGroupingConcept.addSetMember(diagnosisOrder);


    }

    private Matcher<? super Obs> hasGroupMember(final Concept question, final Object answer, final boolean isVoided) {
        return new ArgumentMatcher<Obs>() {
            @Override
            public boolean matches(Object argument) {
                Obs actualGroup = (Obs) argument;
                return CoreMatchers.hasItem(new ArgumentMatcher<Obs>() {
                    @Override
                    public boolean matches(Object argument) {
                        Obs actual = (Obs) argument;
                        return actual.getConcept().equals(question) &&
                                actual.isVoided() == isVoided &&
                                (answer instanceof Concept && actual.getValueCoded().equals(answer)
                                        || answer instanceof String && actual.getValueText().equals(answer));
                    }
                }).matches(actualGroup.getGroupMembers(true));
            }
        };
    }

    private Concept buildConcept(int conceptId, String name) {
        Concept concept = new Concept();
        concept.setConceptId(conceptId);
        concept.addName(new ConceptName(name, Locale.ENGLISH));
        return concept;
    }


    @Test
    public void codeNonCodedDiagnosis_should_editExistingObsGroup() throws Exception {
        String nonCodedDiagnosis = "pain";
        Concept malaria = buildConcept(2, "Malaria");
        Diagnosis malariaDiagnosis = new Diagnosis(new CodedOrFreeTextAnswer(malaria), Diagnosis.Order.PRIMARY);
        List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
        diagnoses.add(malariaDiagnosis);

        MockMetadataTestUtil.setupMockConceptService(conceptService, emrApiProperties);
        MockMetadataTestUtil.setupDiagnosisMetadata(emrApiProperties, conceptService);

        //create an Obs with a non-coded diagnosis
        Diagnosis diagnosis = new Diagnosis(new CodedOrFreeTextAnswer(nonCodedDiagnosis), Diagnosis.Order.PRIMARY);
        diagnosis.setCertainty(Diagnosis.Certainty.PRESUMED);

        DiagnosisMetadata dmd = emrApiProperties.getDiagnosisMetadata();
        Obs obs = dmd.buildDiagnosisObsGroup(diagnosis);

        Encounter encounter = new Encounter();
        obs.setEncounter(encounter);

        when(obsService.saveObs(obs, "code a diagnosis")).thenReturn(obs);
        //change the non-coded diagnosis to a coded diagnosis and update the Obs

        List<Obs> codedObs = service.codeNonCodedDiagnosis(obs, diagnoses);
        for (Obs codedOb : codedObs) {
            if (codedOb.getConcept().equals(dmd.getCodedDiagnosisConcept())){
                assertThat(codedOb.getValueCoded(), is(malaria));
            }
        }
    }



}
