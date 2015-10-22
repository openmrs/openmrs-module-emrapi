package org.openmrs.module.emrapi.encounter.mapper;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.internal.util.collections.Sets;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.test.builder.ConceptDataTypeBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ObsMapperTest {

    @Mock
    private ConceptService conceptService;

    @Mock
    private ObsService obsService;
    @Mock
    private DiagnosisMetadata diagnosisMetadata;
    @Mock
    private EmrApiProperties emrApiProperties;

    @Mock
    private OrderService orderService;

    @Mock
    private Encounter encounter;

    @Mock
    private Patient patient;

    private ObsMapper obsMapper = null;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        obsMapper = new ObsMapper(conceptService, emrApiProperties, obsService, orderService);
        when(emrApiProperties.getDiagnosisMetadata()).thenReturn(diagnosisMetadata);
        when(encounter.getPatient()).thenReturn(patient);
    }

    @Test
    public void shouldTransformGivenETObsToObs() throws ParseException {
        //arrange
        Concept numericConcept = newConcept(new ConceptDataTypeBuilder().numeric(), "numeric-concept-uuid");
        Date observationDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse("2005-01-01T00:00:00.000+0000");
        EncounterTransaction.Observation etObs = new EncounterTransaction.Observation().setUuid("o-uuid").setValue(35.0).setComment("overweight").setObservationDateTime(observationDateTime).setConcept(newEtConcept("ET_CONCEPT"));
        when(conceptService.getConceptByUuid(etObs.getConceptUuid())).thenReturn(numericConcept);

        //act
        Obs obs = this.obsMapper.transformEtObs(encounter,null, etObs);

        //assert
        assertEquals(new Double(35.0), obs.getValueNumeric());
        assertEquals("overweight", obs.getComment());
        assertEquals(observationDateTime, obs.getObsDatetime());
        assertEquals(patient, obs.getPerson());
    }

    @Test
    public void shouldVoidObs() throws ParseException {
        //arrange
        Concept numericConcept = newConcept(new ConceptDataTypeBuilder().numeric(), "numeric-concept-uuid");
        Date observationDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse("2005-01-01T00:00:00.000+0000");
        EncounterTransaction.Observation etObs = new EncounterTransaction.Observation().setUuid("o-uuid").setVoided(true).setValue("").setComment("overweight").setObservationDateTime(observationDateTime).setConcept(newEtConcept("ET_CONCEPT"));
        when(conceptService.getConceptByUuid(etObs.getConceptUuid())).thenReturn(numericConcept);

        //act
        Obs obs = this.obsMapper.transformEtObs(encounter,null, etObs);

        //assert
        assertEquals(true, obs.getVoided());
        assertEquals(patient, obs.getPerson());
    }


    @Test
    public void shouldReturnMatchingObs() {
        //arrange
        Obs obs1 = new Obs();
        obs1.setUuid("o1_uuid");
        obs1.setConcept(newConcept(new ConceptDataTypeBuilder().numeric(), "numeric-concept-uuid"));

        Obs obs2 = new Obs();
        obs2.setUuid("o2_uuid");
        obs2.setConcept(newConcept(new ConceptDataTypeBuilder().numeric(), "numeric-concept-uuid"));

        Set<Obs> obsSet = Sets.newSet(obs1, obs2);

        //act
        Obs obs = this.obsMapper.getMatchingObservation(obsSet, "o2_uuid");

        //assert
        assertEquals(obs2, obs);
    }

    private Concept newConcept(ConceptDatatype conceptDatatype, String conceptUuid) {
        Concept concept = new Concept();
        concept.setDatatype(conceptDatatype);
        concept.setUuid(conceptUuid);
        when(conceptService.getConceptByUuid(conceptUuid)).thenReturn(concept);
        return concept;
    }

    private EncounterTransaction.Concept newEtConcept(String conceptUuid) {
        return new EncounterTransaction.Concept(conceptUuid, "concept_name");
    }

}
