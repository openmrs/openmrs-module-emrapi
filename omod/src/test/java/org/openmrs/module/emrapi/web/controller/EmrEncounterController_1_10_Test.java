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

import org.junit.*;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.module.emrapi.encounter.domain.*;
import org.springframework.beans.factory.annotation.*;

import java.util.*;

import static org.junit.Assert.*;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:moduleApplicationContext.xml"}, inheritLocations = true)
public class EmrEncounterController_1_10_Test extends BaseEmrControllerTest {

    @Autowired
    private EncounterService encounterService;

    @Test
    public void shouldAddDrugOrder() throws Exception {
        executeDataSet("shouldAddNewDrugOrder.xml");

        String json = "{  \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", \n" +
                "   \"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", \n" +
                "   \"encounterDateTime\" : \"2011-05-01T12:10:06.000+0530\", \n" +
                "   \"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\",\n" +
                "   \"providers\" : [{ \n" +
                "     \n" +
                "      \"uuid\":\"331c6bf8-7846-11e3-a96a-0800271c1b75\"\n" +
                "   }],\n" +
                "   \"drugOrders\" : [{   \"careSetting\":\"OUTPATIENT\",\n" +
                "      \"drug\":{  \n" +
                "         \"name\":\"Paracetamol\"\n" +
                "      },\n" +
                "      \"dosingInstructionsType\":\"org.openmrs.SimpleDosingInstructions\",\n" +
                "      \"orderType\":\"Drug order\",\n" +
                "      \"dosingInstructions\":{  \n" +
                "         \"dose\":2,\n" +
                "         \"doseUnits\":\"tab (s)\",\n" +
                "         \"route\":\"PO\",\n" +
                "         \"frequency\":\"QDS\",\n" +
                "         \"asNeeded\":false,\n" +
                "         \"administrationInstructions\": \"AC\",\n" +
                "         \"quantity\":16,\n" +
                "         \"quantityUnits\": \"tab (s)\",\n" +
                "         \"numRefills\":0\n" +
                "      },\n" +
                "      \"duration\": \"1\",\n" +
                "      \"durationUnits\": \"Day(s)\",\n" +
                "      \"scheduledDate\":\"2013-12-02T12:27:32.518Z\",\n" +
                "      \"endDate\":\"2014-12-04T12:27:32.518Z\",\n" +
                "      \"action\":\"NEW\"\n" +
                "   }]\n" +
                "}";

        EncounterTransaction response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransaction.class);

        assertNotNull(response);
        Set<Order> savedDrugOrders = encounterService.getEncounterByUuid("f13d6fae-baa9-4553-955d-920098bec08f").getOrders();
        assertEquals(savedDrugOrders.size(), 1);
    }

    @Test
    public void shouldReviseDrugOrder() throws Exception{

        executeDataSet("shouldReviseDrugOrder.xml");

        String json = "{  \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", \n" +
                "   \"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", \n" +
                "   \"encounterDateTime\" : \"2011-05-01T12:10:06.000+0530\", \n" +
                "   \"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\",\n" +
                "   \"providers\" : [{ \n" +
                "     \n" +
                "      \"uuid\":\"331c6bf8-7846-11e3-a96a-0800271c1b75\"\n" +
                "   }],\n" +
                "   \"drugOrders\" : [{ \n" +
                "      \"existingUuid\":\"0c96f25c-4949-4f72-9931-d808fbcdbbbb\",\n" +
                "      \"dosingInstructionsType\":\"org.openmrs.SimpleDosingInstructions\",\n" +
                "      \"dosingInstructions\":{  \n" +
                "         \"dose\":4,\n" +
                "         \"doseUnits\":\"tab (s)\",\n" +
                "         \"route\":\"PO\",\n" +
                "         \"frequency\":\"QD\",\n" +
                "         \"asNeeded\":false,\n" +
                "         \"administrationInstructions\": \"AC\",\n" +
                "         \"quantity\":160,\n" +
                "         \"quantityUnits\": \"tab (s)\",\n" +
                "         \"numRefills\":0\n" +
                "      },\n" +
                "      \"duration\": \"5\",\n" +
                "      \"durationUnits\": \"Day(s)\",\n" +
                "      \"endDate\":\"2099-12-04T12:27:32.518Z\",\n" +
                "      \"action\":\"REVISE\"\n" +
                "   }]\n" +
                "}";
        EncounterTransaction response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransaction.class);

        assertNotNull(response);
        Set<Order> savedDrugOrders = encounterService.getEncounterByUuid("f13d6fae-baa9-4553-955d-920098bec08f").getOrders();
        assertEquals(savedDrugOrders.size(), 2);
    }

    @Test
    public void shouldDiscontinueDrugOrder() throws Exception{

        executeDataSet("shouldReviseDrugOrder.xml");

        String json = "{  \"patientUuid\" : \"a76e8d23-0c38-408c-b2a8-ea5540f01b51\", \n" +
                "   \"visitTypeUuid\" : \"b45ca846-c79a-11e2-b0c0-8e397087571c\", \n" +
                "   \"encounterDateTime\" : \"2011-05-01T12:10:06.000+0530\", \n" +
                "   \"encounterTypeUuid\": \"2b377dba-62c3-4e53-91ef-b51c68899890\",\n" +
                "   \"providers\" : [{ \n" +
                "     \n" +
                "      \"uuid\":\"331c6bf8-7846-11e3-a96a-0800271c1b75\"\n" +
                "   }],\n" +
                "   \"drugOrders\" : [{ \n" +
                "      \"existingUuid\":\"0c96f25c-4949-4f72-9931-d808fbcdbbbb\",\n" +
                "      \"dosingInstructionsType\":\"org.openmrs.SimpleDosingInstructions\",\n" +
                "      \"dosingInstructions\":{  \n" +
                "         \"dose\":4,\n" +
                "         \"doseUnits\":\"tab (s)\",\n" +
                "         \"route\":\"PO\",\n" +
                "         \"frequency\":\"QD\",\n" +
                "         \"asNeeded\":false,\n" +
                "         \"administrationInstructions\": \"AC\",\n" +
                "         \"quantity\":160,\n" +
                "         \"quantityUnits\": \"tab (s)\",\n" +
                "         \"numRefills\":0\n" +
                "      },\n" +
                "      \"duration\": \"5\",\n" +
                "      \"durationUnits\": \"Day(s)\",\n" +
                "      \"endDate\":\"2099-12-04T12:27:32.518Z\",\n" +
                "      \"action\":\"DISCONTINUE\"\n" +
                "   }]\n" +
                "}";
        EncounterTransaction response = deserialize(handle(newPostRequest("/rest/emrapi/encounter", json)), EncounterTransaction.class);

        assertNotNull(response);
        Set<Order> savedDrugOrders = encounterService.getEncounterByUuid("f13d6fae-baa9-4553-955d-920098bec08f").getOrders();
        assertEquals(savedDrugOrders.size(), 2);
        assertEquals(savedDrugOrders.iterator().next().getAction().name(), "DISCONTINUE");
    }
}
