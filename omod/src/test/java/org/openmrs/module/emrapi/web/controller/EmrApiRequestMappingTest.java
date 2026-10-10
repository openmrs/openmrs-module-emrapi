/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmrs.web.test.jupiter.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ServletRequestPathUtils;

/**
 * Clients call emrapi both with and without the REST version in the URL (EA-194), so every endpoint
 * has to answer on both.
 */
public class EmrApiRequestMappingTest extends BaseModuleWebContextSensitiveTest {
	
	@Autowired
	private WebApplicationContext webApplicationContext;
	
	private RequestMappingHandlerMapping handlerMapping;
	
	@BeforeEach
	public void setUp() {
		handlerMapping = new RequestMappingHandlerMapping();
		handlerMapping.setApplicationContext(webApplicationContext);
		handlerMapping.afterPropertiesSet();
	}
	
	@ParameterizedTest
	@CsvSource({ "POST, /activevisit, ActiveVisitController.ensureActiveVisit",
	        "GET, /patientdiagnoses, DiagnosisController.getDiagnosesList",
	        "GET, /patientuniquediagnoses, DiagnosisController.getUniqueDiagnosesList",
	        "GET, /doseFormGroups, DoseFormGroupController.getDoseFormGroups",
	        "GET, /configuration, EmrApiConfigurationController.getEmrApiConfiguration",
	        "GET, /concept, EmrConceptSearchController.search", "POST, /encounter, EmrEncounterController.update",
	        "GET, /encounter, EmrEncounterController.find",
	        "GET, /encounter/active, EmrEncounterController.getActiveEncounter",
	        "GET, /encounter/some-uuid, EmrEncounterController.get",
	        "GET, /inpatient/admission, InpatientAdmissionController.getInpatientAdmissions",
	        "GET, /inpatient/request, InpatientRequestController.getInpatientRequests",
	        "GET, /inpatient/visits, InpatientVisitsController.getInpatientVisits",
	        "GET, /locationThatSupportsVisits, LocationThatSupportsVisitsController.getLocationThatSupportsVisits",
	        "GET, /maternal/mothersAndChildren, MothersAndChildrenController.getMothersAndChildren",
	        "GET, /patient/some-uuid/visit, VisitController.getVisitsWithDiagnosesAndNotesByPatient" })
	public void shouldMapTheSameHandlerWithAndWithoutTheVersion(String method, String path, String handler)
	        throws Exception {
		assertEquals(handler, getHandler(method, "/rest/v1/emrapi" + path));
		assertEquals(handler, getHandler(method, "/rest/emrapi" + path));
	}
	
	private String getHandler(String method, String uri) throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
		ServletRequestPathUtils.parseAndCache(request);
		HandlerExecutionChain chain = handlerMapping.getHandler(request);
		if (chain == null) {
			return null;
		}
		HandlerMethod handlerMethod = (HandlerMethod) chain.getHandler();
		return handlerMethod.getBeanType().getSimpleName() + "." + handlerMethod.getMethod().getName();
	}
}
