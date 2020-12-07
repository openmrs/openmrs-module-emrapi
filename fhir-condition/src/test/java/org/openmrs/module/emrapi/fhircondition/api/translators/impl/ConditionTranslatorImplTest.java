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

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Provenance;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.translators.ConceptTranslator;
import org.openmrs.module.fhir2.api.translators.PatientReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.PractitionerReferenceTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.openmrs.module.fhir2.api.util.FhirUtils;
import uk.co.it.modular.hamcrest.date.DateMatchers;

import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ConditionTranslatorImplTest {

    private static final String CONDITION_UUID = "00af6f0f-ed07-4cef-b0f1-a76a999db987";

    private static final String PATIENT_UUID = "258797db-1524-4a13-9f09-2881580b0f5b";

    private static final String PRACTITIONER_UUID = "2ffb1a5f-bcd3-4243-8f40-78edc2642789";

    private static final String FAMILY_NAME = "Geoffry";

    private static final String GIVEN_NAME = "Janet";

    private static final String TEST_IDENTIFIER_TYPE = "test identifierType";

    private static final String IDENTIFIER = "identifier";

    private static final String SYSTEM = "urn:oid:2.16.840.1.113883.3.7201";

    private static final Integer CODE = 102309;

    private static final Integer CONDITION_NON_CODED = 5602;

    private static final String CONDITION_NON_CODED_TEXT = "condition non coded";

    private static final String CONDITION_NON_CODED_VALUE = "Other";

    private static final String CONCEPT_UUID = "31d754f5-3e9e-4ca3-805c-87f97a1f5e4b";

    private static final String PRACTITIONER_REFERENCE = FhirConstants.PRACTITIONER + "/" + PRACTITIONER_UUID;

    @Mock
    private PatientReferenceTranslator patientReferenceTranslator;

    @Mock
    private ConceptTranslator conceptTranslator;

    @Mock
    private PractitionerReferenceTranslator<User> practitionerReferenceTranslator;

    @Mock
    private ProvenanceTranslator<Condition> provenanceTranslator;

    @Mock
    private ConditionStatusTranslatorImpl conditionStatusTranslator;

    private ConditionTranslatorImpl conditionTranslator;

    private Condition openMrsCondition;

    private org.hl7.fhir.r4.model.Condition fhirCondition;

    private Patient patient;

    @Before
    public void setUp() {
        conditionTranslator = new ConditionTranslatorImpl();
        conditionTranslator.setPatientReferenceTranslator(patientReferenceTranslator);
        conditionTranslator.setConceptTranslator(conceptTranslator);
        conditionTranslator.setPractitionerReferenceTranslator(practitionerReferenceTranslator);
        conditionTranslator.setProvenanceTranslator(provenanceTranslator);
        conditionTranslator.setConditionStatusTranslator(conditionStatusTranslator);
    }

    @Before
    public void initCondition() {
        PersonName name = new PersonName();
        name.setGivenName(GIVEN_NAME);
        name.setFamilyName(FAMILY_NAME);

        PatientIdentifierType identifierType = new PatientIdentifierType();
        identifierType.setName(TEST_IDENTIFIER_TYPE);

        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setIdentifierType(identifierType);
        identifier.setIdentifier(IDENTIFIER);

        patient = new Patient();
        patient.setUuid(PATIENT_UUID);
        patient.addIdentifier(identifier);
        patient.addName(name);

        Reference patientRef = new Reference();
        patientRef.setReference(PATIENT_UUID);

        openMrsCondition = new Condition();
        openMrsCondition.setUuid(CONDITION_UUID);
        openMrsCondition.setPatient(patient);
        openMrsCondition.setStatus(Condition.Status.ACTIVE);

        fhirCondition = new org.hl7.fhir.r4.model.Condition();
        fhirCondition.setId(CONDITION_UUID);
        fhirCondition.setSubject(patientRef);
    }

    @Test
    public void shouldTranslateConditionToOpenMrsType() {
        Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
        assertThat(condition, notNullValue());
        assertThat(condition.getUuid(), notNullValue());
        assertThat(condition.getUuid(), equalTo(CONDITION_UUID));
    }

    @Test
    public void shouldTranslateConditionToFhirType() {
        org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openMrsCondition);
        assertThat(condition, notNullValue());
        assertThat(condition.getId(), notNullValue());
        assertThat(condition.getId(), equalTo(CONDITION_UUID));
    }

    @Test
    public void shouldUpdateExistingCondition() {
        org.hl7.fhir.r4.model.Condition theCondition = new org.hl7.fhir.r4.model.Condition();
        theCondition.setId(CONDITION_UUID);
        Condition condition = conditionTranslator.toOpenmrsType(openMrsCondition, theCondition);
        assertThat(condition, notNullValue());
    }

    @Test
    public void shouldTranslatePatientToSubjectFhirType() {
        Reference patientRef = new Reference();
        patientRef.setReference(PATIENT_UUID);
        when(patientReferenceTranslator.toFhirResource(openMrsCondition.getPatient())).thenReturn(patientRef);
        org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openMrsCondition);
        assertThat(condition, notNullValue());
        assertThat(condition.getSubject(), notNullValue());
        assertThat(condition.getSubject().getReference(), equalTo(PATIENT_UUID));
    }

    @Test
    public void shouldTranslateOpenMrsConditionOnsetDateToFhirType() {
        openMrsCondition.setOnsetDate(new Date());
        org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openMrsCondition);
        assertThat(condition, notNullValue());
        assertThat(condition.getOnsetDateTimeType().getValue(), notNullValue());
        assertThat(condition.getOnsetDateTimeType().getValue(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void shouldTranslateFhirConditionOnsetToOpenMrsOnsetDate() {
        DateTimeType theDateTime = new DateTimeType();
        theDateTime.setValue(new Date());
        fhirCondition.setOnset(theDateTime);
        Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
        assertThat(condition, notNullValue());
        assertThat(condition.getOnsetDate(), notNullValue());
        assertThat(condition.getOnsetDate(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void shouldTranslateConditionCodeToOpenMrsConcept() {
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setCode(CODE.toString());
        coding.setSystem(SYSTEM);
        codeableConcept.addCoding(coding);
        fhirCondition.setCode(codeableConcept);
        Concept concept = new Concept();
        concept.setUuid(CONCEPT_UUID);
        concept.setConceptId(CODE);
        when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(concept);
        Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
        assertThat(condition, notNullValue());
        assertThat(condition.getConcept(), notNullValue());
        assertThat(condition.getConcept().getConceptId(), equalTo(CODE));
    }

    @Test
    public void shouldTranslateConditionConceptToFhirType() {
        Concept concept = new Concept();
        concept.setUuid(CONCEPT_UUID);
        concept.setConceptId(CODE);
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setCode(CODE.toString());
        coding.setSystem(SYSTEM);
        codeableConcept.addCoding(coding);
        openMrsCondition.setConcept(concept);
        when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
        org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openMrsCondition);
        assertThat(condition, notNullValue());
        assertThat(condition.getCode(), notNullValue());
        assertThat(condition.getCode().getCoding(), not(Matchers.<Coding>empty()));
        assertThat(condition.getCode().getCoding().get(0).getCode(), equalTo(CODE.toString()));
        assertThat(condition.getCode().getCoding().get(0).getSystem(), equalTo(SYSTEM));
    }

    @Test
    public void shouldTranslateConditionNonCodedToOpenMrsType() {
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setCode(String.valueOf(CONDITION_NON_CODED));
        coding.setDisplay(CONDITION_NON_CODED_VALUE);
        codeableConcept.addCoding(coding);
        Concept concept = new Concept();
        concept.setConceptId(CONDITION_NON_CODED);
        fhirCondition.setCode(codeableConcept);
        fhirCondition.addExtension(FhirConstants.OPENMRS_FHIR_EXT_NON_CODED_CONDITION,
                new StringType(CONDITION_NON_CODED_TEXT));
        when(conceptTranslator.toOpenmrsType(codeableConcept)).thenReturn(concept);
        Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
        assertThat(condition, notNullValue());
        assertThat(condition.getConcept(), equalTo(concept));
        assertThat(condition.getConditionNonCoded(), notNullValue());
        assertThat(condition.getConditionNonCoded(), equalTo(CONDITION_NON_CODED_TEXT));
    }

    @Test
    public void shouldTranslateOpenMRSConditionNonCodedToFhirType() {
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setCode(String.valueOf(CONDITION_NON_CODED));
        codeableConcept.addCoding(coding);
        Concept concept = new Concept();
        concept.setConceptId(CONDITION_NON_CODED);
        openMrsCondition.setConcept(concept);
        openMrsCondition.setConditionNonCoded(CONDITION_NON_CODED_TEXT);
        when(conceptTranslator.toFhirResource(concept)).thenReturn(codeableConcept);
        org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openMrsCondition);
        assertThat(condition, notNullValue());
        assertThat(condition.getCode(), notNullValue());
        assertThat(condition.getCode().getCoding(), not(empty()));
        assertThat(condition.getCode().getCoding().get(0).getCode(), equalTo(String.valueOf(CONDITION_NON_CODED)));
    }

    @Test
    public void shouldTranslateConditionDateCreatedToRecordedDateFhirType() {
        openMrsCondition.setDateCreated(new Date());
        org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openMrsCondition);
        assertThat(condition, notNullValue());
        assertThat(condition.getRecordedDate(), notNullValue());
        assertThat(condition.getRecordedDate(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void shouldTranslateConditionRecordedDateToDateCreatedOpenMrsType() {
        fhirCondition.setRecordedDate(new Date());
        Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
        assertThat(condition, notNullValue());
        assertThat(condition.getDateCreated(), notNullValue());
        assertThat(condition.getDateCreated(), DateMatchers.sameDay(new Date()));
    }

    @Test
    public void shouldTranslateConditionRecorderToOpenmrsUser() {
        Reference userRef = new Reference();
        userRef.setReference(FhirConstants.PRACTITIONER + "/" + PRACTITIONER_UUID);
        fhirCondition.setRecorder(userRef);
        User user = new User();
        user.setUuid(PRACTITIONER_UUID);
        when(practitionerReferenceTranslator.toOpenmrsType(userRef)).thenReturn(user);
        Condition condition = conditionTranslator.toOpenmrsType(fhirCondition);
        assertThat(condition, notNullValue());
        assertThat(condition.getCreator(), notNullValue());
        assertThat(condition.getCreator().getUuid(), equalTo(PRACTITIONER_UUID));
    }

    @Test
    public void shouldTranslateConditionCreatorToRecorderFhirType() {
        User user = new User();
        user.setUuid(PRACTITIONER_UUID);
        Reference userRef = new Reference();
        userRef.setReference(PRACTITIONER_REFERENCE);
        openMrsCondition.setCreator(user);
        when(practitionerReferenceTranslator.toFhirResource(user)).thenReturn(userRef);
        org.hl7.fhir.r4.model.Condition condition = conditionTranslator.toFhirResource(openMrsCondition);
        assertThat(condition, notNullValue());
        assertThat(condition.getRecorder(), notNullValue());
        assertThat(condition.getRecorder().getReference(), equalTo(PRACTITIONER_REFERENCE));
    }

    @Test
    public void shouldAddProvenanceToConditionResource() {
        Condition condition = new Condition();
        condition.setUuid(CONDITION_UUID);
        Provenance provenance = new Provenance();
        provenance.setId(new IdType(FhirUtils.newUuid()));
        when(provenanceTranslator.getCreateProvenance(condition)).thenReturn(provenance);
        when(provenanceTranslator.getUpdateProvenance(condition)).thenReturn(provenance);

        org.hl7.fhir.r4.model.Condition result = conditionTranslator.toFhirResource(condition);
        List<Resource> resources = result.getContained();
        assertThat(resources, Matchers.notNullValue());
        assertThat(resources, Matchers.not(empty()));
        assertThat(resources.stream().findAny().isPresent(), CoreMatchers.is(true));
        assertThat(resources.stream().findAny().get().isResource(), CoreMatchers.is(true));
        assertThat(resources.stream().findAny().get().getResourceType().name(),
                Matchers.equalTo(Provenance.class.getSimpleName()));
    }
}
