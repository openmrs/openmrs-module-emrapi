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

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Duration;
import org.openmrs.Order;
import org.openmrs.OrderFrequency;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Encounter;
import org.openmrs.SimpleDosingInstructions;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.CareSettingType;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.util.LocaleUtility;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Locale;
import java.util.List;
import java.util.HashSet;
import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LocaleUtility.class, Context.class})
public class OrderMapper1_12Test {

    private static final CareSettingType OUT_PATIENT_CARE_SETTING = CareSettingType.OUTPATIENT;
    private static final String DRUG_ORDER_TYPE = "Drug Order";
    private static final String ORDER_TYPE = "Order";
    private static final String DAY_DURATION_UNIT = "day";
    private static final String DRUG_UUID = "drug-uuid";
    private static final String ORDER_UUID = "order-uuid";
    private static final String CAPSULE_DOSE_UNIT = "Capsule";
    private static final String TABLET_DOSAGE_FORM = "TABLET";
    private static final String MOUTH_ROUTE = "mouth";
    private static final String TABLET_QUANTITY_UNIT = "TABLET";
    private static final String TWICE_A_DAY_FREQUENCY = "Twice a day";
    private static final String FREE_TEXT_DRUG_NAME = "Free Text Drug";

    private OrderMapper1_12 orderMapper1_12;

    @Before
    public void setup() {
        mockStatic(LocaleUtility.class);
        mockStatic(Context.class);

        orderMapper1_12 = new OrderMapper1_12();
    }

    @Test
    public void shouldMapNewOrder() throws ParseException, NoSuchFieldException, IllegalAccessException {

        Order openMrsOrder = order("boil in water", "comments", "ORD-99", "ORD-100", ORDER_TYPE,"STAT");
        openMrsOrder.setUuid(ORDER_UUID);
        EncounterTransaction.Order order = orderMapper1_12.mapOrder(openMrsOrder);

        assertThat(order.getAction(), is(equalTo(Order.Action.NEW.name())));
        assertThat(order.getUuid(), is(equalTo(ORDER_UUID)));
        assertThat(order.getOrderType(), is(equalTo(ORDER_TYPE)));
        assertThat(order.getInstructions(), is(equalTo("boil in water")));
        assertThat(order.getCommentToFulfiller(), is(equalTo("comments")));
        assertThat(order.getUrgency(),is(equalTo("STAT")));
        assertThat(order.getOrderNumber(), is(equalTo("ORD-100")));
        assertThat(order.getDateCreated(), is(equalTo(new LocalDate().toDate())));
        assertThat(order.getDateChanged(), is(equalTo(new LocalDate().toDate())));
        assertThat(order.getOrderNumber(), is(equalTo("ORD-100")));
    }

    @Test
    public void shouldSetPreviousOrder() throws NoSuchFieldException, IllegalAccessException {
        Order openMrsOrder = order("boil in water", "comments", "Previous Order Uuid", "ORD-100", ORDER_TYPE,"ROUTINE");
        EncounterTransaction.Order order = orderMapper1_12.mapOrder(openMrsOrder);

        assertThat(order.getPreviousOrderUuid(), is(equalTo("Previous Order Uuid")));
    }

    @Test
    public void shouldMapMultipleOrders() throws NoSuchFieldException, IllegalAccessException {
        Order order100 = order("before meals", "boil in water", null, "ORD-100", ORDER_TYPE, "ROUTINE");
        Order order201 = order("before meals", "boil in water", null, "ORD-201", ORDER_TYPE, "ROUTINE");
        Order order350 = order("before meals", "boil in water", null, "ORD-350", ORDER_TYPE, "ROUTINE");

        Encounter encounter = new Encounter();
        encounter.setOrders(new HashSet<Order>(Arrays.asList(order350, order100, order201)));
        List<EncounterTransaction.Order> ordersList = orderMapper1_12.mapOrders(encounter);

        assertEquals(3, ordersList.size());
    }

