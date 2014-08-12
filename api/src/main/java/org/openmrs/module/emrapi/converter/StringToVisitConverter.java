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
package org.openmrs.module.emrapi.converter;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Convert from {@link String} to {@link org.openmrs.Visit}, interpreting it as a Visit.id or uuid
 */
@Component
public class StringToVisitConverter implements Converter<String, Visit> {

    private Pattern onlyDigits = Pattern.compile("\\d+");

    @Autowired
    private VisitService visitService;

	/**
	 * @see org.springframework.core.convert.converter.Converter#convert(Object)
	 */
	@Override
	public Visit convert(String source) {
        if (StringUtils.isBlank(source)) {
            return null;
        } else if (onlyDigits.matcher(source).matches()) {
            return visitService.getVisit(Integer.valueOf(source));
        } else {
            return visitService.getVisitByUuid(source);
        }
	}
	
}
