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
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.openmrs.module.emrapi.encounter.exception.OrderTypeNotFoundException;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterTestOrderServiceHelperTest {

    public static final String TEXT_CONCEPT_UUID = "text-concept-uuid";
    public static final String NUMERIC_CONCEPT_UUID = "numeric-concept-uuid";
    public static final String ORDER_TYPE_UUID = "order-type--uuid";

    @Mock
    private ConceptService conceptService;

    @Mock
    private OrderService orderService;

    EncounterTestOrderServiceHelper encounterTestOrderServiceHelper;

    @Before
    public void setUp() {
        initMocks(this);
        encounterTestOrderServiceHelper = new EncounterTestOrderServiceHelper(conceptService, orderService);
    }

    @Test
    public void shouldAddNewOrder() {
        Concept orderConcept = newConcept(ConceptDatatype.TEXT, TEXT_CONCEPT_UUID);
        OrderType orderType = newOrderType(ORDER_TYPE_UUID);

        List<EncounterTransaction.TestOrder> testOrders = asList(
            new EncounterTransaction.TestOrder().setConceptUuid(TEXT_CONCEPT_UUID).setInstructions("test should be done on empty stomach").setOrderTypeUuid(ORDER_TYPE_UUID)
        );

        Patient patient = new Patient();
        Encounter encounter = new Encounter();
        encounter.setUuid("e-uuid");
        encounter.setPatient(patient);

        encounterTestOrderServiceHelper.update(encounter, testOrders);

        assertEquals(1, encounter.getOrders().size());
        Order order = encounter.getOrders().iterator().next();
        assertEquals(orderConcept, order.getConcept());
        assertEquals(orderType, order.getOrderType());
        assertEquals("test should be done on empty stomach", order.getInstructions());
        assertEquals(patient, order.getPatient());
        assertEquals(encounter, order.getEncounter());
    }

    private OrderType newOrderType(String uuid) {
        OrderType orderType = new OrderType();
        orderType.setUuid(uuid);
        when(orderService.getOrderTypeByUuid(uuid)).thenReturn(orderType);
        return orderType;
    }

    @Test(expected = ConceptNotFoundException.class)
    public void shouldReturnErrorWhenTestOrderConceptIsNotFound() throws Exception {
        List<EncounterTransaction.TestOrder> testOrders = asList(
                new EncounterTransaction.TestOrder().setUuid("o-uuid").setConceptUuid(NUMERIC_CONCEPT_UUID).setInstructions("don't do it")
        );
        Encounter encounter = new Encounter();

        encounterTestOrderServiceHelper.update(encounter, testOrders);
    }

    @Test(expected = OrderTypeNotFoundException.class)
    public void shouldReturnErrorWhenTestOrderTypeIsNotFound() throws Exception {
        List<EncounterTransaction.TestOrder> testOrders = asList(
                new EncounterTransaction.TestOrder().setUuid("o-uuid").setConceptUuid(TEXT_CONCEPT_UUID).setOrderTypeUuid("non-existent-id")
        );
        when(conceptService.getConceptByUuid(TEXT_CONCEPT_UUID)).thenReturn(new Concept());
        Encounter encounter = new Encounter();

        encounterTestOrderServiceHelper.update(encounter, testOrders);
    }

    @Test
    public void shouldUpdateExistingOrder() {
        Concept existingConcept = newConcept(ConceptDatatype.TEXT, TEXT_CONCEPT_UUID);
        Concept newConcept = newConcept(ConceptDatatype.NUMERIC, NUMERIC_CONCEPT_UUID);
        OrderType newOrderType = newOrderType(ORDER_TYPE_UUID);

        List<EncounterTransaction.TestOrder> testOrders = asList(
                new EncounterTransaction.TestOrder().setUuid("o-uuid").setConceptUuid(NUMERIC_CONCEPT_UUID).setInstructions("don't do it").setOrderTypeUuid(ORDER_TYPE_UUID)
        );

        Encounter encounter = new Encounter();
        encounter.setUuid("e-uuid");
        org.openmrs.TestOrder existingOrder = new org.openmrs.TestOrder();
        existingOrder.setUuid("o-uuid");
        existingOrder.setConcept(existingConcept);
        existingOrder.setInstructions("do it");
        existingOrder.setOrderType(new OrderType());
        encounter.addOrder(existingOrder);

        encounterTestOrderServiceHelper.update(encounter, testOrders);

        assertEquals(1, encounter.getOrders().size());
        Order newOrder = encounter.getOrders().iterator().next();
        assertEquals(newConcept, newOrder.getConcept());
        assertEquals(newOrderType, newOrder.getOrderType());
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
