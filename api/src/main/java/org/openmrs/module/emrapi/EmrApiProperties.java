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

import org.openmrs.Concept;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptSource;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.LocationAttributeType;
import org.openmrs.LocationTag;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.VisitType;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.utils.ModuleProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Properties (some constant, some configured via GPs) for this module.
 */
@Component("emrApiProperties")
public class EmrApiProperties extends ModuleProperties {

    public Location getUnknownLocation() {
        return getLocationByGlobalProperty(EmrApiConstants.GP_UNKNOWN_LOCATION);
    }

    public Provider getUnknownProvider() {
        return getProviderByGlobalProperty(EmrApiConstants.GP_UNKNOWN_PROVIDER);
    }

    public EncounterRole getOrderingProviderEncounterRole() {
        return getEncounterRoleByGlobalProperty(EmrApiConstants.GP_ORDERING_PROVIDER_ENCOUNTER_ROLE);
    }

    public Role getFullPrivilegeLevel() {
        return userService.getRole(EmrApiConstants.PRIVILEGE_LEVEL_FULL_ROLE);
    }

    public EncounterType getCheckInEncounterType() {
        return getEncounterTypeByGlobalProperty(EmrApiConstants.GP_CHECK_IN_ENCOUNTER_TYPE);
    }

    public EncounterRole getCheckInClerkEncounterRole() {
        return getEncounterRoleByGlobalProperty(EmrApiConstants.GP_CHECK_IN_CLERK_ENCOUNTER_ROLE);
    }

    public EncounterType getConsultEncounterType() {
        return getEncounterTypeByGlobalProperty(EmrApiConstants.GP_CONSULT_ENCOUNTER_TYPE);
    }

    public EncounterRole getClinicianEncounterRole() {
        return getEncounterRoleByGlobalProperty(EmrApiConstants.GP_CLINICIAN_ENCOUNTER_ROLE);
    }

    public EncounterType getAdmissionEncounterType() {
        return getEncounterTypeByGlobalProperty(EmrApiConstants.GP_ADMISSION_ENCOUNTER_TYPE, false);
    }

    public EncounterType getDischargeEncounterType() {
        return getEncounterTypeByGlobalProperty(EmrApiConstants.GP_DISCHARGE_ENCOUNTER_TYPE, false);
    }

    public EncounterType getTransferWithinHospitalEncounterType() {
        return getEncounterTypeByGlobalProperty(EmrApiConstants.GP_TRANSFER_WITHIN_HOSPITAL_ENCOUNTER_TYPE, false);
    }

    public int getVisitExpireHours() {
        return 12;
    }

    public VisitType getAtFacilityVisitType() {
        return getVisitTypeByGlobalProperty(EmrApiConstants.GP_AT_FACILITY_VISIT_TYPE);
    }

    public LocationTag getSupportsVisitsLocationTag() {
        return locationService.getLocationTagByName(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS);
    }

    public LocationTag getSupportsLoginLocationTag() {
        return locationService.getLocationTagByName(EmrApiConstants.LOCATION_TAG_SUPPORTS_LOGIN);
    }

    public LocationTag getSupportsAdmissionLocationTag() {
        return locationService.getLocationTagByName(EmrApiConstants.LOCATION_TAG_SUPPORTS_ADMISSION);
    }

    public PersonAttributeType getTestPatientPersonAttributeType() {
        PersonAttributeType type = null;
        type = personService.getPersonAttributeTypeByUuid(EmrApiConstants.TEST_PATIENT_ATTRIBUTE_UUID);
        if (type == null) {
            throw new IllegalStateException("Configuration required: Test Patient Attribute UUID");
        }
        return type;
    }

    public PersonAttributeType getTelephoneAttributeType() {
        PersonAttributeType type = null;
        type = personService.getPersonAttributeTypeByName(EmrApiConstants.TELEPHONE_ATTRIBUTE_TYPE_NAME);
        if (type == null) {
            throw new IllegalStateException("Configuration required: " + EmrApiConstants.TELEPHONE_ATTRIBUTE_TYPE_NAME);
        }
        return type;
    }

    public PersonAttributeType getUnknownPatientPersonAttributeType() {
        PersonAttributeType type = null;
        type = personService.getPersonAttributeTypeByName(EmrApiConstants.UNKNOWN_PATIENT_PERSON_ATTRIBUTE_TYPE_NAME);
        if (type == null) {
            throw new IllegalStateException("Configuration required: " + EmrApiConstants.UNKNOWN_PATIENT_PERSON_ATTRIBUTE_TYPE_NAME);
        }
        return type;
    }

