/*
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
        }
        else if (values.length > 1) {
            throw new IllegalArgumentException("Expected just one request parameter named " + parameterName + " but got " + values.length);
        }
        else {
            return values[0];
        }
    }
}
