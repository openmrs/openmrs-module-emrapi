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

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DateMapperTest {
	
	@Test
	public void shouldConvertUTCformatToDate() throws Exception {
		String utcDateString = "2015-07-30T18:30:00.000Z";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		simpleDateFormat.setTimeZone(TimeZone.getTimeZone("IST"));
		
		Date actualDate = new DateMapper().convertUTCToDate(utcDateString);
		
		assertNotNull(actualDate);
		assertEquals("2015-07-31", simpleDateFormat.format(actualDate));
		
	}
	
	@Test(expected = RuntimeException.class)
	public void shouldThrowExceptionForWrongUTCformat() throws Exception {
		String utcDateString = "2015-07-30T11:00:00.000";
		Date actualDate = new DateMapper().convertUTCToDate(utcDateString);
	}
}
