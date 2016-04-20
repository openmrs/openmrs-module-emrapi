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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderGroup;
import org.openmrs.OrderSet;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderSetService;
import org.openmrs.module.emrapi.encounter.builder.DrugOrderBuilder;
import org.openmrs.module.emrapi.encounter.builder.OrderBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.mapper.OpenMRSDrugOrderMapper;
import org.openmrs.module.emrapi.encounter.mapper.OpenMRSOrderGroupMapper;
import org.openmrs.module.emrapi.encounter.mapper.OpenMRSOrderMapper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

public class EmrOrderServiceImpl_1_12Test {

    @Mock
    private EncounterService encounterService;

    @Mock
    private OpenMRSDrugOrderMapper openMRSDrugOrderMapper;

    @Mock
    private OpenMRSOrderMapper openMRSOrderMapper;

    @Mock
    private OrderSetService orderSetService;

    @Mock
    private OpenMRSOrderGroupMapper openMRSOrderGroupMapper;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void shouldSaveNewDrugOrdersInTheSequenceOfOrdering() throws ParseException {
        EmrOrderServiceImpl_1_12 emrOrderService = new EmrOrderServiceImpl_1_12(openMRSDrugOrderMapper, encounterService, openMRSOrderMapper, orderSetService, openMRSOrderGroupMapper);
        EncounterTransaction.DrugOrder drugOrder1 = new DrugOrderBuilder().withDrugUuid("drug-uuid1").build();
        EncounterTransaction.DrugOrder drugOrder2 = new DrugOrderBuilder().withDrugUuid("drug-uuid2").build();
        DrugOrder mappedDrugOrder1 = new DrugOrder();
        DrugOrder mappedDrugOrder2 = new DrugOrder();
        Encounter encounter = new Encounter();
        when(openMRSDrugOrderMapper.map(drugOrder1, encounter)).thenReturn(mappedDrugOrder1);
        when(openMRSDrugOrderMapper.map(drugOrder2, encounter)).thenReturn(mappedDrugOrder2);

        emrOrderService.save(Arrays.asList(drugOrder1, drugOrder2), encounter);

        ArgumentCaptor<Encounter> encounterArgumentCaptor = ArgumentCaptor.forClass(Encounter.class);
        verify(encounterService).saveEncounter(encounterArgumentCaptor.capture());
        Encounter savedEncounter = encounterArgumentCaptor.getValue();
        ArrayList<Order> savedOrders = new ArrayList<Order>(savedEncounter.getOrders());
        assertThat(savedOrders.size(), is(2));
        assertThat((DrugOrder)savedOrders.get(0), is(sameInstance(mappedDrugOrder1)));
        assertThat((DrugOrder)savedOrders.get(1), is(sameInstance(mappedDrugOrder2)));
    }

    @Test
    public void shouldSaveNewDrugOrdersInTheSequenceOfOrderingToAnEncounterWithExistingOrders() throws ParseException {
        EmrOrderServiceImpl_1_12 emrOrderService = new EmrOrderServiceImpl_1_12(openMRSDrugOrderMapper, encounterService, openMRSOrderMapper, orderSetService, openMRSOrderGroupMapper);
        EncounterTransaction.DrugOrder drugOrder3 = new DrugOrderBuilder().withDrugUuid("drug-uuid3").build();
        EncounterTransaction.DrugOrder drugOrder4 = new DrugOrderBuilder().withDrugUuid("drug-uuid4").build();
        DrugOrder existingDrugOrder1 = new DrugOrder();
        DrugOrder existingDrugOrder2 = new DrugOrder();
        DrugOrder mappedDrugOrder3 = new DrugOrder();
        DrugOrder mappedDrugOrder4 = new DrugOrder();
        Encounter encounter = new Encounter();
        encounter.addOrder(existingDrugOrder1);
        encounter.addOrder(existingDrugOrder2);
        when(openMRSDrugOrderMapper.map(drugOrder3, encounter)).thenReturn(mappedDrugOrder3);
        when(openMRSDrugOrderMapper.map(drugOrder4, encounter)).thenReturn(mappedDrugOrder4);

        emrOrderService.save(Arrays.asList(drugOrder3, drugOrder4), encounter);

        ArgumentCaptor<Encounter> encounterArgumentCaptor = ArgumentCaptor.forClass(Encounter.class);
        verify(encounterService).saveEncounter(encounterArgumentCaptor.capture());
        Encounter savedEncounter = encounterArgumentCaptor.getValue();
        ArrayList<Order> savedOrders = new ArrayList<Order>(savedEncounter.getOrders());
        assertThat(savedOrders.size(), is(4));
        assertThat((DrugOrder)savedOrders.get(2), is(sameInstance(mappedDrugOrder3)));
        assertThat((DrugOrder)savedOrders.get(3), is(sameInstance(mappedDrugOrder4)));
    }

