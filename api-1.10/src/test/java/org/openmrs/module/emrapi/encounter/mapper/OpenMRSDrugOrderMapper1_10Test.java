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

import org.hamcrest.Matchers;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.SimpleDosingInstructions;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.module.emrapi.encounter.builder.DrugOrderBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.service.OrderMetadataService;
import org.openmrs.module.emrapi.test.builder.ConceptBuilder;

import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

public class OpenMRSDrugOrderMapper1_10Test {

    public static final String OUT_PATIENT_CARE_SETTING = "OUTPATIENT";
    public static final String DRUG_ORDER_TYPE = "Drug Order";
    public static final String DAY_DURATION_UNIT = "day";
    public static final String DRUG_UUID = "drug-uuid";
    private final Concept DAY_DURATION_CONCEPT = new Concept();

    @Mock
    private OrderService orderService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private DosingInstructionsMapper dosingInstructionsMapper;

    @Mock
    private OrderMetadataService orderMetadataService;

    private OpenMRSDrugOrderMapper openMRSDrugOrderMapper;
    private Encounter encounter;

    @Before
    public void setup() {
        initMocks(this);

        openMRSDrugOrderMapper = new OpenMRSDrugOrderMapper(orderService,conceptService, dosingInstructionsMapper, orderMetadataService);

        Drug drug = new Drug();
        drug.setUuid(DRUG_UUID);
        when(conceptService.getDrugByNameOrId(DRUG_UUID)).thenReturn(drug);

        OrderType orderType = new OrderType("Drug Order", "", "org.openmrs.DrugOrder");
        when(orderService.getOrderTypeByConcept(any(Concept.class))).thenReturn(orderType);

        when(orderMetadataService.getDurationUnitsConceptByName(DAY_DURATION_UNIT)).thenReturn(DAY_DURATION_CONCEPT);

        CareSetting outPatientCareSetting = new CareSetting(OUT_PATIENT_CARE_SETTING, OUT_PATIENT_CARE_SETTING, CareSetting.CareSettingType.OUTPATIENT);
        when(orderService.getCareSettingByName(OUT_PATIENT_CARE_SETTING)).thenReturn(outPatientCareSetting);

        when(dosingInstructionsMapper.map(any(EncounterTransaction.DosingInstructions.class), any(DrugOrder.class))).thenAnswer(argumentAt(1));

        encounter = new Encounter();
        HashSet<EncounterProvider> encounterProviders = new HashSet<EncounterProvider>();
        EncounterProvider encounterProvider = new EncounterProvider();
        encounterProviders.add(encounterProvider);
        encounter.setEncounterProviders(encounterProviders);
    }


    @Test
    public void shouldMapNewDrugOrders() throws ParseException {
        EncounterTransaction.DrugOrder drugOrder = DrugOrderBuilder.sample(DRUG_UUID, DAY_DURATION_UNIT);

        DrugOrder openMrsDrugOrder = openMRSDrugOrderMapper.map(drugOrder, encounter);

        assertThat(openMrsDrugOrder.getCareSetting().getName(), is(equalTo(OUT_PATIENT_CARE_SETTING)));
        assertThat(openMrsDrugOrder.getDrug().getUuid(), is(equalTo(DRUG_UUID)));
        assertTrue(openMrsDrugOrder.getDosingType().isAssignableFrom(SimpleDosingInstructions.class));
        assertTrue(openMrsDrugOrder instanceof DrugOrder);
        assertThat(openMrsDrugOrder.getAction(), is(equalTo(Order.Action.NEW)));
        assertThat(openMrsDrugOrder.getEncounter(), is(equalTo(encounter)));
        assertThat(openMrsDrugOrder.getDuration(), is(equalTo(drugOrder.getDuration())));
        assertThat(openMrsDrugOrder.getDurationUnits(), is(equalTo(DAY_DURATION_CONCEPT)));
        verify(dosingInstructionsMapper).map(any(EncounterTransaction.DosingInstructions.class), any(DrugOrder.class));
    }

