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

package org.openmrs.module.emrapi.utils;

import org.openmrs.User;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;

import java.util.Locale;

/**
 *
 */
public class GeneralUtils {

    /**
     * Given a user, returns the default locale (if any) for that user
     * Returns null if no default locate
     */
    public static Locale getDefaultLocale(User user) {

        if (user != null && user.getUserProperties() != null
                && user.getUserProperties().containsKey(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE)) {
            String localeString = user.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE);
            Locale locale = normalizeLocale(localeString);
            return locale;
        }

        return null;
    }

    /**
     * THIS METHOD IS COPIED FROM org.openmrs.web.WebUtil IN openmrs-core's web submodule
     * <p/>
     * This method checks if input locale string contains control characters and tries to clean up
     * actually contained ones. Also it parses locale object from string representation and
     * validates it object.
     *
     * @param localeString input string with locale parameter
     * @return locale object for input string if CTLs were cleaned up or weren't exist or null if
     *         could not to clean up CTLs from input string
     * @should ignore leading spaces
     * @should accept language only locales
     * @should not accept invalid locales
     * @should not fail with empty strings
     * @should not fail with whitespace only
     */
    public static Locale normalizeLocale(String localeString) {
        if (localeString == null)
            return null;
        localeString = localeString.trim();
        if (localeString.isEmpty())
            return null;
        int len = localeString.length();
        for (int i = 0; i < len; i++) {
            char c = localeString.charAt(i);
            // allow only ASCII letters and "_" character
            if ((c <= 0x20 || c >= 0x7f) || ((c >= 0x20 || c <= 0x7f) && (!Character.isLetter(c) && c != 0x5f))) {
                if (c == 0x09)
                    continue; // allow horizontal tabs
                localeString = localeString.replaceFirst(((Character) c).toString(), "");
                len--;
                i--;
            }
        }
        Locale locale = LocaleUtility.fromSpecification(localeString);
        if (LocaleUtility.isValid(locale))
            return locale;
        else
            return null;
    }

}
