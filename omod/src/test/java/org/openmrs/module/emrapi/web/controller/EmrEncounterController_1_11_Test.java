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

import org.apache.commons.lang3.time.DateUtils;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.ObsService;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.EncounterMatcherNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:moduleApplicationContext.xml"}, inheritLocations = true)
public class EmrEncounterController_1_11_Test extends BaseEmrControllerTest {

    @Autowired
    private VisitService visitService;
    @Autowired
    private ObsService obsService;
    private String dateTimeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    
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
        Date encounterDateTime = new SimpleDateFormat(dateTimeFormat).parse(encounterDateTimeString);

        String json = "{ \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                "\"encounterDateTime\" : \"" + encounterDateTimeString + "\", " +
                "\"visitLocationUuid\": \"f1771d8e-bf1f-4dc5-957f-0d40a5eebf08\", " +
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
                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\"," +
                "\"visitLocationUuid\": \"f1771d8e-bf1f-4dc5-957f-0d40a5eebf08\" }";

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
        String encounterDateTime = "2005-01-02T00:00:00.000+0000";
        String json = "{ \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                        "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                        "\"visitLocationUuid\": \"f1771d8e-bf1f-4dc5-957f-0d40a5eebf08\", " +
                        "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\", " +
                        "\"encounterDateTime\" : \"" + encounterDateTime + "\", " +
                        "\"observations\":[" +
                            "{\"concept\": {\"uuid\": \"d102c80f-1yz9-4da3-bb88-8122ce8868dd\"}, \"conceptName\":\"Should be Ignored\", \"value\":20}, " +
                            "{\"concept\": {\"uuid\": \"8f8e7340-a067-11e3-a5e2-0800200c9a66\"}, \"value\": {\"uuid\": \"e7167090-a067-11e3-a5e2-0800200c9a66\"}}, " +
                            "{\"concept\": {\"uuid\": \"e102c80f-1yz9-4da3-bb88-8122ce8868dd\"}, \"value\":\"text value\", \"comment\":\"overweight\"}]}";

        MockHttpServletResponse response1 = handle(newPostRequest("/rest/emrapi/encounter", json));

        EncounterTransaction response = deserialize(response1, EncounterTransaction.class);

        Visit visit = visitService.getVisitByUuid(response.getVisitUuid());
        Encounter encounter = visit.getEncounters().iterator().next();

        assertEquals(3, encounter.getObs().size());
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
//        TODO : change the observation startTime logic to take current time as start time when startTime is not passed by the client
//        assertEquals(DateUtils.parseDate(encounterDateTime, dateTimeFormat), textObservation.getObsDatetime());

