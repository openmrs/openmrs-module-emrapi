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

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        encounterDrugOrderServiceHelper = new EncounterDrugOrderServiceHelper(conceptService, orderService);
    }

    @Test
    public void shouldAddNewDrugOrder() {
        Date date = new Date();

        EncounterTransaction.DrugOrder drugOrder = new DrugOrderBuilder()
                .withBasicValues("drug-uuid", "test-concept-uuid", date, date, 3, "dosage-instruction-uuid", "dosage-frequency-uuid")
                .withNotes("this is notes")
                .build();
        Concept drugConcept = new Concept(3);
        OrderType drugOrderType = new OrderType("Drug Order", "this is a drug order type");

        Patient patient = new Patient();
        Encounter encounter = new Encounter();
        encounter.setUuid("e-uuid");
        encounter.setPatient(patient);
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
        assertEquals(Integer.toString(instructionConceptId), order.getUnits());
        assertEquals(Integer.toString(frequencyConceptId), order.getFrequency());
        assertEquals(date, order.getStartDate());
        assertEquals(date, order.getAutoExpireDate());
        assertEquals(drug.getDisplayName(), order.getDrug().getDisplayName());
        assertEquals(Double.valueOf(3), order.getDose());
        assertEquals(false, order.getPrn());

    }
}