    @Test
    public void shouldMapNewDrugOrder() throws ParseException, NoSuchFieldException, IllegalAccessException {

        DrugOrder openMrsDrugOrder = drugOrder(CareSetting.CareSettingType.OUTPATIENT, 3, "3-0-2", 5, "before meals", "boil in water", null, "ORD-100");
        Concept concept = concept("newConcept", "newConceptDataType", "newConceptUuid");

        openMrsDrugOrder.setConcept(concept);
        EncounterTransaction.DrugOrder drugOrder = orderMapper1_12.mapDrugOrder(openMrsDrugOrder);

        assertThat(drugOrder.getCareSetting(), is(equalTo(OUT_PATIENT_CARE_SETTING)));
        assertThat(drugOrder.getAction(), is(equalTo(Order.Action.NEW.name())));
        assertThat(drugOrder.getDrug().getUuid(), is(equalTo(DRUG_UUID)));
        assertThat(drugOrder.getDosingInstructionType(), is(equalTo(SimpleDosingInstructions.class.getName())));
        assertThat(drugOrder.getDuration(), is(equalTo(5)));
        assertThat(drugOrder.getDurationUnits(), is(equalTo(DAY_DURATION_UNIT)));

        assertThat(drugOrder.getDateActivated(), is(equalTo(new LocalDate().toDate())));
        assertThat(drugOrder.getScheduledDate(), is(equalTo(new LocalDate().plusDays(3).toDate())));
        assertThat(drugOrder.getEffectiveStartDate(), is(equalTo(new LocalDate().plusDays(3).toDate())));
        assertThat(drugOrder.getAutoExpireDate(), is(equalTo(new LocalDate().plusDays(8).toDate())));
        assertThat(drugOrder.getEffectiveStopDate(), is(equalTo(new LocalDate().plusDays(8).toDate())));

        assertThat(drugOrder.getDosingInstructions().getDose(), is(equalTo(2.0)));
        assertThat(drugOrder.getDosingInstructions().getDoseUnits(), is(equalTo(CAPSULE_DOSE_UNIT)));

        assertThat(drugOrder.getDosingInstructions().getRoute(), is(equalTo(MOUTH_ROUTE)));
        assertTrue(drugOrder.getDosingInstructions().getAsNeeded());

        assertThat(drugOrder.getDosingInstructions().getFrequency(), is(equalTo(TWICE_A_DAY_FREQUENCY)));

        assertThat(drugOrder.getDosingInstructions().getQuantity(), is(equalTo(1.0)));
        assertThat(drugOrder.getDosingInstructions().getQuantityUnits(), is(equalTo(TABLET_QUANTITY_UNIT)));
        assertThat(drugOrder.getDosingInstructions().getAdministrationInstructions(), is(equalTo("3-0-2")));

        assertThat(drugOrder.getInstructions(), is(equalTo("before meals")));
        assertThat(drugOrder.getCommentToFulfiller(), is(equalTo("boil in water")));
        assertThat(drugOrder.getOrderNumber(), is(equalTo("ORD-100")));
        assertThat(drugOrder.getConcept().getName(), is(equalTo("newConcept")));
        assertThat(drugOrder.getConcept().getUuid(), is(equalTo("newConceptUuid")));
    }

    @Test
    public void shouldMapNewNonCodedDrugOrder() throws ParseException, NoSuchFieldException, IllegalAccessException {

        DrugOrder nonCodedDrugOrder = drugOrder(CareSetting.CareSettingType.OUTPATIENT, 3, "3-0-2", 5, "before meals", "boil in water", null, "ORD-100");
        nonCodedDrugOrder.setDrugNonCoded(FREE_TEXT_DRUG_NAME);
        nonCodedDrugOrder.setDrug(null);
        EncounterTransaction.DrugOrder drugOrder = orderMapper1_12.mapDrugOrder(nonCodedDrugOrder);

        assertThat(drugOrder.getDrugNonCoded(), is(equalTo(FREE_TEXT_DRUG_NAME)));
        assertThat(drugOrder.getDrug(), is(equalTo(null)));
    }

    @Test
    public void shouldSetPreviousDrugOrder() throws NoSuchFieldException, IllegalAccessException {
        DrugOrder openMrsDrugOrder = drugOrder(CareSetting.CareSettingType.OUTPATIENT, 3, "3-0-2", 5, "before meals", "boil in water", "previousOrderUuid", "ORD-100");
        EncounterTransaction.DrugOrder drugOrder = orderMapper1_12.mapDrugOrder(openMrsDrugOrder);

        assertThat(drugOrder.getPreviousOrderUuid(), is(equalTo("previousOrderUuid")));
    }

    @Test
    public void shouldReturnDrugOrdersSortedByOrderNumber() throws NoSuchFieldException, IllegalAccessException {
        DrugOrder drugOrder100 = drugOrder(CareSetting.CareSettingType.OUTPATIENT, 3, "3-0-2", 5, "before meals", "boil in water", null, "ORD-100");
        DrugOrder drugOrder201 = drugOrder(CareSetting.CareSettingType.OUTPATIENT, 3, "3-0-2", 5, "before meals", "boil in water", null, "ORD-201");
        DrugOrder drugOrder350 = drugOrder(CareSetting.CareSettingType.OUTPATIENT, 3, "3-0-2", 5, "before meals", "boil in water", null, "ORD-350");

        Encounter encounter = new Encounter();
        encounter.setOrders(new HashSet<Order>(Arrays.asList(drugOrder350, drugOrder100, drugOrder201)));
        List<EncounterTransaction.DrugOrder> sortedDrugOrders = orderMapper1_12.mapDrugOrders(encounter);

        assertEquals("ORD-100", sortedDrugOrders.get(0).getOrderNumber());
        assertEquals("ORD-201", sortedDrugOrders.get(1).getOrderNumber());
        assertEquals("ORD-350", sortedDrugOrders.get(2).getOrderNumber());
    }