        assertEquals("e7167090-a067-11e3-a5e2-0800200c9a66", map.get(ConceptDatatype.CODED).getValueCoded().getUuid());
        assertEquals(new Double(20.0), map.get(ConceptDatatype.NUMERIC).getValueNumeric());
    }

    
    @Test
    public void shouldAddNewObservationGroup() throws Exception {
        executeDataSet("shouldAddNewObservation.xml");
        String encounterDateTime = "2005-01-02T00:00:00.000+0000";
        String observationTime = "2005-01-02T12:00:00.000+0000";
        String json = "{ \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                "\"visitLocationUuid\": \"f1771d8e-bf1f-4dc5-957f-0d40a5eebf08\", " +
                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\", " +
                "\"encounterDateTime\" : \"" + encounterDateTime + "\", " +
                "\"observations\":[" +
                "{\"concept\":{\"uuid\": \"e102c80f-1yz9-4da3-bb88-8122ce8868dd\"}, " +
                " \"groupMembers\" : [{\"concept\":{\"uuid\": \"d102c80f-1yz9-4da3-bb88-8122ce8868dd\"}, \"value\":20, \"comment\":\"overweight\", \"observationDateTime\": \"" + observationTime + "\"}] }" +
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
        assertEquals(new SimpleDateFormat(dateTimeFormat).parse(observationTime), member.getObsDatetime());
    }

    
    @Test
    public void shouldUpdateObservations() throws Exception {
        executeDataSet("shouldUpdateObservations.xml");

        String json = "{ \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                "\"visitLocationUuid\": \"f1771d8e-bf1f-4dc5-957f-0d40a5eebf08\", " +
                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\"," +
                "\"encounterDateTime\" : \"2013-01-01T00:00:00.000+0000\", " +
                "\"observations\":[" +
                "{\"uuid\":\"z9fb7f47-e80a-4056-9285-bd798be13c63\", " +
                " \"groupMembers\" : [{\"uuid\":\"ze48cdcb-6a76-47e3-9f2e-2635032f3a9a\", \"value\":20, \"comment\":\"new gc\" }] }, " +
                "{\"uuid\":\"zf616900-5e7c-4667-9a7f-dcb260abf1de\", \"comment\" : \"new c\", \"value\":100 }" +
                "]}";

        EncounterTransaction response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransaction.class);

        Visit visit = visitService.getVisitByUuid(response.getVisitUuid());
        Encounter encounter = (Encounter) visit.getEncounters().toArray()[0];

        Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(false);
        assertEquals(2, obsAtTopLevel.size());

        List<String> allObsUuids = getAllObsUuids(obsAtTopLevel);
        assertTrue(allObsUuids.contains("z9fb7f47-e80a-4056-9285-bd798be13c63"));
        assertTrue(allObsUuids.contains("zf616900-5e7c-4667-9a7f-dcb260abf1de"));

        Obs obs1 = obsService.getObsByUuid("z9fb7f47-e80a-4056-9285-bd798be13c63");
        assertEquals(1, obs1.getGroupMembers().size());
        Obs member = obs1.getGroupMembers().iterator().next();
        assertEquals(new Double(20), member.getValueNumeric());
        assertEquals("new gc", member.getComment());

        Obs obs2 = obsService.getObsByUuid("zf616900-5e7c-4667-9a7f-dcb260abf1de");
        assertEquals("zf616900-5e7c-4667-9a7f-dcb260abf1de", obs2.getUuid());
        assertEquals(new Double(100), obs2.getValueNumeric());
        assertEquals("new c", obs2.getComment());

    }

    
    @Test
    public void shouldGetEncounterTransactionByDate() throws Exception {
        executeDataSet("baseMetaData.xml");
        executeDataSet("dispositionMetaData.xml");
        executeDataSet("diagnosisMetaData.xml");
        executeDataSet("shouldGetEncounterTransactionByDate.xml");
        String dispositionDateTime = "2005-01-01T01:00:00.000+0000";
        String encounter1PostData = "{" +
                    "\"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                    "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                    "\"encounterTypeUuid\": \"4f3c2244-9d6a-439e-b88a-6e8873489ea7\", " +
                    "\"encounterDateTime\" : \"2004-01-01T10:00:00.000+0000\" " +
                "}";
        EncounterTransaction encounter1Response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", encounter1PostData)), EncounterTransaction.class);
        String cancerDiagnosisUuid = "d102c80f-1yz9-4da3-bb88-8122ce8868dh";
        String malariaDiagnosisUuid = "604dcce9-bcd9-48a8-b2f5-112743cf1db8";
        String visitUuid = encounter1Response.getVisitUuid();
        String encounter2PostData = "{" +
                "\"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                "\"visitUuid\": \"" + visitUuid + "\", " +
                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899891\", " +
                "\"encounterDateTime\" : \"2005-01-01T10:00:00.000+0000\", " +
                "\"observations\":[" +
                    "{\"" +
                        "concept\":{\"uuid\": \"4f3c2244-9d6a-439e-b88a-6e8873489ea7\"}, " +
                        "\"groupMembers\" : [{\"concept\":{\"uuid\": \"82e5f23e-e0b3-4e53-b6bb-c09c1c7fb8b0\"}, \"value\":20, \"comment\":\"overweight\" }] " +
                    "}" +
                "]," +
                "\"diagnoses\":[" +
                    "{\"order\":\"PRIMARY\", \"certainty\": \"CONFIRMED\", \"codedAnswer\": { \"uuid\": \"" + cancerDiagnosisUuid + "\"} }," +
                    "{\"order\":\"PRIMARY\", \"certainty\": \"CONFIRMED\", \"codedAnswer\": { \"uuid\": \"" + malariaDiagnosisUuid + "\"} }" +
                "], " +
                "\"disposition\": {" +
                "    \"code\": \"ADMIT\"," +
                "    \"dispositionDateTime\": \"" + dispositionDateTime + "\"," +
                "    \"additionalObs\": [" +
                "        {" +
                "            \"value\": \"Admit him to ICU.\"," +
                "            \"concept\": {" +
                "                \"uuid\": \"9169366f-3c7f-11e3-8f4c-005056823ee5\"," +
                "                \"name\": \"Disposition Note\"" +
                "            }" +
                "        }" +
                "    ]" +
                "}" +
                "}";
        EncounterTransaction encounter2Response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", encounter2PostData)), EncounterTransaction.class);
        assertEquals(encounter1Response.getVisitUuid(), encounter2Response.getVisitUuid());
        assertNotEquals(encounter1Response.getEncounterUuid(), encounter2Response.getEncounterUuid());

        List<EncounterTransaction> encounterTransactions = deserialize(handle(newGetRequest("/rest/emrapi/encounter",
                new Parameter[]{new Parameter("visitUuid", visitUuid), new Parameter("encounterDate", "2005-01-01"),
                		new Parameter("patientUuid", "a76e8d23-0c38-408c-b2a8-ea5540f01b51"),
                		new Parameter("visitTypeUuids", "b45ca846-c79a-11e2-b0c0-8e397087571c"),
                		new Parameter("encounterTypeUuids", "2b377dba-62c3-4e53-91ef-b51c68899891"),
                		new Parameter("includeAll", "false")})), new TypeReference<List<EncounterTransaction>>() {});


        assertEquals(1, encounterTransactions.size());
        EncounterTransaction fetchedEncounterTransaction = encounterTransactions.get(0);
        assertEquals(visitUuid, fetchedEncounterTransaction.getVisitUuid());
        assertEquals("a76e8d23-0c38-408c-b2a8-ea5540f01b51", fetchedEncounterTransaction.getPatientUuid());
        assertEquals("b45ca846-c79a-11e2-b0c0-8e397087571c", fetchedEncounterTransaction.getVisitTypeUuid());
        assertEquals("2b377dba-62c3-4e53-91ef-b51c68899891", fetchedEncounterTransaction.getEncounterTypeUuid());
        assertEquals("2005-01-01", new SimpleDateFormat("yyyy-MM-dd").format(fetchedEncounterTransaction.getEncounterDateTime()));
        //Assert Observations
        assertEquals(1, fetchedEncounterTransaction.getObservations().size());
        assertEquals("4f3c2244-9d6a-439e-b88a-6e8873489ea7", fetchedEncounterTransaction.getObservations().get(0).getConcept().getUuid());
        assertEquals(1, fetchedEncounterTransaction.getObservations().get(0).getGroupMembers().size());
        assertEquals("82e5f23e-e0b3-4e53-b6bb-c09c1c7fb8b0", fetchedEncounterTransaction.getObservations().get(0).getGroupMembers().get(0).getConcept().getUuid());
        assertEquals(20.0, fetchedEncounterTransaction.getObservations().get(0).getGroupMembers().get(0).getValue());
        //Assert Diagnosis data
        assertEquals(2, fetchedEncounterTransaction.getDiagnoses().size());
        EncounterTransaction.Diagnosis cancerDiagnosis = getDiagnosisByUuid(fetchedEncounterTransaction.getDiagnoses(), cancerDiagnosisUuid);
        assertNotNull(cancerDiagnosis);
        assertEquals("PRIMARY", cancerDiagnosis.getOrder());
        assertEquals("CONFIRMED", cancerDiagnosis.getCertainty());
        assertEquals(cancerDiagnosisUuid, cancerDiagnosis.getCodedAnswer().getUuid());
        assertNotNull(getDiagnosisByUuid(fetchedEncounterTransaction.getDiagnoses(), malariaDiagnosisUuid));
        //Assert Disposition data
        EncounterTransaction.Disposition fetchedDisposition = fetchedEncounterTransaction.getDisposition();
        assertEquals("ADMIT", fetchedDisposition.getCode());
        assertNotNull(fetchedDisposition.getExistingObs());
        assertEquals(1, fetchedDisposition.getAdditionalObs().size());
        assertEquals("Admit him to ICU.", fetchedDisposition.getAdditionalObs().get(0).getValue());
        assertEquals("Disposition Note", fetchedDisposition.getAdditionalObs().get(0).getConcept().getName());
    }

    private EncounterTransaction.Diagnosis getDiagnosisByUuid(List<EncounterTransaction.Diagnosis> diagnoses, String diagnosisUuid) {
        for (EncounterTransaction.Diagnosis diagnose : diagnoses) {
            if(diagnose.getCodedAnswer().getUuid().equals(diagnosisUuid))
                return diagnose;
        }
        return null;
    }

    
    @Test
    public void shouldAddDiagnosesAdObservation() throws Exception {
        executeDataSet("baseMetaData.xml");
        executeDataSet("diagnosisMetaData.xml");
        executeDataSet("shouldAddDiagnosisAsObservation.xml");
        String cancerDiagnosisUuid = "d102c80f-1yz9-4da3-bb88-8122ce8868dh";
        String encounterDateTime = "2005-01-02T00:00:00.000+0000";
        String diagnosisDateTime = "2005-01-02T01:00:00.000+0000";
        String postData = "{" +
                                "\"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                                "\"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899891\", " +
                                "\"encounterDateTime\" : \"" + encounterDateTime + "\", " +
                                "\"diagnoses\":[" +
                                    "{\"order\":\"PRIMARY\", \"certainty\": \"CONFIRMED\", \"codedAnswer\": { \"uuid\": \"" + cancerDiagnosisUuid + "\"}, \"diagnosisDateTime\": \""+ diagnosisDateTime + "\" }" +
                                "]" +
                           "}";

        EncounterTransaction response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", postData)), EncounterTransaction.class);

        Visit visit = visitService.getVisitByUuid(response.getVisitUuid());
        Encounter encounter = visit.getEncounters().iterator().next();

        Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(false);
        assertEquals(1, obsAtTopLevel.size());
        Obs parentObservation = obsAtTopLevel.iterator().next();
        assertTrue(parentObservation.isObsGrouping());
        assertEquals(DateUtils.parseDate(diagnosisDateTime, dateTimeFormat), parentObservation.getObsDatetime());

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
    public void shouldGetAllEncounterTransactionsWhenDateNotProvided() throws Exception {
        executeDataSet("baseMetaData.xml");
        executeDataSet("dispositionMetaData.xml");
        executeDataSet("diagnosisMetaData.xml");
        executeDataSet("shouldGetEncounterTransactionByDate.xml");

        String firstEncounter = "{" +
                "\"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", " +
                "\"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", " +
                "\"encounterTypeUuid\": \"4f3c2244-9d6a-439e-b88a-6e8873489ea7\", " +
                "\"encounterDateTime\" : \"2004-01-01T10:00:00.000+0000\" " +
                "}";
        EncounterTransaction encounter1Response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", firstEncounter)), EncounterTransaction.class);
        String visitUuid = encounter1Response.getVisitUuid();

        List<EncounterTransaction> encounterTransactions = deserialize(handle(newGetRequest("/rest/emrapi/encounter",
                new Parameter[]{new Parameter("visitUuid", visitUuid),
                		new Parameter("patientUuid", "a76e8d23-0c38-408c-b2a8-ea5540f01b51"),
                		new Parameter("includeAll", "true")})), new TypeReference<List<EncounterTransaction>>() {});

        assertEquals(1, encounterTransactions.size());
    }

    private List<String> getAllObsUuids(Set<Obs> obsAtTopLevel) {
        ArrayList<String> obsUuids = new ArrayList<String>();
        for (Obs observation : obsAtTopLevel) {
            obsUuids.add(observation.getUuid());
        }
        return obsUuids;
    }

}