    public PatientIdentifierType getPrimaryIdentifierType() {
        return getPatientIdentifierTypeByGlobalProperty(EmrApiConstants.PRIMARY_IDENTIFIER_TYPE, true);
    }

    public List<PatientIdentifierType> getExtraPatientIdentifierTypes() {
        return getPatientIdentifierTypesByGlobalProperty(EmrApiConstants.GP_EXTRA_PATIENT_IDENTIFIER_TYPES, false);
    }

    public DiagnosisMetadata getDiagnosisMetadata() {
        return new DiagnosisMetadata(conceptService, getEmrApiConceptSource());
    }

    public DispositionDescriptor getDispositionDescriptor() {
        return new DispositionDescriptor(conceptService);
    }

    public List<ConceptSource> getConceptSourcesForDiagnosisSearch() {
        ConceptSource icd10 = conceptService.getConceptSourceByName("ICD-10-WHO");
        if (icd10 != null) {
            return Arrays.asList(icd10);
        } else {
            return null;
        }
    }

    public ConceptSource getEmrApiConceptSource() {
        return conceptService.getConceptSourceByName(EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
    }

    protected Concept getEmrApiConceptByMapping(String code) {
        return getSingleConceptByMapping(getEmrApiConceptSource(), code);
    }

    public Concept getConsultFreeTextCommentsConcept() {
        return getEmrApiConceptByMapping(EmrApiConstants.CONCEPT_CODE_CONSULT_FREE_TEXT_COMMENT);
    }

    public Concept getUnknownCauseOfDeathConcept() {
        return getEmrApiConceptByMapping(EmrApiConstants.CONCEPT_CODE_UNKNOWN_CAUSE_OF_DEATH);
    }

    public LocationAttributeType getLocationAttributeTypeNameToPrintOnIdCard() {
        LocationAttributeType type = null;
        type = locationService.getLocationAttributeTypeByUuid(EmrApiConstants.LOCATION_ATTRIBUTE_TYPE_NAME_TO_PRINT_ON_ID_CARD);
        if (type == null) {
            throw new IllegalStateException("Configuration required: " + EmrApiConstants.LOCATION_ATTRIBUTE_TYPE_NAME_TO_PRINT_ON_ID_CARD);
        }
        return type;
    }

    public List<PatientIdentifierType> getIdentifierTypesToSearch() {
        ArrayList<PatientIdentifierType> types = new ArrayList<PatientIdentifierType>();
        types.add(getPrimaryIdentifierType());
//        PatientIdentifierType paperRecordIdentifierType = getPaperRecordIdentifierType();
//        if (paperRecordIdentifierType != null) {
//            types.add(paperRecordIdentifierType);
//        }
        List<PatientIdentifierType> extraPatientIdentifierTypes = getExtraPatientIdentifierTypes();
        if (extraPatientIdentifierTypes != null && extraPatientIdentifierTypes.size() > 0) {
            types.addAll(extraPatientIdentifierTypes);
        }
        return types;
    }

    /**
     * Expects there to be a GP configured to point to a concept set, which is a set of other concept sets.
     * E.g. "HUM Diagnosis Sets" contains "HUM Outpatient Diagnosis Set", "HUM ER Diagnosis Set", etc.
     *
     * @return
     */
    public Collection<Concept> getDiagnosisSets() {
        String gp = getGlobalProperty(EmrApiConstants.GP_DIAGNOSIS_SET_OF_SETS, false);
        if (StringUtils.hasText(gp)) {
            Concept setOfSets = conceptService.getConceptByUuid(gp);
            if (setOfSets == null) {
                throw new IllegalStateException("Configuration required: " + EmrApiConstants.GP_DIAGNOSIS_SET_OF_SETS);
            }
            return setOfSets.getSetMembers();
        } else {
            return null;
        }
    }

    public ConceptMapType getSameAsConceptMapType() {
        return conceptService.getConceptMapTypeByUuid(EmrApiConstants.SAME_AS_CONCEPT_MAP_TYPE_UUID);
    }

    public ConceptMapType getNarrowerThanConceptMapType() {
        return conceptService.getConceptMapTypeByUuid(EmrApiConstants.NARROWER_THAN_CONCEPT_MAP_TYPE_UUID);
    }

}
