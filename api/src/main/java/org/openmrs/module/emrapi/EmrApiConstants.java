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

package org.openmrs.module.emrapi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 *
 */
public class EmrApiConstants {


    public static final String GP_UNKNOWN_LOCATION = "emr.unknownLocation";

    public static final String GP_UNKNOWN_PROVIDER = "emr.unknownProvider";

    public static final String GP_ORDERING_PROVIDER_ENCOUNTER_ROLE = "emr.orderingProviderEncounterRole";

    public static final String GP_AT_FACILITY_VISIT_TYPE = "emr.atFacilityVisitType";

    public static final String GP_CHECK_IN_ENCOUNTER_TYPE = "emr.checkInEncounterType";

    public static final String GP_VISIT_NOTE_ENCOUNTER_TYPE = "emr.visitNoteEncounterType";

    @Deprecated      // replaced by GP_VISIT_NOTE_ENCOUNTER_TYPE, as "Visit Note" is the proper naming convention
    public static final String GP_CONSULT_ENCOUNTER_TYPE = "emr.consultEncounterType";

    public static final String GP_ADMISSION_ENCOUNTER_TYPE = "emr.admissionEncounterType";

    public static final String GP_EXIT_FROM_INPATIENT_ENCOUNTER_TYPE = "emr.exitFromInpatientEncounterType";

    public static final String GP_TRANSFER_WITHIN_HOSPITAL_ENCOUNTER_TYPE = "emr.transferWithinHospitalEncounterType";

    public static final String GP_CHECK_IN_CLERK_ENCOUNTER_ROLE = "emr.checkInClerkEncounterRole";

    public static final String GP_CLINICIAN_ENCOUNTER_ROLE = "emr.clinicianEncounterRole";

    public static final String GP_ADMISSION_FORM = "emr.admissionForm";

    public static final String GP_TRANSFER_WITHIN_HOSPITAL_FORM = "emr.transferWithinHospitalForm";

    public static final String GP_EXIT_FROM_INPATIENT_FORM = "emr.exitFromInpatientForm";

    public static final String GP_PERSON_IMAGES_DIRECTORY = "emr.personImagesDirectory";

    public static final String LOCATION_TAG_SUPPORTS_VISITS = "Visit Location";

    public static final String LOCATION_TAG_SUPPORTS_LOGIN = "Login Location";

    public static final String LOCATION_TAG_SUPPORTS_ADMISSION = "Admission Location";

    public static final String LOCATION_TAG_SUPPORTS_TRANSFER = "Transfer Location";

    public static final String DAEMON_USER_UUID = "A4F30A1B-5EB9-11DF-A648-37A07F9C90FB";

    public static final String ROLE_PREFIX_CAPABILITY = "Application Role: ";

    public static final String ROLE_PREFIX_PRIVILEGE_LEVEL = "Privilege Level: ";

    public static final String PRIVILEGE_LEVEL_FULL_ROLE = ROLE_PREFIX_PRIVILEGE_LEVEL + "Full";

    public static final String PRIVILEGE_LEVEL_FULL_DESCRIPTION = "A role that has all API privileges";

    public static final String PRIVILEGE_LEVEL_FULL_UUID = "ab2160f6-0941-430c-9752-6714353fbd3c";

    public static final String PRIVILEGE_LEVEL_HIGH_ROLE = ROLE_PREFIX_PRIVILEGE_LEVEL + "High";

    public static final String PRIVILEGE_LEVEL_HIGH_DESCRIPTION = "A role that has all API privileges except administrative privileges with security implications";

    public static final String PRIVILEGE_LEVEL_HIGH_UUID = "f089471c-e00b-468e-96e8-46aea1b339af";

    public static final String PRIVILEGE_PREFIX_APP = "App: ";

    public static final String PRIVILEGE_PREFIX_TASK = "Task: ";

    public static final String PRIVILEGE_DELETE_ENCOUNTER = "Task: emr.patient.encounter.delete";

    public static final String PRIVILEGE_EDIT_ENCOUNTER = "Task: emr.patient.encounter.edit";

    public static final String PRIVILEGE_DELETE_VISIT = "Task: emr.patient.visit.delete";

    public static final String UNKNOWN_PATIENT_PERSON_ATTRIBUTE_TYPE_NAME = "Unknown patient";

    public static final String TEST_PATIENT_ATTRIBUTE_UUID = "4f07985c-88a5-4abd-aa0c-f3ec8324d8e7";

    public static final String TELEPHONE_ATTRIBUTE_TYPE_NAME = "Telephone Number";

