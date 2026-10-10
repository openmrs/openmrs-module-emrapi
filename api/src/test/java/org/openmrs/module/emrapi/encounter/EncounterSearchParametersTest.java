/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.encounter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EncounterSearchParametersTest {
	
	/**
	 * Bahmni posts these parameters as JSON, with the browser's offset on each date, and relies on
	 * Jackson's default date parsing.
	 */
	@Test
	public void shouldParseDatesWithTheirOffsetAndAsEpochMilliseconds() throws Exception {
		EncounterSearchParameters parameters = new ObjectMapper().readValue(
		    "{\"encounterDateTimeFrom\":\"2024-01-01T00:00:00.000+0530\",\"encounterDateTimeTo\":1704047400000}",
		    EncounterSearchParameters.class);
		
		assertEquals(Instant.parse("2023-12-31T18:30:00Z"), parameters.getEncounterDateTimeFrom().toInstant());
		assertEquals(Instant.parse("2023-12-31T18:30:00Z"), parameters.getEncounterDateTimeTo().toInstant());
	}
}
