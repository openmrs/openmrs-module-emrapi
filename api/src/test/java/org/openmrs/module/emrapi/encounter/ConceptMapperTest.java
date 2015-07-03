package org.openmrs.module.emrapi.encounter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptSource;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.test.builder.ConceptBuilder;
import org.openmrs.util.LocaleUtility;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LocaleUtility.class)
public class ConceptMapperTest {

    @Before
    public void setup() throws Exception {
        mockStatic(LocaleUtility.class);
        Mockito.when(LocaleUtility.getLocalesInOrder()).thenReturn(new HashSet<Locale>());
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

}
