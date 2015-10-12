package org.openmrs.module.emrapi.encounter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSource;
import org.openmrs.User;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.test.builder.ConceptBuilder;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LocaleUtility.class, Context.class})
public class ConceptMapperTest {

    @Mock
    private User authenticatedUser;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        mockStatic(LocaleUtility.class);
        mockStatic(Context.class);
        Mockito.when(LocaleUtility.getLocalesInOrder()).thenReturn(new HashSet<Locale>());
        Mockito.when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);
    }

    @Test
    public void should_convert_concept_to_encounter_concept() {
        ConceptMapper conceptMapper = new ConceptMapper();

        ConceptBuilder conceptBuilder = new ConceptBuilder(null, new ConceptDatatype(), new ConceptClass());
        ConceptSource source = new ConceptSource();
        source.setName("PACS Procedure Code");
        conceptBuilder.addName("Test concept");
        conceptBuilder.addMapping(new ConceptMapType(), source, "122");

        EncounterTransaction.Concept encounterTransactionConcept = conceptMapper.map(conceptBuilder.get());

        Assert.assertEquals("[{source=PACS Procedure Code, name=null, code=122}]", encounterTransactionConcept.getMappings().toString());
    }

    @Test
    public void should_use_locale_specific_short_name_if_available() throws Exception {
        Mockito.when(authenticatedUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE)).thenReturn("fr");

        ConceptMapper conceptMapper = new ConceptMapper();

        ConceptBuilder conceptBuilder = new ConceptBuilder(null, new ConceptDatatype(), new ConceptClass());
        ConceptName conceptNameInFrench = new ConceptName("French Test Name", Locale.FRENCH);
        conceptNameInFrench.setConceptNameType(ConceptNameType.SHORT);

        ConceptName conceptNameInEnglish = new ConceptName("English Test Name", Locale.ENGLISH);
        conceptNameInEnglish.setConceptNameType(ConceptNameType.SHORT);

        conceptBuilder.add(conceptNameInEnglish);
        conceptBuilder.add(conceptNameInFrench);

        EncounterTransaction.Concept encounterTransactionConcept = conceptMapper.map(conceptBuilder.get());

        Assert.assertEquals("French Test Name", encounterTransactionConcept.getShortName());
    }

    @Test
    public void should_use_first_short_name_if_locale_specific_is_not_available() throws Exception {
        Mockito.when(authenticatedUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE)).thenReturn("fr");

        ConceptMapper conceptMapper = new ConceptMapper();

        ConceptBuilder conceptBuilder = new ConceptBuilder(null, new ConceptDatatype(), new ConceptClass());
        ConceptName conceptNameInEnglish = new ConceptName("English Test Name Full", Locale.ENGLISH);

        ConceptName shortConceptNameInEnglish = new ConceptName("English Test Name", Locale.ENGLISH);
        shortConceptNameInEnglish.setConceptNameType(ConceptNameType.SHORT);

        conceptBuilder.add(conceptNameInEnglish);
        conceptBuilder.add(shortConceptNameInEnglish);

        EncounterTransaction.Concept encounterTransactionConcept = conceptMapper.map(conceptBuilder.get());

        Assert.assertEquals("English Test Name", encounterTransactionConcept.getShortName());
    }
}
