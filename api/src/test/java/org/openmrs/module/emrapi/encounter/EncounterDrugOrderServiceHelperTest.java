package org.openmrs.module.emrapi.encounter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.encounter.domain.DrugOrderBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.InvalidDrugException;

import java.util.Date;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterDrugOrderServiceHelperTest {

    EncounterDrugOrderServiceHelper encounterDrugOrderServiceHelper;
    @Mock
    private OrderService orderService;
    @Mock
    private ConceptService conceptService;
    Patient patient;
    Encounter encounter;
    Date today;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        encounterDrugOrderServiceHelper = new EncounterDrugOrderServiceHelper(conceptService, orderService);

        patient = new Patient();
        encounter = new Encounter();
        encounter.setUuid("e-uuid");
        encounter.setPatient(patient);
        today = new Date();
    }

    @Test
    public void shouldAddNewDrugOrder() {

        EncounterTransaction.DrugOrder drugOrder = new DrugOrderBuilder()
                .withBasicValues("drug-uuid", "test-concept-uuid", today, today, 3.0, "dosage-instruction-uuid", "dosage-frequency-uuid")
                .withNotes("this is notes")
                .build();
        Concept drugConcept = new Concept(3);
        OrderType drugOrderType = new OrderType("Drug Order", "this is a drug order type");

        when(orderService.getAllOrderTypes()).thenReturn(asList(drugOrderType));
        int instructionConceptId = 1;
        when(conceptService.getConceptByUuid("dosage-instruction-uuid")).thenReturn(new Concept(instructionConceptId));
        Integer frequencyConceptId = 2;
        when(conceptService.getConceptByUuid("dosage-frequency-uuid")).thenReturn(new Concept(frequencyConceptId));
        when(conceptService.getConceptByUuid("test-concept-uuid")).thenReturn(drugConcept);
        Drug drug = new Drug();
        drug.setName("test drug");
        when(conceptService.getDrugByUuid("drug-uuid")).thenReturn(drug);

        encounterDrugOrderServiceHelper.update(encounter, asList(drugOrder));

        assertThat(encounter.getOrders().size(), is(1));
        org.openmrs.DrugOrder order = (org.openmrs.DrugOrder) encounter.getOrders().iterator().next();
        assertEquals(drugConcept, order.getConcept());
        assertEquals(drugOrderType, order.getOrderType());
        assertEquals(patient, order.getPatient());
        assertEquals(encounter, order.getEncounter());
        assertEquals("this is notes", order.getInstructions());
        assertEquals("dosage-instruction-uuid", order.getUnits());
        assertEquals("dosage-frequency-uuid", order.getFrequency());
        assertEquals(today, order.getStartDate());
        assertEquals(today, order.getAutoExpireDate());
        assertEquals(drug.getDisplayName(), order.getDrug().getDisplayName());
        assertEquals(Double.valueOf(3), order.getDose());
        assertEquals(false, order.getPrn());

    }

    @Test(expected = InvalidDrugException.class)
    public void shouldThrowExceptionIfDrugDoesNotContainUUid() {
        EncounterTransaction.DrugOrder drugOrder = new DrugOrderBuilder().build();
        encounterDrugOrderServiceHelper.update(encounter, asList(drugOrder));
    }

    @Test(expected = InvalidDrugException.class)
    public void shouldThrowExceptionIfDrugDoesNotContainValidDosageFrequencyWhenPRNIsFalse() {
        EncounterTransaction.DrugOrder drugOrder1 = new DrugOrderBuilder().withPrn(false).withDosageFrequency("im not a valid frequency").build();
        EncounterTransaction.DrugOrder drugOrder2 = new DrugOrderBuilder().withPrn(false).withDosageFrequency("").build();
        encounterDrugOrderServiceHelper.update(encounter, asList(drugOrder1, drugOrder2));
    }

    @Test
    public void shouldAddNewDrugOrderWhenPrnIsTrueWIthNoDosageFrequencyOrDosageInstruction() {

        EncounterTransaction.DrugOrder drugOrder = new DrugOrderBuilder()
                .withBasicValues("drug-uuid", "test-concept-uuid", today, today, 3.0, "", "")
                .withPrn(true)
                .build();
        Concept drugConcept = new Concept(3);
        OrderType drugOrderType = new OrderType("Drug Order", "this is a drug order type");

        when(orderService.getAllOrderTypes()).thenReturn(asList(drugOrderType));
        when(conceptService.getConceptByUuid("test-concept-uuid")).thenReturn(drugConcept);
        Drug drug = new Drug();
        drug.setName("test drug");
        when(conceptService.getDrugByUuid("drug-uuid")).thenReturn(drug);

        encounterDrugOrderServiceHelper.update(encounter, asList(drugOrder));

        assertThat(encounter.getOrders().size(), is(1));
        org.openmrs.DrugOrder order = (org.openmrs.DrugOrder) encounter.getOrders().iterator().next();
        assertEquals(drugConcept, order.getConcept());
        assertEquals(drugOrderType, order.getOrderType());
        assertEquals(patient, order.getPatient());
        assertEquals(encounter, order.getEncounter());
        assertEquals(today, order.getStartDate());
        assertEquals(today, order.getAutoExpireDate());
        assertEquals(drug.getDisplayName(), order.getDrug().getDisplayName());
        assertEquals(Double.valueOf(3), order.getDose());

        assertEquals(true, order.getPrn());
        assertEquals(null, order.getFrequency());
        assertEquals(null, order.getUnits());

    }

}