    @Test
    public void shouldSaveOrders() throws ParseException {
        EmrOrderServiceImpl_1_12 emrOrderService = new EmrOrderServiceImpl_1_12(openMRSDrugOrderMapper, encounterService, openMRSOrderMapper, orderSetService, openMRSOrderGroupMapper);
        EncounterTransaction.Order order1 = new OrderBuilder().withConceptUuid("concept-uuid1").withComment("Comment").build();
        EncounterTransaction.Order order2 = new OrderBuilder().withConceptUuid("concept-uuid2").withComment("Comment").build();

        Order mappedOrder1 = new Order();
        Concept concept = new Concept();
        concept.setUuid("concept-uuid1");
        mappedOrder1.setConcept(concept);
        mappedOrder1.setCommentToFulfiller("Comment");

        Order mappedOrder2 = new Order();
        concept = new Concept();
        concept.setUuid("concept-uuid2");
        mappedOrder2.setConcept(concept);
        mappedOrder2.setCommentToFulfiller("Comment");

        Encounter encounter = new Encounter();
        when(openMRSOrderMapper.map(order1,encounter)).thenReturn(mappedOrder1);
        when(openMRSOrderMapper.map(order2,encounter)).thenReturn(mappedOrder2);

        emrOrderService.saveOrders(Arrays.asList(order1, order2), encounter);

        ArgumentCaptor<Encounter> encounterArgumentCaptor = ArgumentCaptor.forClass(Encounter.class);
        verify(encounterService).saveEncounter(encounterArgumentCaptor.capture());
        Encounter savedEncounter = encounterArgumentCaptor.getValue();
        ArrayList<Order> savedOrders = new ArrayList<Order>(savedEncounter.getOrders());
        assertThat(savedOrders.size(), is(2));
        assertTrue(existsInOrdersList(mappedOrder1, savedOrders));
        assertTrue(existsInOrdersList(mappedOrder2, savedOrders));
    }

    @Test
    public void shouldSaveOrdersWithOrderGroups() throws ParseException {
        EmrOrderServiceImpl_1_12 emrOrderService = new EmrOrderServiceImpl_1_12(openMRSDrugOrderMapper, encounterService, openMRSOrderMapper, orderSetService, openMRSOrderGroupMapper);

        EncounterTransaction.Order order1 = new OrderBuilder().withConceptUuid("concept-uuid1").withComment("Comment").withOrderGroup("orderSet-uuid1").build();
        EncounterTransaction.Order order2 = new OrderBuilder().withConceptUuid("concept-uuid2").withComment("Comment").withOrderGroup("orderSet-uuid1").build();
        EncounterTransaction.Order order3 = new OrderBuilder().withConceptUuid("concept-uuid3").withComment("Comment").withOrderGroup("orderSet-uuid2").build();

        Encounter encounter = new Encounter();
        Patient patient = new Patient();

        OrderGroup mappedOrderGroup1 = new OrderGroup();
        mappedOrderGroup1.setEncounter(encounter);
        mappedOrderGroup1.setPatient(patient);
        OrderSet orderSet1 = new OrderSet();
        mappedOrderGroup1.setOrderSet(orderSet1);

        OrderGroup mappedOrderGroup2 = new OrderGroup();
        mappedOrderGroup2.setEncounter(encounter);
        mappedOrderGroup2.setPatient(patient);
        OrderSet orderSet2 = new OrderSet();
        mappedOrderGroup2.setOrderSet(orderSet2);

        Order mappedOrder1 = new Order();
        Concept concept = new Concept();
        concept.setUuid("concept-uuid1");
        mappedOrder1.setConcept(concept);
        mappedOrder1.setOrderGroup(mappedOrderGroup1);
        mappedOrder1.setCommentToFulfiller("Comment");

        Order mappedOrder2 = new Order();
        concept = new Concept();
        concept.setUuid("concept-uuid2");
        mappedOrder2.setConcept(concept);
        mappedOrder2.setOrderGroup(mappedOrderGroup1);
        mappedOrder2.setCommentToFulfiller("Comment");

        Order mappedOrder3 = new Order();
        concept = new Concept();
        concept.setUuid("concept-uuid3");
        mappedOrder2.setConcept(concept);
        mappedOrder3.setOrderGroup(mappedOrderGroup2);
        mappedOrder2.setCommentToFulfiller("Comment");

        when(openMRSOrderMapper.map(order1,encounter)).thenReturn(mappedOrder1);
        when(openMRSOrderMapper.map(order2,encounter)).thenReturn(mappedOrder2);
        when(openMRSOrderMapper.map(order3,encounter)).thenReturn(mappedOrder3);

        when(openMRSOrderGroupMapper.map(order1.getOrderGroup(), encounter)).thenReturn(mappedOrderGroup1);
        when(openMRSOrderGroupMapper.map(order2.getOrderGroup(), encounter)).thenReturn(mappedOrderGroup1);
        when(openMRSOrderGroupMapper.map(order3.getOrderGroup(), encounter)).thenReturn(mappedOrderGroup2);

        when(orderSetService.getOrderSetByUuid(order1.getOrderGroup().getOrderSet().getUuid())).thenReturn(mappedOrder1.getOrderGroup().getOrderSet());
        when(orderSetService.getOrderSetByUuid(order2.getOrderGroup().getOrderSet().getUuid())).thenReturn(mappedOrder2.getOrderGroup().getOrderSet());
        when(orderSetService.getOrderSetByUuid(order3.getOrderGroup().getOrderSet().getUuid())).thenReturn(mappedOrder3.getOrderGroup().getOrderSet());

        emrOrderService.saveOrders(Arrays.asList(order1, order2, order3), encounter);

        ArgumentCaptor<Encounter> encounterArgumentCaptor = ArgumentCaptor.forClass(Encounter.class);
        verify(encounterService).saveEncounter(encounterArgumentCaptor.capture());
        Encounter savedEncounter = encounterArgumentCaptor.getValue();
        ArrayList<Order> savedOrders = new ArrayList<Order>(savedEncounter.getOrders());
        assertThat(savedOrders.size(), is(3));

        HashMap<String, OrderGroup> orderGroups = new HashMap<String, OrderGroup>();

        for (Order savedOrder : savedOrders) {
            if (savedOrder.getOrderGroup() != null) {
                orderGroups.put(savedOrder.getOrderGroup().getOrderSet().getUuid(), savedOrder.getOrderGroup());
            }
        }

        assertEquals(2, orderGroups.size());
    }

