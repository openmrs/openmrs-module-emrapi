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
package org.openmrs.module.emrapi.encounter.domain;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class EncounterTransactionTest {

    @Test
    public void dateConversion() {
        EncounterTransaction encounterTransaction = new EncounterTransaction();
        encounterTransaction.setEncounterDateTime(new Date(2013, 10, 30, 0, 0, 0));
        assertEquals(new Date(2013, 10, 30, 0, 0, 0), encounterTransaction.getEncounterDateTime());
    }

    @Test
    public void shouldDeserializeContext() throws IOException {
        String patientProgramUuidKey = "patientProgramUuid";
        String patientProgramUuidValue = "c4f735a8-dbac-11e5-b5d2-0a1d41d68578";
        EncounterTransaction encounterTransaction = new ObjectMapper().readValue("{" +
                "	\"context\": {" +
                "		\"" + patientProgramUuidKey + "\": \"" + patientProgramUuidValue + "\"," +
                "		\"anotherKey\": \"anotherValue\"," +
                "		\"contextObject\": {" +
                "			\"key\": \"value\"" +
                "		}" +
                "	}" +
                "}", EncounterTransaction.class);
        Map<String, Object> context = encounterTransaction.getContext();
        assertThat(context.get(patientProgramUuidKey), is(equalTo((Object) patientProgramUuidValue)));
    }
}
