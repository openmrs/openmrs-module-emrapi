/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.disposition.actions;

import java.util.Map;

/**
 *
 */
public class DispositionActionUtils {
	
	public static String getSingleRequiredParameter(Map<String, String[]> requestParameters, String parameterName) {
		String value = getSingleOptionalParameter(requestParameters, parameterName);
		if (value == null) {
			throw new IllegalArgumentException("Missing required request parameter: " + parameterName);
		}
		return value;
	}
	
	public static String getSingleOptionalParameter(Map<String, String[]> requestParameters, String parameterName) {
		String[] values = requestParameters.get(parameterName);
		if (values == null || values.length == 0) {
			return null;
		} else if (values.length > 1) {
			throw new IllegalArgumentException(
			        "Expected just one request parameter named " + parameterName + " but got " + values.length);
		} else {
			return values[0];
		}
	}
}