    @Test
    public void shouldMapRevisedDrugOrders() throws ParseException {
        EncounterTransaction.DrugOrder drugOrder = DrugOrderBuilder.sample(DRUG_UUID, DAY_DURATION_UNIT);
        DrugOrder openMrsDrugOrder = openMRSDrugOrderMapper.map(drugOrder, encounter);

        drugOrder.setAction(Order.Action.REVISE.name());
        drugOrder.setPreviousOrderUuid(openMrsDrugOrder.getUuid());
        when(orderService.getOrderByUuid(openMrsDrugOrder.getUuid())).thenReturn(openMrsDrugOrder);
        DrugOrder revisedOpenMrsDrugOrder = openMRSDrugOrderMapper.map(drugOrder, encounter);

        assertThat(revisedOpenMrsDrugOrder.getPreviousOrder().getUuid(), is(equalTo(openMrsDrugOrder.getUuid())));
        assertThat(revisedOpenMrsDrugOrder.getCareSetting().getName(), is(equalTo(OUT_PATIENT_CARE_SETTING)));
        assertThat(revisedOpenMrsDrugOrder.getDrug().getUuid(), is(equalTo(DRUG_UUID)));
        assertTrue(revisedOpenMrsDrugOrder.getDosingType().isAssignableFrom(SimpleDosingInstructions.class));
        assertTrue(openMrsDrugOrder instanceof DrugOrder);
        assertThat(revisedOpenMrsDrugOrder.getAction(), is(equalTo(Order.Action.REVISE)));
        assertThat(revisedOpenMrsDrugOrder.getEncounter(), is(equalTo(encounter)));
        assertThat(revisedOpenMrsDrugOrder.getDuration(), is(equalTo(drugOrder.getDuration())));
        assertThat(revisedOpenMrsDrugOrder.getDurationUnits(), is(equalTo(DAY_DURATION_CONCEPT)));
        verify(dosingInstructionsMapper, times(2)).map(any(EncounterTransaction.DosingInstructions.class), any(DrugOrder.class));
    }

    @Test
    public void shouldMapStoppedDrugOrders() throws ParseException {
        EncounterTransaction.DrugOrder drugOrder = DrugOrderBuilder.sample(DRUG_UUID, DAY_DURATION_UNIT);
        DrugOrder openMrsDrugOrder = openMRSDrugOrderMapper.map(drugOrder, encounter);

        drugOrder.setAction(Order.Action.DISCONTINUE.name());
        drugOrder.setPreviousOrderUuid(openMrsDrugOrder.getUuid());
        when(orderService.getOrderByUuid(openMrsDrugOrder.getUuid())).thenReturn(openMrsDrugOrder);
        DrugOrder revisedOpenMrsDrugOrder = openMRSDrugOrderMapper.map(drugOrder, encounter);

        assertThat(revisedOpenMrsDrugOrder.getPreviousOrder().getUuid(), is(equalTo(openMrsDrugOrder.getUuid())));
        assertThat(revisedOpenMrsDrugOrder.getCareSetting().getName(), is(equalTo(OUT_PATIENT_CARE_SETTING)));
        assertThat(revisedOpenMrsDrugOrder.getDrug().getUuid(), is(equalTo(DRUG_UUID)));
        assertTrue(revisedOpenMrsDrugOrder.getDosingType().isAssignableFrom(SimpleDosingInstructions.class));
        assertTrue(openMrsDrugOrder instanceof DrugOrder);
        assertThat(revisedOpenMrsDrugOrder.getAction(), is(equalTo(Order.Action.DISCONTINUE)));
        assertThat(revisedOpenMrsDrugOrder.getEncounter(), is(equalTo(encounter)));
        assertThat(revisedOpenMrsDrugOrder.getDuration(), is(equalTo(drugOrder.getDuration())));
        assertThat(revisedOpenMrsDrugOrder.getDurationUnits(), is(equalTo(DAY_DURATION_CONCEPT)));
        verify(dosingInstructionsMapper, times(2)).map(any(EncounterTransaction.DosingInstructions.class), any(DrugOrder.class));
    }

    private Answer<DrugOrder> argumentAt(final int arg) {
        return new Answer<DrugOrder>() {
            @Override
            public DrugOrder answer(InvocationOnMock invocationOnMock) throws Throwable {
                return (DrugOrder) invocationOnMock.getArguments()[arg];
            }
        };
    }

    private static Date date(String string) throws ParseException {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        return  formatter.parseDateTime(string).toDate();
    }
}
