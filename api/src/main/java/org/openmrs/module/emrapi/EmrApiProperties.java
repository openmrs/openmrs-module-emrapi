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

import org.apache.commons.lang3.math.NumberUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptSource;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.VisitType;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.metadatamapping.util.ModuleProperties;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Properties (some constant, some configured via GPs) for this module.
 */
@Component("emrApiProperties")
public class EmrApiProperties extends ModuleProperties {

	@Override
	public String getMetadataSourceName() {
		return EmrApiConstants.EMR_METADATA_SOURCE_NAME;
	}

    public Location getUnknownLocation() {
		return getEmrApiMetadataByCode(Location.class, EmrApiConstants.GP_UNKNOWN_LOCATION);
	}

	public Provider getUnknownProvider() {
		//have to use ProviderService because it returns objects of class org.openmrs.module.providermanagement.Provider
		//casted to org.openmrs.Provider, which MetadataMappingService can't handle
		return providerService.getProviderByUuid(getEmrApiMetadataUuidByCode(EmrApiConstants.GP_UNKNOWN_PROVIDER));
	}

	public EncounterRole getOrderingProviderEncounterRole() {
		return getEmrApiMetadataByCode(EncounterRole.class, EmrApiConstants.GP_ORDERING_PROVIDER_ENCOUNTER_ROLE);
	}

	public Role getFullPrivilegeLevel() {
		return userService.getRole(EmrApiConstants.PRIVILEGE_LEVEL_FULL_ROLE);
	}

    public Role getHighPrivilegeLevel(){
        return userService.getRole(EmrApiConstants.PRIVILEGE_LEVEL_HIGH_ROLE);
    }

	public EncounterType getCheckInEncounterType() {
		return getEmrApiMetadataByCode(EncounterType.class, EmrApiConstants.GP_CHECK_IN_ENCOUNTER_TYPE);
	}

	public EncounterRole getCheckInClerkEncounterRole() {
		return getEmrApiMetadataByCode(EncounterRole.class, EmrApiConstants.GP_CHECK_IN_CLERK_ENCOUNTER_ROLE);
	}

    public EncounterType getVisitNoteEncounterType() {
        try {
            return  getEmrApiMetadataByCode(EncounterType.class, EmrApiConstants.GP_VISIT_NOTE_ENCOUNTER_TYPE);
        }
        // hack for implementations who are still using old global property "consultEncounterType"
        catch (IllegalStateException ex) {
            return getConsultEncounterType();
        }
    }

    @Deprecated // use visit note encounter type, as "Visit Note" is the proper naming convention
	public EncounterType getConsultEncounterType() {
		return getEmrApiMetadataByCode(EncounterType.class, EmrApiConstants.GP_CONSULT_ENCOUNTER_TYPE);
	}

	public EncounterRole getClinicianEncounterRole() {
		return getEmrApiMetadataByCode(EncounterRole.class, EmrApiConstants.GP_CLINICIAN_ENCOUNTER_ROLE);
	}

	public EncounterType getAdmissionEncounterType() {
		return getEmrApiMetadataByCode(EncounterType.class, EmrApiConstants.GP_ADMISSION_ENCOUNTER_TYPE, false);
	}

	public EncounterType getExitFromInpatientEncounterType() {
		return getEmrApiMetadataByCode(EncounterType.class, EmrApiConstants.GP_EXIT_FROM_INPATIENT_ENCOUNTER_TYPE, false);
	}

	public EncounterType getTransferWithinHospitalEncounterType() {
		return getEmrApiMetadataByCode(EncounterType.class, EmrApiConstants.GP_TRANSFER_WITHIN_HOSPITAL_ENCOUNTER_TYPE, false);
	}

	public Form getAdmissionForm() {
		return getEmrApiMetadataByCode(Form.class, EmrApiConstants.GP_ADMISSION_FORM, false);
	}

	public Form getDischargeForm() {
		return getEmrApiMetadataByCode(Form.class, EmrApiConstants.GP_EXIT_FROM_INPATIENT_FORM, false);
	}

	public Form getTransferForm() {
		return getEmrApiMetadataByCode(Form.class, EmrApiConstants.GP_TRANSFER_WITHIN_HOSPITAL_FORM, false);
	}

	public int getVisitExpireHours() {
        return NumberUtils.toInt(getGlobalProperty(EmrApiConstants.GP_VISIT_EXPIRE_HOURS, false), EmrApiConstants.DEFAULT_VISIT_EXPIRE_HOURS);
	}

