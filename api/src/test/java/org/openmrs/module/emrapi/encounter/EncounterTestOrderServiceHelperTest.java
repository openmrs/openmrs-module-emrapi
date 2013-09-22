package org.openmrs.module.emrapi.encounter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterTestOrderServiceHelperTest {

    public static final String TEXT_CONCEPT_UUID = "text-concept-uuid";
    public static final String NUMERIC_CONCEPT_UUID = "numeric-concept-uuid";

    @Mock
    private ConceptService conceptService;

    EncounterTestOrderServiceHelper encounterTestOrderServiceHelper;

    @Before
    public void setUp() {
        initMocks(this);
        encounterTestOrderServiceHelper = new EncounterTestOrderServiceHelper(conceptService);
    }

    @Test
    public void shouldAddNewOrder() {
        Concept orderConcept = newConcept(ConceptDatatype.TEXT, TEXT_CONCEPT_UUID);

        List<EncounterTransaction.TestOrder> testOrders = asList(
            new EncounterTransaction.TestOrder().setConceptUuid(TEXT_CONCEPT_UUID).setInstructions("test should be done on empty stomach")
        );

        Patient patient = new Patient();
        Encounter encounter = new Encounter();
        encounter.setUuid("e-uuid");
        encounter.setPatient(patient);

        encounterTestOrderServiceHelper.update(encounter, testOrders);

        assertEquals(1, encounter.getOrders().size());
        Order order = encounter.getOrders().iterator().next();
        assertEquals(orderConcept, order.getConcept());
        assertEquals("test should be done on empty stomach", order.getInstructions());
        assertEquals(patient, order.getPatient());
        assertEquals(encounter, order.getEncounter());
    }

    @Test(expected = ConceptNotFoundException.class)
    public void shouldReturnErrorWhenTestOrderConceptIsNotFound() throws Exception {
        List<EncounterTransaction.TestOrder> testOrders = asList(
                new EncounterTransaction.TestOrder().setUuid("o-uuid").setConceptUuid(NUMERIC_CONCEPT_UUID).setInstructions("don't do it")
        );
        Encounter encounter = new Encounter();
        encounterTestOrderServiceHelper.update(encounter, testOrders);
    }

    @Test
    public void shouldUpdateExistingOrder() {
        Concept existingConcept = newConcept(ConceptDatatype.TEXT, TEXT_CONCEPT_UUID);
        Concept newConcept = newConcept(ConceptDatatype.NUMERIC, NUMERIC_CONCEPT_UUID);

        List<EncounterTransaction.TestOrder> testOrders = asList(
                new EncounterTransaction.TestOrder().setUuid("o-uuid").setConceptUuid(NUMERIC_CONCEPT_UUID).setInstructions("don't do it")
        );

        Encounter encounter = new Encounter();
        encounter.setUuid("e-uuid");
        org.openmrs.TestOrder existingOrder = new org.openmrs.TestOrder();
        existingOrder.setUuid("o-uuid");
        existingOrder.setConcept(existingConcept);
        existingOrder.setInstructions("do it");
        encounter.addOrder(existingOrder);

        encounterTestOrderServiceHelper.update(encounter, testOrders);

        assertEquals(1, encounter.getOrders().size());
        Order newOrder = encounter.getOrders().iterator().next();
        assertEquals(newConcept, newOrder.getConcept());
        assertEquals("don't do it", newOrder.getInstructions());
    }

    @Test
    public void shouldVoidExistingOrder() throws Exception {
        Concept concept = newConcept(ConceptDatatype.TEXT, TEXT_CONCEPT_UUID);

        List<EncounterTransaction.TestOrder> testOrders = asList(
                new EncounterTransaction.TestOrder().setUuid("o-uuid").setInstructions("don't do it").setVoided(true).setVoidReason("closed")
        );

        Encounter encounter = new Encounter();
        encounter.setUuid("e-uuid");
        org.openmrs.TestOrder existingOrder = new org.openmrs.TestOrder();
        existingOrder.setUuid("o-uuid");
        existingOrder.setConcept(concept);
        existingOrder.setInstructions("do it");
        encounter.addOrder(existingOrder);

        encounterTestOrderServiceHelper.update(encounter, testOrders);
        assertEquals(1, encounter.getOrders().size());

        Order order = encounter.getOrders().iterator().next();
        assertTrue(order.getVoided());
        assertEquals("closed", order.getVoidReason());
    }

    private Concept newConcept(String hl7, String uuid) {
        Concept concept = new Concept();
        ConceptDatatype textDataType = new ConceptDatatype();
        textDataType.setHl7Abbreviation(hl7);
        concept.setDatatype(textDataType);
        concept.setUuid(uuid);
        when(conceptService.getConceptByUuid(uuid)).thenReturn(concept);
        return concept;
    }
}
