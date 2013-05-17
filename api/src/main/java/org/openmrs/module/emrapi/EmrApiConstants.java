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

package org.openmrs.module.emrapi;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class EmrApiConstants {


    public static final String GP_UNKNOWN_LOCATION = "emr.unknownLocation";

    public static final String GP_UNKNOWN_PROVIDER = "emr.unknownProvider";

    public static final String GP_ORDERING_PROVIDER_ENCOUNTER_ROLE = "emr.orderingProviderEncounterRole";

    public static final String GP_AT_FACILITY_VISIT_TYPE = "emr.atFacilityVisitType";

    public static final String GP_CHECK_IN_ENCOUNTER_TYPE = "emr.checkInEncounterType";

    public static final String GP_CONSULT_ENCOUNTER_TYPE = "emr.consultEncounterType";

    public static final String GP_ADMISSION_ENCOUNTER_TYPE = "emr.admissionEncounterType";

    public static final String GP_DISCHARGE_ENCOUNTER_TYPE = "emr.dischargeEncounterType";

    public static final String GP_TRANSFER_WITHIN_HOSPITAL_ENCOUNTER_TYPE = "emr.transferWithinHospitalEncounterType";

    public static final String GP_CHECK_IN_CLERK_ENCOUNTER_ROLE = "emr.checkInClerkEncounterRole";

    public static final String GP_CLINICIAN_ENCOUNTER_ROLE = "emr.clinicianEncounterRole";

    public static final String LOCATION_TAG_SUPPORTS_VISITS = "Visit Location";

    public static final String LOCATION_TAG_SUPPORTS_LOGIN = "Login Location";

    public static final String LOCATION_TAG_SUPPORTS_ADMISSION = "Admission Location";

    public static final String DAEMON_USER_UUID = "A4F30A1B-5EB9-11DF-A648-37A07F9C90FB";

    public static final String ROLE_PREFIX_CAPABILITY = "Application Role: ";

    public static final String ROLE_PREFIX_PRIVILEGE_LEVEL = "Privilege Level: ";

    public static final String PRIVILEGE_LEVEL_FULL_ROLE = ROLE_PREFIX_PRIVILEGE_LEVEL + "Full";

    public static final String PRIVILEGE_LEVEL_FULL_DESCRIPTION = "A role that has all API privileges";

    public static final String PRIVILEGE_LEVEL_FULL_UUID = "ab2160f6-0941-430c-9752-6714353fbd3c";

    public static final String PRIVILEGE_PREFIX_APP = "App: ";

    public static final String PRIVILEGE_PREFIX_TASK = "Task: ";

    public static final String PRIVILEGE_PRINTERS_ACCESS_PRINTERS = "Printers - Access Printers";

    public static final String PRIVILEGE_PRINTERS_MANAGE_PRINTERS = "Printers - Manage Printers";

    public static final String UNKNOWN_PATIENT_PERSON_ATTRIBUTE_TYPE_NAME = "Unknown patient";

    public static final String TEST_PATIENT_ATTRIBUTE_UUID = "4f07985c-88a5-4abd-aa0c-f3ec8324d8e7";

    public static final String TELEPHONE_ATTRIBUTE_TYPE_NAME = "Telephone Number";

    public static final String PRIMARY_IDENTIFIER_TYPE = "emr.primaryIdentifierType";

    public static final String GP_EXTRA_PATIENT_IDENTIFIER_TYPES = "emr.extraPatientIdentifierTypes";

    public static final String SAME_AS_CONCEPT_MAP_TYPE_UUID = "35543629-7d8c-11e1-909d-c80aa9edcf4e";

    public static final String NARROWER_THAN_CONCEPT_MAP_TYPE_UUID = "43ac5109-7d8c-11e1-909d-c80aa9edcf4e";

    // TODO fix this to EMRAPI and emrapi
    public static final String EMR_CONCEPT_SOURCE_NAME = "org.openmrs.module.emr";

    public static final String EMR_CONCEPT_SOURCE_DESCRIPTION = "Source used to tag concepts used in the EMR module";

    public static final String EMR_CONCEPT_SOURCE_UUID = "b5e44116-778f-11e2-ad73-903c7bb72694";

    public static final String GP_DIAGNOSIS_SET_OF_SETS = "emr.concept.diagnosisSetOfSets";

    public static final String CONCEPT_CODE_DIAGNOSIS_CONCEPT_SET = "Diagnosis Concept Set";

    public static final String CONCEPT_CODE_CONSULT_FREE_TEXT_COMMENT = "Consult Free Text Comments";

    public static final String CONCEPT_CODE_CODED_DIAGNOSIS = "Coded Diagnosis";

    public static final String CONCEPT_CODE_NON_CODED_DIAGNOSIS = "Non-Coded Diagnosis";

    public static final String CONCEPT_CODE_DIAGNOSIS_ORDER = "Diagnosis Order"; // e.g. Primary or Secondary

    public static final String CONCEPT_CODE_DIAGNOSIS_ORDER_PRIMARY = "Primary";

    public static final String CONCEPT_CODE_DIAGNOSIS_ORDER_SECONDARY = "Secondary";

    public static final String CONCEPT_CODE_DIAGNOSIS_CERTAINTY = "Diagnosis Certainty"; // e.g. confirmed or presumed

    public static final String CONCEPT_CODE_DIAGNOSIS_CERTAINTY_CONFIRMED = "Confirmed";

    public static final String CONCEPT_CODE_DIAGNOSIS_CERTAINTY_PRESUMED = "Presumed";

    public static final String CONCEPT_CODE_DISPOSITION_CONCEPT_SET = "Disposition Concept Set";

    public static final String CONCEPT_CODE_DISPOSITION = "Disposition";

    public static final String CONCEPT_CODE_UNKNOWN_CAUSE_OF_DEATH = "Unknown Cause of Death";

    public static final String LOCATION_ATTRIBUTE_TYPE_NAME_TO_PRINT_ON_ID_CARD = "a5fb5770-409a-11e2-a25f-0800200c9a66";


    // remember if we add more types here to also create them in the module activator
    public static final Map<String, String> LOCATION_ATTRIBUTE_TYPE_DEFAULT_PRINTER = new HashMap<String, String>() {{
        put("ID_CARD", "b48ef9a0-38d3-11e2-81c1-0800200c9a66");
        put("LABEL", "bd6c1c10-38d3-11e2-81c1-0800200c9a66");
    }};

}
