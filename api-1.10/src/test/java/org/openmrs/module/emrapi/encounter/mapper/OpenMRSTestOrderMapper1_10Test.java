package org.openmrs.module.emrapi.encounter.mapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OpenMRSTestOrderMapper1_10Test {

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
    public void createNewTestOrderFromEtTestOrder() throws Exception {
        Provider provider = mock(Provider.class);
        handleEncounterProvider(provider);

        Concept mrsBloodConcept = mock(Concept.class);
        when(conceptService.getConceptByUuid("bloodConceptUuid")).thenReturn(mrsBloodConcept);

        Date currentDate = new Date();

        EncounterTransaction.Concept blood = new EncounterTransaction.Concept("bloodConceptUuid","blood");

        EncounterTransaction.TestOrder etTestOrder = new EncounterTransaction.TestOrder();
        etTestOrder.setConcept(blood);
        etTestOrder.setVoided(false);
        etTestOrder.setVoidReason("");
        etTestOrder.setDateCreated(currentDate);

        OpenMRSTestOrderMapper testOrderMapper = new OpenMRSTestOrderMapper(orderService,conceptService);

        TestOrder testOrder = testOrderMapper.map(etTestOrder, encounter);

        Assert.assertEquals(encounter,testOrder.getEncounter());
        Assert.assertEquals(mrsBloodConcept, testOrder.getConcept());
        Assert.assertEquals(false,testOrder.getVoided());
        Assert.assertEquals("", testOrder.getVoidReason());
        Assert.assertEquals(provider,testOrder.getOrderer());
    }

    @Test
    public void updateExistingTestOrderFromEtTestOrder() throws Exception {
        Provider provider = mock(Provider.class);
        handleEncounterProvider(provider);

        TestOrder mrsOrder = new TestOrder();
        when(orderService.getOrderByUuid("orderUuid")).thenReturn(mrsOrder);

        Date createdDate = new Date();
        EncounterTransaction.Concept blood = new EncounterTransaction.Concept("bloodConceptUuid","blood");

        EncounterTransaction.TestOrder etTestOrder = new EncounterTransaction.TestOrder();
        etTestOrder.setUuid("orderUuid")
        .setConcept(blood)
        .setVoided(true)
        .setVoidReason("Some problem")
        .setDateCreated(createdDate);


        OpenMRSTestOrderMapper testOrderMapper = new OpenMRSTestOrderMapper(orderService,conceptService);
        TestOrder testOrder = testOrderMapper.map(etTestOrder, encounter);

        Assert.assertEquals(true,testOrder.getVoided());
        Assert.assertEquals("Some problem", testOrder.getVoidReason());
        Assert.assertNotNull(testOrder.getDateChanged());
    }


    @Test(expected = APIException.class)
    public void handleTestOrderWithInvalidUuid() throws Exception {
        Provider provider = mock(Provider.class);
        handleEncounterProvider(provider);

        when(orderService.getOrderByUuid("orderUuid")).thenReturn(null);

        Date createdDate = new Date();
        EncounterTransaction.Concept blood = new EncounterTransaction.Concept("bloodConceptUuid","blood");

        EncounterTransaction.TestOrder etTestOrder = new EncounterTransaction.TestOrder();
        etTestOrder.setUuid("orderUuid")
                .setConcept(blood)
                .setVoided(true)
                .setVoidReason("Some problem")
                .setDateCreated(createdDate);


        OpenMRSTestOrderMapper testOrderMapper = new OpenMRSTestOrderMapper(orderService,conceptService);
        TestOrder testOrder = testOrderMapper.map(etTestOrder, encounter);
    }

    private void handleEncounterProvider(Provider provider){
        EncounterProvider encounterProvider = mock(EncounterProvider.class);
        when(encounterProvider.getProvider()).thenReturn(provider);

        Set<EncounterProvider> providerSet = new HashSet<EncounterProvider>();
        providerSet.add(encounterProvider);

        when(encounter.getEncounterProviders()).thenReturn(providerSet);
    }



    }