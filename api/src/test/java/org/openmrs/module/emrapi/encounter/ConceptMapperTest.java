package org.openmrs.module.emrapi.encounter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.ConceptSource;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.test.builder.ConceptBuilder;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LocaleUtility.class, Context.class})
public class ConceptMapperTest {

    @Mock
    private User authenticatedUser;

    @Mock
    private AdministrationService administrationService;

    @Before
    public void setup() throws Exception {
        initMocks(this);
        mockStatic(LocaleUtility.class);
        mockStatic(Context.class);
        when(LocaleUtility.getLocalesInOrder()).thenReturn(new HashSet<Locale>());
        when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);
        when(Context.getAdministrationService()).thenReturn(administrationService);
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

        Map<String, Object> mappings = encounterTransactionConcept.getMappings().get(0);

        assertThat(mappings.size(), is(3));
        assertThat((String) mappings.get("code"), is("122"));
        assertThat(mappings.get("name"), is(nullValue()));
        assertThat((String) mappings.get("source"), is("PACS Procedure Code"));
    }

    @Test
    public void should_use_locale_specific_short_name_if_available() throws Exception {
        when(authenticatedUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE)).thenReturn("fr");
        when(LocaleUtility.fromSpecification("fr")).thenReturn(Locale.FRENCH);

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
    public void should_use_fully_specified_name_if_short_name_is_not_available_in_a_locale() throws Exception {
        when(authenticatedUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE)).thenReturn("fr");
        when(administrationService.getGlobalProperty("default_locale")).thenReturn("en");
        when(LocaleUtility.fromSpecification("fr")).thenReturn(Locale.FRENCH);
        when(LocaleUtility.fromSpecification("en")).thenReturn(Locale.ENGLISH);

        ConceptMapper conceptMapper = new ConceptMapper();

        ConceptBuilder conceptBuilder = new ConceptBuilder(null, new ConceptDatatype(), new ConceptClass());

        ConceptName fullNameInFrench = new ConceptName("French Name Full", Locale.FRENCH);
        fullNameInFrench.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);

        ConceptName fullNameInEnglish = new ConceptName("English Name Full", Locale.ENGLISH);
        fullNameInEnglish.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);

        ConceptName shortNameInEnglish = new ConceptName("English Name Short", Locale.ENGLISH);
        shortNameInEnglish.setConceptNameType(ConceptNameType.SHORT);

        conceptBuilder.add(fullNameInFrench);
        conceptBuilder.add(fullNameInEnglish);
        conceptBuilder.add(shortNameInEnglish);

        EncounterTransaction.Concept encounterTransactionConcept = conceptMapper.map(conceptBuilder.get());

        Assert.assertEquals("French Name Full", encounterTransactionConcept.getShortName());
    }

    @Test
    public void should_use_short_name_of_default_locale_if_both_fully_specified_name_and_short_name_are_not_available_in_a_locale() throws Exception {
        when(authenticatedUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE)).thenReturn("fr");
        when(administrationService.getGlobalProperty("default_locale")).thenReturn("en");
        when(LocaleUtility.fromSpecification("fr")).thenReturn(Locale.FRENCH);
        when(LocaleUtility.fromSpecification("en")).thenReturn(Locale.ENGLISH);

        ConceptMapper conceptMapper = new ConceptMapper();

        ConceptBuilder conceptBuilder = new ConceptBuilder(null, new ConceptDatatype(), new ConceptClass());

        ConceptName fullNameInEnglish = new ConceptName("English Name Full", Locale.ENGLISH);
        fullNameInEnglish.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);

        ConceptName shortNameInEnglish = new ConceptName("English Name Short", Locale.ENGLISH);
        shortNameInEnglish.setConceptNameType(ConceptNameType.SHORT);

        conceptBuilder.add(fullNameInEnglish);
        conceptBuilder.add(shortNameInEnglish);

        EncounterTransaction.Concept encounterTransactionConcept = conceptMapper.map(conceptBuilder.get());

        Assert.assertEquals("English Name Short", encounterTransactionConcept.getShortName());
    }

    @Test
    public void should_use_fully_specified_name_of_default_locale_if_both_fully_specified_name_and_short_name_are_not_available_in_a_locale_and_short_name_of_the_default_locale_is_also_not_available() throws Exception {
        when(authenticatedUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE)).thenReturn("fr");
        when(administrationService.getGlobalProperty("default_locale")).thenReturn("en");
        when(LocaleUtility.fromSpecification("fr")).thenReturn(Locale.FRENCH);
        when(LocaleUtility.fromSpecification("en")).thenReturn(Locale.ENGLISH);

        ConceptMapper conceptMapper = new ConceptMapper();

        ConceptBuilder conceptBuilder = new ConceptBuilder(null, new ConceptDatatype(), new ConceptClass());

        ConceptName fullNameInEnglish = new ConceptName("English Name Full", Locale.ENGLISH);
        fullNameInEnglish.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);

        conceptBuilder.add(fullNameInEnglish);

        EncounterTransaction.Concept encounterTransactionConcept = conceptMapper.map(conceptBuilder.get());

        Assert.assertEquals("English Name Full", encounterTransactionConcept.getShortName());
    }

    @Test
    public void should_use_any_available_name_when_no_name_in_either_default_locale_or_users_locale_is_available() throws Exception {
        when(authenticatedUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE)).thenReturn("fr");
        when(administrationService.getGlobalProperty("default_locale")).thenReturn("en");
        when(LocaleUtility.fromSpecification("fr")).thenReturn(Locale.FRENCH);
        when(LocaleUtility.fromSpecification("en")).thenReturn(Locale.ENGLISH);

        ConceptMapper conceptMapper = new ConceptMapper();

        ConceptBuilder conceptBuilder = new ConceptBuilder(null, new ConceptDatatype(), new ConceptClass());

        ConceptName fullNameInEnglish = new ConceptName("Italian Name Full", Locale.ITALIAN);
        fullNameInEnglish.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);

        conceptBuilder.add(fullNameInEnglish);

        EncounterTransaction.Concept encounterTransactionConcept = conceptMapper.map(conceptBuilder.get());

        Assert.assertEquals("Italian Name Full", encounterTransactionConcept.getShortName());
    }

    @Test
    public void should_set_hiNormal_and_lowNormal_properties_for_numeric_concept() throws Exception {
        when(authenticatedUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE)).thenReturn("fr");

        ConceptNumeric concept = new ConceptNumeric();

        ConceptName conceptName = new ConceptName();
        conceptName.setName("Temperature Data");
        concept.addName(conceptName);

        ConceptDatatype conceptDatatype = new ConceptDatatype();
        conceptDatatype.setName("Numeric");
        concept.setDatatype(conceptDatatype);

        ConceptClass conceptClass = new ConceptClass();
        conceptClass.setName("Concept Details");
        concept.setConceptClass(conceptClass);

        concept.setHiNormal((double) 20);
        concept.setLowNormal((double) 15);

        ConceptMapper conceptMapper = new ConceptMapper();
        EncounterTransaction.Concept encounterTransactionConcept = conceptMapper.map(concept);

        Assert.assertEquals((double) 20, encounterTransactionConcept.getHiNormal(), 0.0);
        Assert.assertEquals((double) 15, encounterTransactionConcept.getLowNormal(), 0.0);
    }
}
