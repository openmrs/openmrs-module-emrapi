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
package org.openmrs.module.emrapi.web.controller;

import junit.framework.Assert;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.joda.time.DateTime;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.TestOrder;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.EncounterMatcherNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:moduleApplicationContext.xml"}, inheritLocations = true)
public class EmrEncounterControllerTest extends BaseEmrControllerTest {

    @Autowired
    private VisitService visitService;

    @Test
    public void shouldCreateVisitWhenNoVisitsAreActive() throws Exception {
        executeDataSet("shouldCreateVisitWhenNoVisitsAreActive.xml");

        String json = "{ \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", \"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\"," +
                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\" }";

        EncounterTransaction response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransaction.class);

        Visit visit = visitService.getVisitByUuid(response.getVisitUuid());
        assertEquals("a76e8d23-0c38-408c-b2a8-ea5540f01b51", visit.getPatient().getUuid());
        assertEquals("b45ca846-c79a-11e2-b0c0-8e397087571c", visit.getVisitType().getUuid());
    }

    @Test
    public void shouldCreateNewEncounter() throws Exception {
        executeDataSet("shouldCreateMatchingEncounter.xml");

        String encounterDateTimeString = "2011-05-01T12:10:06.000+0530";
        Date encounterDateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(encounterDateTimeString);

        String json = "{ \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                "\"encounterDateTime\" : \"" + encounterDateTimeString + "\", " +
                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\" }";

        EncounterTransaction response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransaction.class);

        assertEquals("1e5d5d48-6b78-11e0-93c3-18a905e044dc", response.getVisitUuid());

        Visit visit = visitService.getVisitByUuid(response.getVisitUuid());
        assertEquals(1, visit.getEncounters().size());
        Encounter encounter = visit.getEncounters().iterator().next();

        assertEquals("a76e8d23-0c38-408c-b2a8-ea5540f01b51", encounter.getPatient().getUuid());
        assertEquals("2b377dba-62c3-4e53-91ef-b51c68899890", encounter.getEncounterType().getUuid());
        assertEquals(encounterDateTime, encounter.getEncounterDatetime());
    }

    @Test
    public void shouldUpdateMatchingEncounterWhenCustomMatchingStrategyIsProvided() throws Exception {
        executeDataSet("shouldUpdateMatchingEncounterWhenCustomMatchingStrategyIsProvided.xml");

        String json = "{ \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\" }";

        EncounterTransaction response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransaction.class);

        assertEquals("f13d6fae-baa9-4553-955d-920098bec08g", response.getEncounterUuid());
    }

    @Test(expected = EncounterMatcherNotFoundException.class)
    public void shouldReturnErrorWhenInvalidMatchingStrategyIsProvided() throws Exception {
        executeDataSet("shouldReturnErrorWhenInvalidMatchingStrategyIsProvided.xml");

        String json = "{ \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\" }";

        handle(newPostRequest("/rest/emrapi/encounter", json));
    }

    @Test
    public void shouldAddNewObservation() throws Exception {
        executeDataSet("shouldAddNewObservation.xml");

        String json = "{ \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                        "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                        "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\", " +
                        "\"encounterDateTime\" : \"2005-01-01T00:00:00.000+0000\", " +
                        "\"observations\":[" +
                            "{\"concept\": {\"uuid\": \"d102c80f-1yz9-4da3-bb88-8122ce8868dd\"}, \"conceptName\":\"Should be Ignored\", \"value\":20 }, " +
                            "{\"concept\": {\"uuid\": \"e102c80f-1yz9-4da3-bb88-8122ce8868dd\"}, \"value\":\"text value\", \"comment\":\"overweight\"}]}";

        MockHttpServletResponse response1 = handle(newPostRequest("/rest/emrapi/encounter", json));

        EncounterTransaction response = deserialize(response1, EncounterTransaction.class);

        Visit visit = visitService.getVisitByUuid(response.getVisitUuid());
        Encounter encounter = visit.getEncounters().iterator().next();

        assertEquals(2, encounter.getObs().size());
        Iterator<Obs> obsIterator = encounter.getObs().iterator();

        Map<String, Obs> map = new HashMap <String, Obs>();
        while (obsIterator.hasNext()) {
            Obs obs = obsIterator.next();
            map.put(obs.getConcept().getDatatype().getHl7Abbreviation(), obs);
        }
        Obs textObservation = map.get(ConceptDatatype.TEXT);
        assertEquals("text value", textObservation.getValueText());
        assertEquals("a76e8d23-0c38-408c-b2a8-ea5540f01b51", textObservation.getPerson().getUuid());
        assertEquals("e102c80f-1yz9-4da3-bb88-8122ce8868dd", textObservation.getConcept().getUuid());
        assertEquals("f13d6fae-baa9-4553-955d-920098bec08f", textObservation.getEncounter().getUuid());
        assertEquals("overweight", textObservation.getComment());
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse("2005-01-01T00:00:00.000+0000"), textObservation.getObsDatetime());

        assertEquals(new Double(20.0), map.get(ConceptDatatype.NUMERIC).getValueNumeric());
    }

    @Test
    public void shouldAddNewObservationGroup() throws Exception {
        executeDataSet("shouldAddNewObservation.xml");

        String json = "{ \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\", " +
                "\"encounterDateTime\" : \"2005-01-01T00:00:00.000+0000\", " +
                "\"observations\":[" +
                "{\"concept\":{\"uuid\": \"e102c80f-1yz9-4da3-bb88-8122ce8868dd\"}, " +
                " \"groupMembers\" : [{\"concept\":{\"uuid\": \"d102c80f-1yz9-4da3-bb88-8122ce8868dd\"}, \"value\":20, \"comment\":\"overweight\" }] }" +
                "]}";

        EncounterTransaction response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransaction.class);

        Visit visit = visitService.getVisitByUuid(response.getVisitUuid());
        Encounter encounter = (Encounter) visit.getEncounters().toArray()[0];

        assertEquals(1, encounter.getObs().size());
        Obs obs = (Obs) encounter.getAllObs().toArray()[0];
        assertEquals("e102c80f-1yz9-4da3-bb88-8122ce8868dd", obs.getConcept().getUuid());
        
        assertEquals(1, obs.getGroupMembers().size());
        Obs member = obs.getGroupMembers().iterator().next();
        assertEquals("d102c80f-1yz9-4da3-bb88-8122ce8868dd", member.getConcept().getUuid());
        assertEquals(new Double(20.0), member.getValueNumeric());
        assertEquals("a76e8d23-0c38-408c-b2a8-ea5540f01b51", member.getPerson().getUuid());
        assertEquals("f13d6fae-baa9-4553-955d-920098bec08f", member.getEncounter().getUuid());
        assertEquals("overweight", member.getComment());
        assertEquals(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse("2005-01-01T00:00:00.000+0000"), member.getObsDatetime());
    }

    @Test
    public void shouldUpdateObservations() throws Exception {
        executeDataSet("shouldUpdateObservations.xml");

        String json = "{ \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\"," +
                "\"encounterDateTime\" : \"2013-01-01T00:00:00.000+0000\", " +
                "\"observations\":[" +
                "{\"observationUuid\":\"z9fb7f47-e80a-4056-9285-bd798be13c63\", " +
                " \"groupMembers\" : [{\"observationUuid\":\"ze48cdcb-6a76-47e3-9f2e-2635032f3a9a\", \"value\":20, \"comment\":\"new gc\" }] }, " +
                "{\"observationUuid\":\"zf616900-5e7c-4667-9a7f-dcb260abf1de\", \"comment\" : \"new c\", \"value\":100 }" +
                "]}";

        EncounterTransaction response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransaction.class);

        Visit visit = visitService.getVisitByUuid(response.getVisitUuid());
        Encounter encounter = (Encounter) visit.getEncounters().toArray()[0];

        assertEquals(2, encounter.getObsAtTopLevel(false).size());
        Iterator<Obs> iterator = encounter.getObsAtTopLevel(false).iterator();
        
        Obs obs1 = iterator.next();
        assertEquals("zf616900-5e7c-4667-9a7f-dcb260abf1de", obs1.getUuid());
        assertEquals(new Double(100), obs1.getValueNumeric());
        assertEquals("new c", obs1.getComment());
        
        Obs obs2 = iterator.next();
        assertEquals("z9fb7f47-e80a-4056-9285-bd798be13c63", obs2.getUuid());
        assertEquals(1, obs2.getGroupMembers().size());
        Obs member = obs2.getGroupMembers().iterator().next();
        assertEquals(new Double(20), member.getValueNumeric());
        assertEquals("new gc", member.getComment());
    }


    @Test
    public void shouldAddNewTestOrder() throws Exception {
        executeDataSet("shouldAddNewTestOrder.xml");

        String json = "{ \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\", " +
                "\"encounterDateTime\" : \"2005-01-01T00:00:00.000+0000\", " +
                "\"testOrders\":[" +
                "{\"concept\": {\"uuid\": \"d102c80f-1yz9-4da3-bb88-8122ce8868dd\"}, \"instructions\":\"do it\", \"orderTypeUuid\": \"1a61ef2a-250c-11e3-b832-0800271c1b75\" }]}";


        EncounterTransaction response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransaction.class);

        Visit visit = visitService.getVisitByUuid(response.getVisitUuid());
        Encounter encounter = visit.getEncounters().iterator().next();

        assertEquals(1, encounter.getOrders().size());
        Order testOrder = encounter.getOrders().iterator().next();
        assertEquals("d102c80f-1yz9-4da3-bb88-8122ce8868dd", testOrder.getConcept().getUuid());
        assertEquals("a76e8d23-0c38-408c-b2a8-ea5540f01b51", testOrder.getPatient().getUuid());
        assertEquals("f13d6fae-baa9-4553-955d-920098bec08f", testOrder.getEncounter().getUuid());
        assertEquals("1a61ef2a-250c-11e3-b832-0800271c1b75", testOrder.getOrderType().getUuid());
        assertEquals("do it", testOrder.getInstructions());
    }

    @Test
    public void shouldAddDiagnosesAdObservation() throws Exception {
        executeDataSet("baseMetaData.xml");
        executeDataSet("diagnosisMetaData.xml");
        executeDataSet("shouldAddDiagnosisAsObservation.xml");

        String cancerDiagnosisUuid = "d102c80f-1yz9-4da3-bb88-8122ce8868dh";

        String postData = "{" +
                                "\"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899891\", " +
                                "\"encounterDateTime\" : \"2005-01-01T00:00:00.000+0000\", " +
                                "\"diagnoses\":[" +
                                    "{\"order\":\"PRIMARY\", \"certainty\": \"CONFIRMED\", \"codedAnswer\": { \"uuid\": \"" + cancerDiagnosisUuid + "\"} }" +
                                "]" +
                           "}";

        EncounterTransaction response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", postData)), EncounterTransaction.class);

        Visit visit = visitService.getVisitByUuid(response.getVisitUuid());
        Encounter encounter = visit.getEncounters().iterator().next();

        Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(false);
        assertEquals(1, obsAtTopLevel.size());
        Obs parentObservation = obsAtTopLevel.iterator().next();
        assertTrue(parentObservation.isObsGrouping());
        Set<Obs> diagnosisObservationGroupMembers = parentObservation.getGroupMembers();
        assertEquals(3, diagnosisObservationGroupMembers.size());
        ArrayList<String> valueCodedNames = getValuCodedNames(diagnosisObservationGroupMembers);
        assertTrue(valueCodedNames.contains("Confirmed"));
        assertTrue(valueCodedNames.contains("Primary"));
        assertTrue(valueCodedNames.contains("Cancer"));
    }

    private ArrayList<String> getValuCodedNames(Set<Obs> diagnosisObservationGroupMembers) {
        ArrayList<String> valueCodedNames = new ArrayList<String>();
        for (Obs diagnosisObservationGroupMember : diagnosisObservationGroupMembers) {
            valueCodedNames.add(diagnosisObservationGroupMember.getValueCoded().getName().getName());
        }
        return valueCodedNames;
    }

    @Test
    public void shouldAddNewDrugOrder() throws Exception {
        executeDataSet("shouldAddNewDrugOrder.xml");

        String json = "{ \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\", " +
                "\"encounterDateTime\" : \"2005-01-01T00:00:00.000+0000\", " +
                "\"testOrders\":[" +
                "{\"concept\":{ \"uuid\": \"d102c80f-1yz9-4da3-bb88-8122ce8868dd\"}, " +
                "\"instructions\":\"do it\", \"orderTypeUuid\": \"1a61ef2a-250c-11e3-b832-0800271c1b75\" }]," +
                "\"drugOrders\":[" +
                "{\"uuid\": \"4d6fb6e0-4950-426c-9a9b-1f97e6037893\"," +
                "\"concept\": {\"uuid\": \"29dc4a20-507f-40ed-9545-d47e932483fa\"}," +
                "\"notes\": \"Take as needed\"," +
                "\"startDate\": \"2013-09-30T09:26:09.717Z\"," +
                "\"endDate\": \"2013-10-02T09:26:09.717Z\"," +
                "\"numberPerDosage\": 1," +
                "\"dosageInstructionUuid\": \"632aa422-2696-11e3-895c-0800271c1b75\"," +
                "\"dosageFrequencyUuid\": \"6302096d-2696-11e3-895c-0800271c1b75\"," +
                "\"prn\": true}" +
                "]}";

        EncounterTransaction response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransaction.class);

        Visit visit = visitService.getVisitByUuid(response.getVisitUuid());
        Encounter encounter = visit.getEncounters().iterator().next();
        assertEquals(2, encounter.getOrders().size());

        List<Order> orders = new ArrayList<Order>(encounter.getOrders());

        List<DrugOrder> drugOrders = getOrdersOfType(orders, DrugOrder.class);
        assertEquals(1, drugOrders.size());
        DrugOrder drugOrder = drugOrders.get(0);
        assertEquals("a76e8d23-0c38-408c-b2a8-ea5540f01b51", drugOrder.getPatient().getUuid());
        assertEquals("f13d6fae-baa9-4553-955d-920098bec08f", drugOrder.getEncounter().getUuid());
        assertEquals("29dc4a20-507f-40ed-9545-d47e932483fa", drugOrder.getConcept().getUuid());
        assertEquals("1a61ef2a-250c-11e3-b832-9876541c1b75", drugOrder.getOrderType().getUuid());
        assertEquals("Take as needed", drugOrder.getInstructions());
        assertEquals(new DateTime("2013-09-30T09:26:09.717Z").toDate(), drugOrder.getStartDate());
        assertEquals(new DateTime("2013-10-02T09:26:09.717Z").toDate(), drugOrder.getAutoExpireDate());
        assertEquals("6302096d-2696-11e3-895c-0800271c1b75", drugOrder.getFrequency());
        assertEquals("632aa422-2696-11e3-895c-0800271c1b75", drugOrder.getUnits());
        assertEquals("test drug", drugOrder.getDrug().getDisplayName());
        assertEquals(Double.valueOf(1), drugOrder.getDose());
        assertEquals(true, drugOrder.getPrn());

        List<TestOrder> testOrders = getOrdersOfType(orders, TestOrder.class);
        assertEquals(1, testOrders.size());
        TestOrder testOrder = testOrders.get(0);
        assertEquals("d102c80f-1yz9-4da3-bb88-8122ce8868dd", testOrder.getConcept().getUuid());
        assertEquals("a76e8d23-0c38-408c-b2a8-ea5540f01b51", testOrder.getPatient().getUuid());
        assertEquals("f13d6fae-baa9-4553-955d-920098bec08f", testOrder.getEncounter().getUuid());
        assertEquals("1a61ef2a-250c-11e3-b832-0800271c1b75", testOrder.getOrderType().getUuid());
        assertEquals("do it", testOrder.getInstructions());

    }

    private <T> List<T> getOrdersOfType(List<Order> orders, Class<T> clazz) {
        List<T> matchingOrders = new ArrayList<T>();
        for (Order order : orders) {
            if (order.getClass().equals(clazz)) {
                matchingOrders.add((T) order);
            }
        }
        return matchingOrders;
    }
}
