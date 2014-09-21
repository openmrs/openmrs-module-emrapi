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

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.util.OpenmrsUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Date;

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

    public static PatientIdentifierType getPatientIdentifierType(String id) {
        return getPatientIdentifierType(id, Context.getPatientService());
    }

    /**
     * Get the patient identifier type by: 1)an integer id like 5090 or 2) uuid like
     * "a3e12268-74bf-11df-9768-17cfc9833272" or 3) a name like "Temporary Identifier"
     *
     * @param id
     * @param patientService
     * @return the identifier type if exist, else null
     * @should find an identifier type by its id
     * @should find an identifier type by its uuid
     * @should find an identifier type by its name
     * @should return null otherwise
     */
    public static PatientIdentifierType getPatientIdentifierType(String id, PatientService patientService) {
        PatientIdentifierType identifierType = null;

        if (id != null) {

            // see if this is parseable int; if so, try looking up by id
            try { //handle integer: id
                int identifierTypeId = Integer.parseInt(id);
                identifierType = patientService.getPatientIdentifierType(identifierTypeId);

                if (identifierType != null) {
                    return identifierType;
                }
            } catch (Exception ex) {
                //do nothing
            }

            //handle uuid id: "a3e1302b-74bf-11df-9768-17cfc9833272", if id matches uuid format
            if (isValidUuidFormat(id)) {
                identifierType = patientService.getPatientIdentifierTypeByUuid(id);

                if (identifierType != null) {
                    return identifierType;
                }
            }
            // handle name
            else {
                // if it's neither a uuid or id, try identifier type name
                identifierType = patientService.getPatientIdentifierTypeByName(id);
            }
        }
        return identifierType;
    }

    /**
     * Determines if the passed string is in valid uuid format By OpenMRS standards, a uuid must be
     * 36 characters in length and not contain whitespace, but we do not enforce that a uuid be in
     * the "canonical" form, with alphanumerics seperated by dashes, since the MVP dictionary does
     * not use this format (We also are being slightly lenient and accepting uuids that are 37 or 38
     * characters in length, since the uuid data field is 38 characters long)
     */
    public static boolean isValidUuidFormat(String uuid) {
        if (uuid.length() < 36 || uuid.length() > 38 || uuid.contains(" ")) {
            return false;
        }

        return true;
    }

    /**
     * Gets the specified property (referenced by string) off of a person address
     * Returns null if the underlying property is null
     */
    public static String getPersonAddressProperty(PersonAddress address, String property) {
        try {
            Class<?> personAddressClass = Context.loadClass("org.openmrs.PersonAddress");
            Method getPersonAddressProperty;
            getPersonAddressProperty = personAddressClass.getMethod("get" + property.substring(0, 1).toUpperCase() + property.substring(1));
            return (String) getPersonAddressProperty.invoke(address);
        } catch (Exception e) {
            throw new APIException("Invalid property name " + property + " passed to getPersonAddressProperty");
        }
    }

    /**
     * Utility method to fetch the patient identifier for a patient of a certain type at a certain location
     * <p/>
     * Returns null if no identifiers found for that patient of that type at that location
     * If the patient has multiple identifiers for that type/location, it returns the first preferred one
     * If no preferred identifiers, returns first non-preferred one
     *
     * @param patient
     * @param patientIdentifierType
     * @param location
     * @return
     */
    public static PatientIdentifier getPatientIdentifier(Patient patient, PatientIdentifierType patientIdentifierType, Location location) {

        // TODO: add some sort of data quality flag if there are two or more identifiers of the same type and location?

        List<PatientIdentifier> patientIdentifiers = patient.getPatientIdentifiers(patientIdentifierType);

        if (patientIdentifiers == null || patientIdentifiers.size() == 0) {
            return null;
        }

        for (PatientIdentifier patientIdentifier : patientIdentifiers) {
            if (location.equals(patientIdentifier.getLocation()) && patientIdentifier.isPreferred()) {
                return patientIdentifier;
            }
        }

        for (PatientIdentifier patientIdentifier : patientIdentifiers) {
            if (location.equals(patientIdentifier.getLocation())) {
                return patientIdentifier;
            }
        }

        return null;
    }

    /**
     * Ensures that bean.propertyName is equal to newValue
     *
     * @param bean
     * @param propertyName
     * @param newValue
     * @return true if we changed the value, false if we left it as-was
     */
    public static boolean setPropertyIfDifferent(Object bean, String propertyName, Object newValue) {
        try {
            Object currentValue = PropertyUtils.getProperty(bean, propertyName);
            if (OpenmrsUtil.nullSafeEquals(currentValue, newValue)) {
                return false;
            } else {
                PropertyUtils.setProperty(bean, propertyName, newValue);
                return true;
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Gets the value of the user property
     * EmrApiConstants.USER_PROPERTY_NAME_LAST_VIEWED_PATIENT_IDS for the user as a list of patients
     * in reverse order impying the patient that was first added comes last while the last added one
     * comes first
     *
     * @param user
     * @should return a list of the patients last viewed by the specified user
     * @should not return voided patients
     */
    public static List<Patient> getLastViewedPatients(User user) {
        List<Patient> lastViewed = new ArrayList<Patient>();
        if (user != null) {
            //The user object cached in the user's context needs to be up to date
            user = Context.getUserService().getUser(user.getId());
            String lastViewedPatientIdsString = user
                    .getUserProperty(EmrApiConstants.USER_PROPERTY_NAME_LAST_VIEWED_PATIENT_IDS);
            if (StringUtils.isNotBlank(lastViewedPatientIdsString)) {
                PatientService ps = Context.getPatientService();
                lastViewedPatientIdsString = lastViewedPatientIdsString.replaceAll("\\s", "");
                String[] patientIds = lastViewedPatientIdsString.split(",");
                for (String pId : patientIds) {
                    try {
                        Patient p = ps.getPatient(Integer.valueOf(pId));
                        if (p != null && !p.isVoided() && !p.isPersonVoided()) {
                            lastViewed.add(p);
                        }
                    }
                    catch (NumberFormatException e) {}
                }
            }
        }

        Collections.reverse(lastViewed);

        return lastViewed;
    }

    public static Date getCurrentDateIfNull(Date date){
        return date == null ? new Date() : date;
    }
}
