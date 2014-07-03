/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.emrapi.encounter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Encounter;
import org.openmrs.OrderFrequency;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.encounter.domain.DrugOrderBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.InvalidDrugException;

import java.util.Date;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterDrugOrderServiceHelper_1_10Test {

    EncounterDrugOrderServiceHelper_1_10 encounterDrugOrderServiceHelper;
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
        encounterDrugOrderServiceHelper = new EncounterDrugOrderServiceHelper_1_10(conceptService, orderService);

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
        OrderType drugOrderType = new OrderType("Drug Order", "this is a drug order type", "org.openmrs.DrugOrder");

        Concept drugConcept = createConcept("test-concept-uuid");
        when(conceptService.getConceptByUuid("test-concept-uuid")).thenReturn(drugConcept);
        when(orderService.getOrderTypes(true)).thenReturn(asList(drugOrderType));
        when(conceptService.getConceptByUuid("dosage-instruction-uuid")).thenReturn(createConcept("dosage-instruction-uuid"));
        when(conceptService.getConceptByUuid("dosage-frequency-uuid")).thenReturn(createConcept("dosage-frequency-uuid"));

        OrderFrequency expectedOrderFrequency = new OrderFrequency();
        expectedOrderFrequency.setUuid(drugOrder.getDosageFrequencyUuid());
        when(orderService.getOrderFrequencyByUuid(drugOrder.getDosageFrequencyUuid())).thenReturn(expectedOrderFrequency);
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
        assertEquals(drugOrder.getDosageInstructionUuid(), order.getDoseUnits().getUuid());
        assertEquals(drugOrder.getDosageFrequencyUuid(), order.getFrequency().getUuid());
        assertEquals(today, order.getStartDate());
        assertEquals(today, order.getAutoExpireDate());
        assertEquals(drug.getDisplayName(), order.getDrug().getDisplayName());
        assertEquals(Double.valueOf(3), order.getDose());
        assertEquals(false, order.getPrn());

    }

    private Concept createConcept(String uuid) {
        Concept concept = new Concept();
        concept.setUuid(uuid);
        return concept;
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
        OrderType drugOrderType = new OrderType("Drug Order", "this is a drug order type", "org.openmrs.DrugOrder");

        when(orderService.getOrderTypes(true)).thenReturn(asList(drugOrderType));
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
        assertEquals(null, order.getDoseUnits());
    }
}

