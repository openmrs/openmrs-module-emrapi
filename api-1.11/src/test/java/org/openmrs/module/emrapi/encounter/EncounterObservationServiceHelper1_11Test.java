package org.openmrs.module.emrapi.encounter;


import org.junit.Test;
import org.mockito.Mock;

import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.mapper.ObsMapper1_11;
import org.openmrs.module.emrapi.test.builder.ConceptDataTypeBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterObservationServiceHelper1_11Test {


    public static final String TEXT_CONCEPT_UUID = "text-concept-uuid";

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
    private EncounterTransaction.Observation observation, rootObservation;

    private ObsMapper1_11 obsMapper1_11 = null;


    @Test
    public void shouldCreateNewObservationWithNamespace() throws ParseException {
        initMocks(this);
        obsMapper1_11 = new ObsMapper1_11(conceptService,emrApiProperties,obsService,orderService);
        EncounterObservationServiceHelper encounterObservationServiceHelper = new EncounterObservationServiceHelper(conceptService, emrApiProperties, obsService, orderService,obsMapper1_11);

        newConcept(new ConceptDataTypeBuilder().text(), TEXT_CONCEPT_UUID);
        List<EncounterTransaction.Observation> observations = asList(
                new EncounterTransaction.Observation()
                        .setConcept(getConcept(TEXT_CONCEPT_UUID))
                        .setValue("text value")
                        .setComment("overweight")
                        .setFormNamespace("formNamespace")
                        .setFormFieldPath("formFieldPath")
        );

        Date encounterDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse("2005-01-01T00:00:00.000+0000");
        Patient patient = new Patient();

        Encounter encounter = new Encounter();
        encounter.setUuid("e-uuid");
        encounter.setPatient(patient);
        encounter.setEncounterDatetime(encounterDateTime);

        encounterObservationServiceHelper.update(encounter, observations);

        assertEquals(1, encounter.getObs().size());
        Obs textObservation = encounter.getObs().iterator().next();
        assertEquals("formNamespace",textObservation.getFormFieldNamespace());
        assertEquals("formFieldPath",textObservation.getFormFieldPath());
    }

    private Concept newConcept(ConceptDatatype conceptDatatype, String conceptUuid) {
        Concept concept = new Concept();
        concept.setDatatype(conceptDatatype);
        concept.setUuid(conceptUuid);
        when(conceptService.getConceptByUuid(conceptUuid)).thenReturn(concept);
        return concept;
    }

    private EncounterTransaction.Concept getConcept(String conceptUuid) {
        return new EncounterTransaction.Concept(conceptUuid, "concept_name");
    }

}