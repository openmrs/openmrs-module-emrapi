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

import org.junit.Before;
import org.junit.Test;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.metadatamapping.MetadataSet;
import org.openmrs.module.metadatamapping.api.MetadataMappingService;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ModulePropertiesTest {

    private ModuleProperties moduleProperties;

    private AdministrationService administrationService;
    private PatientService patientService;

    @Before
    public void setUp() throws Exception {
        administrationService = mock(AdministrationService.class);
        patientService = mock(PatientService.class);

        moduleProperties = new ModuleProperties() {
        };
        moduleProperties.setAdministrationService(administrationService);
        moduleProperties.setPatientService(patientService);
    }

    @Test
    public void getPatientIdentifierTypesByGlobalProperty_shouldHandleASingleType() throws Exception {
        PatientIdentifierType identifierType = new PatientIdentifierType();
        identifierType.setUuid("some-uuid");

        when(administrationService.getGlobalProperty("oneIdentifierType")).thenReturn("some-uuid");
        when(patientService.getPatientIdentifierTypeByUuid("some-uuid")).thenReturn(identifierType);

        List<PatientIdentifierType> oneIdentifierType = moduleProperties.getPatientIdentifierTypesByGlobalProperty("oneIdentifierType", true);
        assertThat(oneIdentifierType.size(), is(1));
        assertThat(oneIdentifierType.get(0), is(identifierType));
    }

    @Test
    public void getPatientIdentifierTypesByGlobalProperty_shouldHandleMultipleTypes() throws Exception {
        PatientIdentifierType identifierType1 = new PatientIdentifierType();
        identifierType1.setUuid("uuid1");
        PatientIdentifierType identifierType2 = new PatientIdentifierType();
        identifierType1.setUuid("uuid2");

        when(administrationService.getGlobalProperty("twoIdentifierTypes")).thenReturn("uuid1,uuid2");
        when(patientService.getPatientIdentifierTypeByUuid("uuid1")).thenReturn(identifierType1);
        when(patientService.getPatientIdentifierTypeByUuid("uuid2")).thenReturn(identifierType2);

        List<PatientIdentifierType> oneIdentifierType = moduleProperties.getPatientIdentifierTypesByGlobalProperty("twoIdentifierTypes", true);
        assertThat(oneIdentifierType.size(), is(2));
        assertThat(oneIdentifierType.get(0), is(identifierType1));
        assertThat(oneIdentifierType.get(1), is(identifierType2));
    }

    @Test
    public void getIntegerByGlobalProperty_shouldParseInteger() throws Exception {
        when(administrationService.getGlobalProperty("someInteger")).thenReturn("123");
        assertThat(moduleProperties.getIntegerByGlobalProperty("someInteger"), is(123));
    }

    @Test(expected = IllegalStateException.class)
    public void getIntegerByGlobalProperty_shouldFailForUnparseableInteger() throws Exception {
        when(administrationService.getGlobalProperty("someInteger")).thenReturn("AAA");
        moduleProperties.getIntegerByGlobalProperty("someInteger");
    }

}
