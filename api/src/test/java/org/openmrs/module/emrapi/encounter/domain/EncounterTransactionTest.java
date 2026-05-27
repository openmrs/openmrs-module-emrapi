/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
		EncounterTransaction encounterTransaction = new ObjectMapper()
		        .readValue("{" + "	\"context\": {" + "		\"" + patientProgramUuidKey + "\": \"" + patientProgramUuidValue
		                + "\"," + "		\"anotherKey\": \"anotherValue\"," + "		\"contextObject\": {"
		                + "			\"key\": \"value\"" + "		}" + "	}" + "}",
		            EncounterTransaction.class);
		Map<String, Object> context = encounterTransaction.getContext();
		assertThat(context.get(patientProgramUuidKey), is(equalTo((Object) patientProgramUuidValue)));
	}
}
