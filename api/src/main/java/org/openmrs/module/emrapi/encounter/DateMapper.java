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

import org.apache.commons.lang.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateMapper {
	
	public Date convertUTCToDate(String date) {
		if (!StringUtils.isBlank(date)) {
			try {
				DateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				utcFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
				
				return utcFormat.parse(date);
			}
			catch (ParseException e) {
				throw new RuntimeException("Date format needs to be in UTC format. Incorrect Date:" + date + ".", e);
			}
		}
		return null;
	}
}
