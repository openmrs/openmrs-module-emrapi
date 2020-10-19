/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.fhir2conditions.api.translators.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.emrapi.conditionslist.Condition;
import org.openmrs.module.fhir2.FhirConstants;

public class ConditionStatusTranslatorImplTest {

    private static final String ACTIVE = "active";

    private static final String INACTIVE = "inactive";

    private ConditionStatusTranslatorImpl statusTranslator;

    @Before
    public void setUp() {
        this.statusTranslator = new ConditionStatusTranslatorImpl();
    }

    @Test
    public void shouldMapOpenMrsActiveToFHIRActive() {
        CodeableConcept codeableConcept = statusTranslator.toFhirResource(Condition.Status.ACTIVE);
        assertThat(codeableConcept, notNullValue());
        assertThat(codeableConcept.getCodingFirstRep().getCode(), equalTo(ACTIVE));
        assertThat(codeableConcept.getCodingFirstRep().getDisplay(), equalTo("Active"));
        assertThat(codeableConcept.getCodingFirstRep().getSystem(),
                equalTo(FhirConstants.CONDITION_CLINICAL_STATUS_SYSTEM_URI));
    }

    @Test
    public void shouldMapOpenMrsInActiveToFHIRInActive() {
        CodeableConcept codeableConcept = statusTranslator.toFhirResource(Condition.Status.INACTIVE);
        assertThat(codeableConcept, notNullValue());
        assertThat(codeableConcept.getCodingFirstRep().getCode(), equalTo(INACTIVE));
        assertThat(codeableConcept.getCodingFirstRep().getDisplay(), equalTo("Inactive"));
        assertThat(codeableConcept.getCodingFirstRep().getSystem(),
                equalTo(FhirConstants.CONDITION_CLINICAL_STATUS_SYSTEM_URI));
    }

    @Test
    public void shouldMapOpenMrsHistoryOfToFHIRInActive() {
        CodeableConcept codeableConcept = statusTranslator.toFhirResource(Condition.Status.HISTORY_OF);
        assertThat(codeableConcept, notNullValue());
        assertThat(codeableConcept.getCodingFirstRep().getCode(), equalTo(INACTIVE));
        assertThat(codeableConcept.getCodingFirstRep().getSystem(),
                equalTo(FhirConstants.CONDITION_CLINICAL_STATUS_SYSTEM_URI));
    }

    @Test
    public void shouldMapFHIRActiveToOpenMrsActiveClinicalCondition() {
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setCode(ACTIVE);
        coding.setSystem(FhirConstants.CONDITION_CLINICAL_STATUS_SYSTEM_URI);
        codeableConcept.addCoding(coding);
        assertThat(statusTranslator.toOpenmrsType(codeableConcept), is(Condition.Status.ACTIVE));
    }

    @Test
    public void shouldMapFHIRInActiveToOpenMrsInActive() {
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        coding.setCode(INACTIVE);
        coding.setSystem(FhirConstants.CONDITION_CLINICAL_STATUS_SYSTEM_URI);
        codeableConcept.addCoding(coding);
        assertThat(statusTranslator.toOpenmrsType(codeableConcept), is(Condition.Status.INACTIVE));
    }
}
