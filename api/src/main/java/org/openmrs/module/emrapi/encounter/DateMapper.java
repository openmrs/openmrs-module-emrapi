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
            } catch (ParseException e) {
                throw new RuntimeException("Date format needs to be in UTC format. Incorrect Date:" + date + ".", e);
            }
        }
        return null;
    }
}
