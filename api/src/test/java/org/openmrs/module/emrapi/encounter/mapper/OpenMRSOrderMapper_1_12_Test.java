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
package org.openmrs.module.emrapi.encounter.mapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Provider;
import org.openmrs.Order;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.encounter.builder.OrderBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OpenMRSOrderMapper_1_12_Test {

    @Mock
    private OrderService orderService;

    @Mock
    private ConceptService conceptService;

    @Mock(answer=Answers.RETURNS_DEEP_STUBS)
    private Encounter encounter;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void createNewOrderFromEtOrder() throws Exception {
        Provider provider = mock(Provider.class);
        handleEncounterProvider(provider);

        Concept mrsBloodConcept = mock(Concept.class);
        when(conceptService.getConceptByUuid("bloodConceptUuid")).thenReturn(mrsBloodConcept);

        Date currentDate = new Date();

        EncounterTransaction.Concept blood = new EncounterTransaction.Concept("bloodConceptUuid","blood");

        EncounterTransaction.Order etOrder = new EncounterTransaction.Order();
        etOrder.setConcept(blood);
        etOrder.setUrgency("STAT");
        etOrder.setDateCreated(currentDate);

        OpenMRSOrderMapper orderMapper = new OpenMRSOrderMapper(orderService,conceptService);

        Order order = orderMapper.map(etOrder, encounter);

        Assert.assertEquals(encounter, order.getEncounter());
        Assert.assertEquals(Order.Urgency.STAT, order.getUrgency());
        Assert.assertEquals(mrsBloodConcept, order.getConcept());
        Assert.assertEquals(provider, order.getOrderer());
    }

    @Test(expected = APIException.class)
    public void shouldThrowExceptionForInvalidUrgencyType() throws Exception {
        Provider provider = mock(Provider.class);
        handleEncounterProvider(provider);

        EncounterTransaction.Order etOrder = new EncounterTransaction.Order();
        etOrder.setUrgency("STT");

        OpenMRSOrderMapper orderMapper = new OpenMRSOrderMapper(orderService,conceptService);

        orderMapper.map(etOrder, encounter);

    }

    @Test
    public void discontinueOrder() throws Exception {
        Provider provider = mock(Provider.class);
        handleEncounterProvider(provider);

        Order mrsOrder = new Order();
        when(orderService.getOrderByUuid("previous order uuid")).thenReturn(mrsOrder);

        Date createdDate = new Date();
        EncounterTransaction.Concept blood = new EncounterTransaction.Concept("bloodConceptUuid", "blood");

        EncounterTransaction.Order etOrder = new OrderBuilder().withAction(Order.Action.DISCONTINUE.toString()).
                withUuid("orderUuid").withConcept(blood).withPreviousOrderUuid("previous order uuid").withDateCreated(createdDate).build();

        OpenMRSOrderMapper orderMapper = new OpenMRSOrderMapper(orderService, conceptService);
        Order order = orderMapper.map(etOrder, encounter);

        Assert.assertEquals(Order.Action.DISCONTINUE, order.getAction());
    }

    @Test
    public void createRevisedOrderFromEtOrder(){
        Provider provider = mock(Provider.class);
        handleEncounterProvider(provider);

        Order originalOrder = new Order();
        when(orderService.getOrderByUuid("previousOrderUuid")).thenReturn(originalOrder);


        Date currentDate = new Date();


        EncounterTransaction.Order etOrder = new EncounterTransaction.Order();
        etOrder.setUuid(null);
        etOrder.setPreviousOrderUuid("previousOrderUuid");
        etOrder.setAutoExpireDate(currentDate);
        etOrder.setCommentToFulfiller("Comment");

        OpenMRSOrderMapper orderMapper = new OpenMRSOrderMapper(orderService,conceptService);

        Order order = orderMapper.map(etOrder, encounter);

        verify(orderService).getOrderByUuid("previousOrderUuid");
        Assert.assertEquals(encounter, order.getEncounter());
        Assert.assertEquals("Comment", order.getCommentToFulfiller());
        Assert.assertEquals(currentDate, order.getAutoExpireDate());
        Assert.assertEquals(provider, order.getOrderer());
    }

    private void handleEncounterProvider(Provider provider){
        EncounterProvider encounterProvider = mock(EncounterProvider.class);
        when(encounterProvider.getProvider()).thenReturn(provider);

        Set<EncounterProvider> providerSet = new HashSet<EncounterProvider>();
        providerSet.add(encounterProvider);

        when(encounter.getEncounterProviders()).thenReturn(providerSet);
    }



    }