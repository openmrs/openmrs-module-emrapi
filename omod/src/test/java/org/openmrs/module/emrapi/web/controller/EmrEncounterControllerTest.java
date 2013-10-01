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

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransactionResponse;
import org.openmrs.module.emrapi.encounter.exception.EncounterMatcherNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;

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

        EncounterTransactionResponse response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransactionResponse.class);

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

        EncounterTransactionResponse response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransactionResponse.class);

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

        EncounterTransactionResponse response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransactionResponse.class);

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
                            "{\"conceptUuid\":\"d102c80f-1yz9-4da3-bb88-8122ce8868dd\", \"value\":20 }, " +
                            "{\"conceptUuid\":\"e102c80f-1yz9-4da3-bb88-8122ce8868dd\", \"value\":\"text value\", \"comment\":\"overweight\"}]}";

        EncounterTransactionResponse response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransactionResponse.class);

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
    public void shouldAddNewTestOrder() throws Exception {
        executeDataSet("shouldAddNewTestOrder.xml");

        String json = "{ \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\", " +
                "\"encounterDateTime\" : \"2005-01-01T00:00:00.000+0000\", " +
                "\"testOrders\":[" +
                "{\"conceptUuid\":\"d102c80f-1yz9-4da3-bb88-8122ce8868dd\", \"instructions\":\"do it\", \"orderTypeUuid\": \"1a61ef2a-250c-11e3-b832-0800271c1b75\" }]}";

        EncounterTransactionResponse response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransactionResponse.class);

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

    @Ignore
    @Test
    public void shouldAddDiagnosesAdObservation() throws Exception {
        executeDataSet("shouldAddDiagnosisAsObservation.xml");
        String uuid = "1234-1234-1234-1234";
        Obs certaintyObservation = new Obs();
        Concept certaintyConcept = new Concept();
        ConceptName certaintyConceptName = new ConceptName();
        certaintyConceptName.setName("CONFIRMED");
        certaintyConcept.addName(certaintyConceptName);
        certaintyObservation.setConcept(certaintyConcept);

        Obs orderObservation = new Obs();
        Concept orderConcept = new Concept();
        ConceptName orderConceptName = new ConceptName();
        orderConceptName.setName("PRIMARY");
        orderConcept.addName(orderConceptName);
        orderObservation.setConcept(orderConcept);

        Obs diagnosisObservation = new Obs();
        Concept diagnosisConcept = new Concept();
        diagnosisConcept.setUuid(uuid);
        ConceptName diagnosisConceptName = new ConceptName();
        diagnosisConceptName.setName("tuberculosis");
        diagnosisConcept.addName(orderConceptName);
        diagnosisObservation.setConcept(diagnosisConcept);

        String postData = "{ \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\", " +
                "\"encounterDateTime\" : \"2005-01-01T00:00:00.000+0000\", " +
                "{\"diagnoses\":[{\"order\":\"PRIMARY\", \"certainty\": \"CONFIRMED\", \"concept\": \"Concept:" + uuid + "\"}]}";

        EncounterTransactionResponse response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", postData)), EncounterTransactionResponse.class);

        Visit visit = visitService.getVisitByUuid(response.getVisitUuid());
        Encounter encounter = visit.getEncounters().iterator().next();

        Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(false);
        assertEquals(1, obsAtTopLevel.size());
        Obs parentObservation = obsAtTopLevel.iterator().next();
        assertTrue(parentObservation.isObsGrouping());
        Set<Obs> diagnosisObservationGroupMembers = parentObservation.getGroupMembers();
        assertEquals(3, diagnosisObservationGroupMembers.size());
        assertTrue(diagnosisObservationGroupMembers.contains(matches(certaintyObservation)));
        assertTrue(diagnosisObservationGroupMembers.contains(matches(orderObservation)));
        assertTrue(diagnosisObservationGroupMembers.contains(matches(diagnosisObservation)));
    }

    public static Matcher matches(final Object expected){
        return new BaseMatcher() {
            protected Object theExpected = expected;
            public boolean matches(Object o) {
                Obs expectedObs = (Obs) theExpected;
                Obs otherObs = (Obs) o;
                return expectedObs.getConcept().getName().getName().equals(otherObs.getConcept().getName().getName()) &&
                        expectedObs.hasGroupMembers() == otherObs.hasGroupMembers() &&
                        expectedObs.getConcept().getUuid().equals(otherObs.getConcept().getUuid()) &&
                        expectedObs.getConcept().getConceptClass().equals(otherObs.getConcept().getConceptClass());
            }

            public void describeTo(Description description) {
                description.appendText(theExpected.toString());
            }
        };
    }
}
