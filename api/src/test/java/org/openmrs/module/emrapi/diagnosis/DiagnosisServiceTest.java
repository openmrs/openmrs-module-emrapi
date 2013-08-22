package org.openmrs.module.emrapi.diagnosis;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.test.MockMetadataTestUtil;

import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DiagnosisServiceTest {

    private DiagnosisServiceImpl service;

    private EmrApiProperties emrApiProperties;
    private ConceptService conceptService;
    private ObsService obsService;

    @Before
    public void setUp() throws Exception {
        emrApiProperties = mock(EmrApiProperties.class);
        conceptService = mock(ConceptService.class);
        obsService = mock(ObsService.class);

        DiagnosisServiceImpl service = new DiagnosisServiceImpl();
        service.setEmrApiProperties(emrApiProperties);
        service.setObsService(obsService);
        this.service = service;
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

        MockMetadataTestUtil.setupMockConceptService(conceptService, emrApiProperties);
        MockMetadataTestUtil.setupDiagnosisMetadata(emrApiProperties, conceptService);

        //create an Obs with a non-coded diagnosis
        Diagnosis diagnosis = new Diagnosis(new CodedOrFreeTextAnswer(nonCodedDiagnosis), Diagnosis.Order.PRIMARY);
        diagnosis.setCertainty(Diagnosis.Certainty.PRESUMED);

        DiagnosisMetadata dmd = emrApiProperties.getDiagnosisMetadata();
        Obs obs = dmd.buildDiagnosisObsGroup(diagnosis);

        when(obsService.saveObs(obs, "code a diagnosis")).thenReturn(obs);
        //change the non-coded diagnosis to a coded diagnosis and update the Obs
        Obs codedObs = service.codeNonCodedDiagnosis(obs, malaria);

        assertThat(codedObs.getConcept(), is(dmd.getCodedDiagnosisConcept()));
        assertThat(codedObs.getValueCoded(), is(malaria));
        assertThat(codedObs.getValueText(), is(""));

    }
}