    public static final String PRIMARY_IDENTIFIER_TYPE = "emr.primaryIdentifierType";

    public static final String GP_EXTRA_PATIENT_IDENTIFIER_TYPES = "emr.extraPatientIdentifierTypes";

    public static final String SAME_AS_CONCEPT_MAP_TYPE_UUID = "35543629-7d8c-11e1-909d-c80aa9edcf4e";

    public static final String NARROWER_THAN_CONCEPT_MAP_TYPE_UUID = "43ac5109-7d8c-11e1-909d-c80aa9edcf4e";

    public static final String EMR_CONCEPT_SOURCE_NAME = "org.openmrs.module.emrapi";

    public static final String EMR_CONCEPT_SOURCE_DESCRIPTION = "Source used to tag concepts used in the EMR API module";

    public static final String EMR_METADATA_SOURCE_NAME = "org.openmrs.module.emrapi";

    public static final String EMR_METADATA_SOURCE_DESCRIPTION = "Source used to tag metadata used in the EMR API module";

    public static final String EMR_CONCEPT_SOURCE_UUID = "edd52713-8887-47b7-ba9e-6e1148824ca4";

    public static final String GP_DIAGNOSIS_SET_OF_SETS = "emr.concept.diagnosisSetOfSets";

	public static final String GP_NON_DIAGNOSIS_CONCEPT_SETS = "emrapi.nonDiagnosisConceptSets";

	public static final String GP_SUPPRESSED_DIAGNOSIS_CONCEPTS = "emrapi.suppressedDiagnosisConcepts";

    public static final String CONCEPT_CODE_DIAGNOSIS_CONCEPT_SET = "Diagnosis Concept Set";

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

    public static final String CONCEPT_CODE_ADMISSION_LOCATION = "Admission Location";

    public static final String CONCEPT_CODE_INTERNAL_TRANSFER_LOCATION = "Internal Transfer Location";

    public static final String CONCEPT_CODE_DATE_OF_DEATH = "Date of Death";

    public static final String CONCEPT_CODE_UNKNOWN_CAUSE_OF_DEATH = "Unknown Cause of Death";

    public static final String CONCEPT_CODE_ADMISSION_DECISION = "Admission Decision";

    public static final String CONCEPT_CODE_DENY_ADMISSION = "Deny Admission";

    public static final String USER_PROPERTY_NAME_LAST_VIEWED_PATIENT_IDS = "emrapi.lastViewedPatientIds";

    public static final String EVENT_TOPIC_NAME_PATIENT_VIEWED = "org.openmrs.module.emrapi.event.PatientViewed";

    public static final String EVENT_KEY_PATIENT_UUID = "patientUuid";

    public static final String EVENT_KEY_USER_UUID = "userUuid";

    public static final String GP_LAST_VIEWED_PATIENT_SIZE_LIMIT = "emrapi.lastViewedPatientSizeLimit";

    public static final int DEFAULT_LAST_VIEWED_PATIENT_SIZE_LIMIT = 50;

    public static final String GP_VISIT_EXPIRE_HOURS = "emrapi.visitExpireHours";

    public static final int DEFAULT_VISIT_EXPIRE_HOURS = 12;

    /*public static final String CONCEPT_CODE_DISPOSITION = "Disposition";

    public static final String CONCEPTDISPOSITION_ANSWER_ADMIT = "Admit";

    public static final String DISPOSITION_ANSWER_DISCHARGE = "Discharge";

    public static final String DISPOSITION_ANSWER_TRANSFER = "Transfer";

    public static final String DISPOSITION_ANSWER_REFER = "Refer";

    public static final String DISPOSITION_NOTE_CONCEPT = "Disposition Note";
*/

    public static final String CONCEPT_CODE_DISPOSITION_CONCEPT = "Disposition";

    public static final String LOCATION_TAG_SUPPORTS_DISPENSING = "Dispensing Location";

    public static final String EXTRA_PATIENT_IDENTIFIER_TYPES_DESCR = "Extra Patient Identifier Types that should be displayed";
    
    public static final String GP_VISIT_ASSIGNMENT_HANDLER_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAP = "emrapi.EmrApiVisitAssignmentHandler.encounterTypeToNewVisitTypeMap";

    public static final ArrayList<String> UNSAFE_PRIVILEGES = new ArrayList<String>(Arrays.asList(
            "Share Metadata",
            "Edit Reports",
            "Add Reports",
            "Add Report Objects",
            "Edit Report Objects",
            "Manage Privileges"
    ));
}
