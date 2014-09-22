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
import org.openmrs.SimpleDosingInstructions;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.util.LocaleUtility;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.util.Locale;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LocaleUtility.class)
public class DrugOrderMapper1_10Test {

    public static final String OUT_PATIENT_CARE_SETTING = "OUTPATIENT";
    public static final String DRUG_ORDER_TYPE = "Drug Order";
    public static final String DAY_DURATION_UNIT = "day";
    public static final String DRUG_UUID = "drug-uuid";
    public static final String CAPSULE_DOSE_UNIT = "Capsule";
    public static final String TABLET_DOSAGE_FORM = "TABLET";
    public static final String MOUTH_ROUTE = "mouth";
    public static final String TABLET_QUANTITY_UNIT = "TABLET";
    public static final String TWICE_A_DAY_FREQUENCY = "Twice a day";

    private OrderMapper1_10 drugOrderMapper110;

    @Before
    public void setup() {
        mockStatic(LocaleUtility.class);

        drugOrderMapper110 = new OrderMapper1_10();
    }

    @Test
    public void shouldMapNewDrugOrder() throws ParseException {

        DrugOrder openMrsDrugOrder = drugOrder(CareSetting.CareSettingType.OUTPATIENT, 3, "3-0-2", 5, "before meals", "boil in water");
        EncounterTransaction.DrugOrder drugOrder = drugOrderMapper110.mapDrugOrder(openMrsDrugOrder);

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
    }

    private DrugOrder drugOrder(CareSetting.CareSettingType careSettingType, int daysToStartAfter, String dosingInstructions, int duration, String instructions, String commentToFulfiller) {
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
        return order;
    }

    private Concept concept(String name) {
        Concept doseUnitsConcept = new Concept();
        doseUnitsConcept.setFullySpecifiedName(new ConceptName(name, Locale.ENGLISH));
        return doseUnitsConcept;
    }

}