	public VisitType getAtFacilityVisitType() {
		return getEmrApiMetadataByCode(VisitType.class, EmrApiConstants.GP_AT_FACILITY_VISIT_TYPE);
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

	public LocationTag getSupportsTransferLocationTag() {
		return locationService.getLocationTagByName(EmrApiConstants.LOCATION_TAG_SUPPORTS_TRANSFER);
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
		return getEmrApiMetadataByCode(PatientIdentifierType.class, EmrApiConstants.PRIMARY_IDENTIFIER_TYPE, true);
	}

	public List<PatientIdentifierType> getExtraPatientIdentifierTypes() {
		return getPatientIdentifierTypesByCode(EmrApiConstants.GP_EXTRA_PATIENT_IDENTIFIER_TYPES);
	}

	public DiagnosisMetadata getDiagnosisMetadata() {
		return new DiagnosisMetadata(conceptService, getEmrApiConceptSource());
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

	public Concept getUnknownCauseOfDeathConcept() {
		return getEmrApiConceptByMapping(EmrApiConstants.CONCEPT_CODE_UNKNOWN_CAUSE_OF_DEATH);
	}

    public Concept getAdmissionDecisionConcept()  {
        return getEmrApiConceptByMapping(EmrApiConstants.CONCEPT_CODE_ADMISSION_DECISION);
    }

    public Concept getDenyAdmissionConcept()  {
        return getEmrApiConceptByMapping(EmrApiConstants.CONCEPT_CODE_DENY_ADMISSION);
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
		String diagnosisSetsUuid = getGlobalProperty(EmrApiConstants.GP_DIAGNOSIS_SET_OF_SETS, true);
		if (StringUtils.hasText(diagnosisSetsUuid)) {
			Concept setOfSets = conceptService.getConceptByUuid(diagnosisSetsUuid);
			if (setOfSets == null) {
				throw new IllegalStateException("Configuration required: " + EmrApiConstants.GP_DIAGNOSIS_SET_OF_SETS);
			}
			return setOfSets.getSetMembers();
		} else {
			return null;
		}
	}

	public Collection<Concept> getNonDiagnosisConceptSets() {
		Collection<Concept> concepts = getConceptsByGlobalProperty(EmrApiConstants.GP_NON_DIAGNOSIS_CONCEPT_SETS);

		for (Concept concept : concepts) {
			if (!concept.isSet()) {
				throw new IllegalStateException("Invalid configuration: concept '" + concept.getUuid() + "' defined in " + EmrApiConstants.GP_NON_DIAGNOSIS_CONCEPT_SETS + " is not a concept set");
			}
		}
		return concepts;
	}

	public Collection<Concept> getSuppressedDiagnosisConcepts() {
		return getConceptsByGlobalProperty(EmrApiConstants.GP_SUPPRESSED_DIAGNOSIS_CONCEPTS);
	}

	public ConceptMapType getSameAsConceptMapType() {
		return conceptService.getConceptMapTypeByUuid(EmrApiConstants.SAME_AS_CONCEPT_MAP_TYPE_UUID);
	}

	public ConceptMapType getNarrowerThanConceptMapType() {
		return conceptService.getConceptMapTypeByUuid(EmrApiConstants.NARROWER_THAN_CONCEPT_MAP_TYPE_UUID);
	}

    public Integer getLastViewedPatientSizeLimit() {
        String limit = administrationService.getGlobalProperty(EmrApiConstants.GP_LAST_VIEWED_PATIENT_SIZE_LIMIT);
        if (StringUtils.hasText(limit)) {
            try {
                return Integer.valueOf(limit);
            }
            catch (NumberFormatException e) {

            }
        }
        return EmrApiConstants.DEFAULT_LAST_VIEWED_PATIENT_SIZE_LIMIT;
    }

    public File getPersonImageDirectory() {
        String personImagesDir = getGlobalProperty(EmrApiConstants.GP_PERSON_IMAGES_DIRECTORY, false);
        if (personImagesDir == null || personImagesDir.isEmpty()) {
            File appDataDirectory = new File(OpenmrsUtil.getApplicationDataDirectory());
            personImagesDir =  appDataDirectory.getAbsolutePath() + "/person_images";
        }

        return new File(personImagesDir);
    }
}
