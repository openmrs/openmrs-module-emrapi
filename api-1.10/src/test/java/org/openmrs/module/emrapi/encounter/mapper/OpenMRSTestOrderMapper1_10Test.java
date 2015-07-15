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
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.TestOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.encounter.builder.TestOrderBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

public class OpenMRSTestOrderMapper1_10Test {

    @Mock
    private OrderService orderService;

    @Mock
    private ConceptService conceptService;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Encounter encounter;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void createNewTestOrderFromEtTestOrder() throws Exception {
        Provider provider = mock(Provider.class);
        handleEncounterProvider(provider);

        Concept mrsBloodConcept = mock(Concept.class);
        when(conceptService.getConceptByUuid("bloodConceptUuid")).thenReturn(mrsBloodConcept);

        Date currentDate = new Date();

        EncounterTransaction.Concept blood = new EncounterTransaction.Concept("bloodConceptUuid", "blood");

        EncounterTransaction.TestOrder etTestOrder = new EncounterTransaction.TestOrder();
        etTestOrder.setConcept(blood);
        etTestOrder.setVoided(false);
        etTestOrder.setVoidReason("");
        etTestOrder.setDateCreated(currentDate);

        OpenMRSTestOrderMapper testOrderMapper = new OpenMRSTestOrderMapper(orderService, conceptService);

        TestOrder testOrder = testOrderMapper.map(etTestOrder, encounter);

        Assert.assertEquals(encounter, testOrder.getEncounter());
        Assert.assertEquals(mrsBloodConcept, testOrder.getConcept());
        Assert.assertEquals(false, testOrder.getVoided());
        Assert.assertEquals("", testOrder.getVoidReason());
        Assert.assertEquals(provider, testOrder.getOrderer());
    }

    @Test
    public void discontinueTestOrder() throws Exception {
        Provider provider = mock(Provider.class);
        handleEncounterProvider(provider);

        TestOrder mrsOrder = new TestOrder();
        when(orderService.getOrderByUuid("previous order uuid")).thenReturn(mrsOrder);

        Date createdDate = new Date();
        EncounterTransaction.Concept blood = new EncounterTransaction.Concept("bloodConceptUuid", "blood");

        EncounterTransaction.TestOrder etTestOrder = new TestOrderBuilder().withAction(Order.Action.DISCONTINUE.toString()).
                withUuid("orderUuid").withConcept(blood).withPreviousOrderUuid("previous order uuid").withDateCreated(createdDate).build();

        OpenMRSTestOrderMapper testOrderMapper = new OpenMRSTestOrderMapper(orderService, conceptService);
        TestOrder testOrder = testOrderMapper.map(etTestOrder, encounter);

        Assert.assertEquals(Order.Action.DISCONTINUE, testOrder.getAction());
    }

    @Test
    public void createRevisedTestOrderFromEtTestOrder() {
        Provider provider = mock(Provider.class);
        handleEncounterProvider(provider);

        TestOrder originalTestOrder = new TestOrder();
        when(orderService.getOrderByUuid("previousOrderUuid")).thenReturn(originalTestOrder);


        Date currentDate = new Date();


        EncounterTransaction.TestOrder etTestOrder = new EncounterTransaction.TestOrder();
        etTestOrder.setUuid(null);
        etTestOrder.setPreviousOrderUuid("previousOrderUuid");
        etTestOrder.setAutoExpireDate(currentDate);
        etTestOrder.setCommentToFulfiller("Comment");

        OpenMRSTestOrderMapper testOrderMapper = new OpenMRSTestOrderMapper(orderService,conceptService);

        TestOrder testOrder = testOrderMapper.map(etTestOrder, encounter);

        verify(orderService).getOrderByUuid("previousOrderUuid");
        Assert.assertEquals(encounter, testOrder.getEncounter());
        Assert.assertEquals("Comment", testOrder.getCommentToFulfiller());
        Assert.assertEquals(currentDate, testOrder.getAutoExpireDate());
        Assert.assertEquals(provider,testOrder.getOrderer());
    }

    private void handleEncounterProvider(Provider provider){
        EncounterProvider encounterProvider = mock(EncounterProvider.class);
        when(encounterProvider.getProvider()).thenReturn(provider);

        Set<EncounterProvider> providerSet = new HashSet<EncounterProvider>();
        providerSet.add(encounterProvider);

        when(encounter.getEncounterProviders()).thenReturn(providerSet);
    }



    }