    private Order order(String instructions, String commentToFulfiller, String previousOrderUuid, String orderNumber, String orderType,String urgency) throws NoSuchFieldException, IllegalAccessException {
        Order order = new Order();
        order.setPatient(new Patient());
        order.setAction(Order.Action.NEW);

        OrderType ordertype = new OrderType();
        ordertype.setName(orderType);
        order.setOrderType(ordertype);

        Concept durationConcept = concept(DAY_DURATION_UNIT);
        ConceptSource durationConceptSource = new ConceptSource();
        durationConceptSource.setUuid(Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE);
        durationConcept.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(durationConceptSource, "D", "Day"), new ConceptMapType()));

        order.setDateCreated(new LocalDate().toDate());
        order.setDateChanged(new LocalDate().toDate());

        OrderFrequency orderFrequency = new OrderFrequency();
        orderFrequency.setFrequencyPerDay(2.0);
        orderFrequency.setConcept(concept("Order_Concept"));

        order.setInstructions(instructions);
        order.setCommentToFulfiller(commentToFulfiller);

        order.setUrgency(Order.Urgency.valueOf(urgency));

        Field field = Order.class.getDeclaredField("orderNumber");
        field.setAccessible(true);
        field.set(order, orderNumber);

        if (StringUtils.isNotBlank(previousOrderUuid)) {
            Order previousOrder = new Order();
            previousOrder.setUuid(previousOrderUuid);
            order.setPreviousOrder(previousOrder);
        }

        return order;
    }

    private DrugOrder drugOrder(CareSetting.CareSettingType careSettingType, int daysToStartAfter, String dosingInstructions,
                                int duration, String instructions, String commentToFulfiller, String previousOrderUuid, String orderNumber) throws NoSuchFieldException, IllegalAccessException {
        DrugOrder order = new DrugOrder();
        order.setPatient(new Patient());
        order.setCareSetting(new CareSetting(careSettingType.name(), null, CareSetting.CareSettingType.OUTPATIENT));
        order.setAction(Order.Action.NEW);

        Drug drug = new Drug();
        drug.setUuid(DRUG_UUID);
        drug.setDosageForm(concept(TABLET_DOSAGE_FORM));
        order.setDrug(drug);

        OrderType orderType = new OrderType();
        orderType.setName(DRUG_ORDER_TYPE);
        order.setOrderType(orderType);

        order.setDosingType(SimpleDosingInstructions.class);

        order.setDuration(duration);
        Concept durationConcept = concept(DAY_DURATION_UNIT);
        ConceptSource durationConceptSource = new ConceptSource();
        durationConceptSource.setUuid(Duration.SNOMED_CT_CONCEPT_SOURCE_HL7_CODE);
        durationConcept.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(durationConceptSource, "D", "Day"), new ConceptMapType()));
        order.setDurationUnits(durationConcept);

        order.setDateActivated(new LocalDate().toDate());
        order.setUrgency(Order.Urgency.ON_SCHEDULED_DATE);
        order.setScheduledDate(new LocalDate().plusDays(daysToStartAfter).toDate());
        order.setAutoExpireDate(new LocalDate().plusDays(daysToStartAfter + duration).toDate());

        order.setDose(2.0);
        order.setDoseUnits(concept(CAPSULE_DOSE_UNIT));
        order.setDosingInstructions(dosingInstructions);

        order.setRoute(concept(MOUTH_ROUTE));
        order.setAsNeeded(true);

        OrderFrequency orderFrequency = new OrderFrequency();
        orderFrequency.setFrequencyPerDay(2.0);
        orderFrequency.setConcept(concept(TWICE_A_DAY_FREQUENCY));
        order.setFrequency(orderFrequency);

        order.setQuantity(1.0);
        order.setQuantityUnits(concept(TABLET_QUANTITY_UNIT));

        order.setInstructions(instructions);
        order.setCommentToFulfiller(commentToFulfiller);

        Field field = Order.class.getDeclaredField("orderNumber");
        field.setAccessible(true);
        field.set(order, orderNumber);

        if (StringUtils.isNotBlank(previousOrderUuid)) {
            Order previousOrder = new Order();
            previousOrder.setUuid(previousOrderUuid);
            order.setPreviousOrder(previousOrder);
        }

        return order;
    }

    private Concept concept(String name) {
        Concept doseUnitsConcept = new Concept();
        doseUnitsConcept.setFullySpecifiedName(new ConceptName(name, Locale.ENGLISH));
        return doseUnitsConcept;
    }

    private Concept concept(String name, String dataType, String conceptUuid) {
        Concept concept = concept(name);

        ConceptDatatype conceptDataType = new ConceptDatatype();
        conceptDataType.setName(dataType);

        concept.setDatatype(conceptDataType);
        concept.setUuid(conceptUuid);
        return concept;
    }

}
