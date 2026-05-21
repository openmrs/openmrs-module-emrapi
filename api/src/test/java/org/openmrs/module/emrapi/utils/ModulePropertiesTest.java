/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.utils;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;

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
		
		moduleProperties = new ModuleProperties() {};
		moduleProperties.setAdministrationService(administrationService);
		moduleProperties.setPatientService(patientService);
	}
	
	@Test
	public void getPatientIdentifierTypesByGlobalProperty_shouldHandleASingleType() throws Exception {
		PatientIdentifierType identifierType = new PatientIdentifierType();
		identifierType.setUuid("some-uuid");
		
		when(administrationService.getGlobalProperty("oneIdentifierType")).thenReturn("some-uuid");
		when(patientService.getPatientIdentifierTypeByUuid("some-uuid")).thenReturn(identifierType);
		
		List<PatientIdentifierType> oneIdentifierType = moduleProperties
		        .getPatientIdentifierTypesByGlobalProperty("oneIdentifierType", true);
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
		
		List<PatientIdentifierType> oneIdentifierType = moduleProperties
		        .getPatientIdentifierTypesByGlobalProperty("twoIdentifierTypes", true);
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