    @Test
    public void shouldSaveOrdersToEncounterWithExistingOrders() throws ParseException {
        EmrOrderServiceImpl_1_12 emrOrderService = new EmrOrderServiceImpl_1_12(openMRSDrugOrderMapper, encounterService, openMRSOrderMapper, orderSetService, openMRSOrderGroupMapper);
        EncounterTransaction.Order order1 = new OrderBuilder().withConceptUuid("concept-uuid1").withComment("Comment").build();
        EncounterTransaction.Order order2 = new OrderBuilder().withConceptUuid("concept-uuid2").withComment("Comment").build();

        Order mappedOrder1 = new Order();
        Concept concept = new Concept();
        concept.setUuid("concept-uuid1");
        mappedOrder1.setConcept(concept);
        mappedOrder1.setCommentToFulfiller("Comment");


        Order mappedOrder2 = new Order();
        concept = new Concept();
        concept.setUuid("concept-uuid2");
        mappedOrder2.setConcept(concept);
        mappedOrder2.setCommentToFulfiller("Comment");


        Order existingOrder1 = new Order();
        Order existingOrder2 = new Order();

        Encounter encounter = new Encounter();
        encounter.addOrder(existingOrder1);
        encounter.addOrder(existingOrder2);

        when(openMRSOrderMapper.map(order1,encounter)).thenReturn(mappedOrder1);
        when(openMRSOrderMapper.map(order2,encounter)).thenReturn(mappedOrder2);

        emrOrderService.saveOrders(Arrays.asList(order1, order2), encounter);

        ArgumentCaptor<Encounter> encounterArgumentCaptor = ArgumentCaptor.forClass(Encounter.class);
        verify(encounterService).saveEncounter(encounterArgumentCaptor.capture());
        Encounter savedEncounter = encounterArgumentCaptor.getValue();
        ArrayList<Order> savedOrders = new ArrayList<Order>(savedEncounter.getOrders());
        assertThat(savedOrders.size(), is(4));
        assertTrue(existsInOrdersList(mappedOrder1, savedOrders));
        assertTrue(existsInOrdersList(mappedOrder2, savedOrders));
    }

    private boolean existsInOrdersList(Order order, ArrayList<Order> orderArrayList) {
        for(Order orderItem : orderArrayList) {
            if(orderItem.getConcept()!=null && orderItem.getConcept().getUuid().equals(order.getConcept().getUuid()) &&
                    orderItem.getCommentToFulfiller().equals(order.getCommentToFulfiller()))
                return true;
        }
        return false;
    }
}