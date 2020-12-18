/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.fhircondition.api.translators.impl;

import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.openmrs.User;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.ConditionTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Optional;

import static org.apache.commons.lang3.Validate.notNull;

@Setter(AccessLevel.PACKAGE)
@Component("fhir.condition.conditionTranslatorImpl")
@OpenmrsProfile(openmrsPlatformVersion = "2.0.* - 2.1.*")
public class ConditionTranslatorImpl implements ConditionTranslator<Condition> {

    @Autowired
    private PatientReferenceTranslator patientReferenceTranslator;

    @Autowired
    private ConceptTranslator conceptTranslator;

    @Autowired
    private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;

    @Autowired
    private ProvenanceTranslator<Condition> provenanceTranslator;

    @Autowired
    private ConditionStatusTranslatorImpl conditionStatusTranslator;

    @Override
    public org.hl7.fhir.r4.model.Condition toFhirResource(@Nonnull Condition condition) {
        notNull(condition, "The OpenMRS Condition object should not be null");

        org.hl7.fhir.r4.model.Condition fhirCondition = new org.hl7.fhir.r4.model.Condition();
        fhirCondition.setId(condition.getUuid());
        fhirCondition.setSubject(patientReferenceTranslator.toFhirResource(condition.getPatient()));
        fhirCondition.setClinicalStatus(conditionStatusTranslator.toFhirResource(condition.getStatus()));

        if (condition.getConcept() != null) {
            fhirCondition.setCode(conceptTranslator.toFhirResource(condition.getConcept()));
            if (condition.getConditionNonCoded() != null) {
                Extension extension = new Extension();
                extension.setUrl(FhirConstants.OPENMRS_FHIR_EXT_NON_CODED_CONDITION);
                extension.setValue(new StringType(condition.getConditionNonCoded()));
               fhirCondition.addExtension(extension);
            }
        }

        fhirCondition.setOnset(new DateTimeType().setValue(condition.getOnsetDate()));
        fhirCondition.setRecorder(practitionerReferenceTranslator.toFhirResource(condition.getCreator()));
        fhirCondition.setRecordedDate(condition.getDateCreated());
        fhirCondition.getMeta().setLastUpdated(condition.getDateChanged());
        fhirCondition.addContained(provenanceTranslator.getCreateProvenance(condition));
        fhirCondition.addContained(provenanceTranslator.getUpdateProvenance(condition));

        return fhirCondition;
    }

    @Override
    public Condition toOpenmrsType(@Nonnull org.hl7.fhir.r4.model.Condition condition) {
        notNull(condition, "The Condition object should not be null");
        return this.toOpenmrsType(new Condition(), condition);
    }

    @Override
    public Condition toOpenmrsType(@Nonnull Condition existingCondition, @Nonnull org.hl7.fhir.r4.model.Condition condition) {
        notNull(existingCondition, "The existing OpenMRS Condition object should not be null");
        notNull(condition, "The Condition object should not be null");
        existingCondition.setUuid(condition.getId());
        existingCondition.setPatient(patientReferenceTranslator.toOpenmrsType(condition.getSubject()));
        existingCondition.setStatus(conditionStatusTranslator.toOpenmrsType(condition.getClinicalStatus()));

        if (!condition.getCode().isEmpty()) {
            existingCondition.setConcept(conceptTranslator.toOpenmrsType(condition.getCode()));
        }
        Optional<Extension> extension = Optional
                .ofNullable(condition.getExtensionByUrl(FhirConstants.OPENMRS_FHIR_EXT_NON_CODED_CONDITION));
        extension.ifPresent(value -> existingCondition.setConditionNonCoded(String.valueOf(value.getValue())));

        existingCondition.setOnsetDate(condition.getOnsetDateTimeType().getValue());
        existingCondition.setCreator(practitionerReferenceTranslator.toOpenmrsType(condition.getRecorder()));
        existingCondition.setDateCreated(condition.getRecordedDate());
        existingCondition.setDateChanged(condition.getMeta().getLastUpdated());

        return existingCondition;
    }
}
