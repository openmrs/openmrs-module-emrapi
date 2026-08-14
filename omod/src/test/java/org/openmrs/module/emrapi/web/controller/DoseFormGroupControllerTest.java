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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * The dose form to dose form group to route of administration mappings themselves are the
 * EmrConceptService's, and are tested in EmrConceptServiceComponentTest. What is tested here is how
 * this controller exposes them: the shape of the payload and the representation the Concepts in it
 * are converted to.
 */
public class DoseFormGroupControllerTest extends BaseModuleWebContextSensitiveTest {
	
	@Autowired
	DoseFormGroupController controller;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet("doseFormGroupDataset.xml");
	}
	
	/**
	 * Pins the response contract: the top-level keys and the nesting under each.
	 */
	@Test
	public void shouldReturnDoseFormsAndDoseFormGroups() throws Exception {
		Map<String, List<Map<String, Object>>> response = getDoseFormGroups();
		
		assertNotNull("response should contain a doseForms entry", response.get("doseForms"));
		assertNotNull("response should contain a doseFormGroups entry", response.get("doseFormGroups"));
		
		Map<String, Set<String>> groupsByDoseForm = new HashMap<String, Set<String>>();
		for (Map<String, Object> entry : response.get("doseForms")) {
			Set<String> groups = new HashSet<String>();
			for (Object group : (List<?>) entry.get("doseFormGroups")) {
				groups.add(displayOf(group));
			}
			groupsByDoseForm.put(displayOf(entry.get("doseForm")), groups);
		}
		assertEquals(new HashSet<String>(Arrays.asList("Oral Solid")), groupsByDoseForm.get("Tablet"));
		
		Map<String, Set<String>> routesByGroup = new HashMap<String, Set<String>>();
		for (Map<String, Object> entry : response.get("doseFormGroups")) {
			Set<String> routes = new HashSet<String>();
			for (Object route : (List<?>) entry.get("routes")) {
				routes.add(displayOf(route));
			}
			routesByGroup.put(displayOf(entry.get("doseFormGroup")), routes);
		}
		assertEquals(new HashSet<String>(Arrays.asList("Oral")), routesByGroup.get("Oral Solid"));
	}
	
	/**
	 * The requested representation has to reach the nested Concepts. It only does so because the
	 * response is a bean with a registered Converter: ConversionUtil converts the values of a Map with
	 * a hardcoded Representation.REF, so back when this endpoint returned a SimpleObject every Concept
	 * came back as a REF no matter what was asked for.
	 */
	@Test
	public void shouldReturnConceptsInTheRequestedRepresentation() throws Exception {
		Map<String, Object> asFull = firstDoseForm(representation("full"));
		assertTrue("expected more than a REF, got " + asFull.keySet(),
		    asFull.keySet().containsAll(Arrays.asList("uuid", "display", "datatype", "conceptClass")));
		
		// The default is REF, not DEFAULT: a Concept's default representation drags its names,
		// descriptions, mappings, answers, set members and attributes along, and CIEL ships around 86
		// dose forms. Anyone who wants more can ask for it. Pinned so that widening the default payload
		// again, which would be a breaking change for clients by then relying on it, is deliberate.
		Map<String, Object> asDefault = firstDoseForm(new MockHttpServletRequest());
		assertEquals(new HashSet<String>(Arrays.asList("uuid", "display", "links")), asDefault.keySet());
	}
	
	/**
	 * A custom representation is handled by a different branch of SimpleBeanConverter, which builds its
	 * description from the request rather than by introspecting the bean.
	 */
	@Test
	public void shouldSupportACustomRepresentation() throws Exception {
		Map<String, List<Map<String, Object>>> response = getDoseFormGroups(representation("custom:(doseForms)"));
		
		assertNotNull("doseForms was asked for", response.get("doseForms"));
		assertEquals("only doseForms was asked for", 1, response.keySet().size());
	}
	
	private MockHttpServletRequest representation(String v) {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter("v", v);
		return request;
	}
	
	private Map<String, Object> firstDoseForm(MockHttpServletRequest request) {
		return asConcept(getDoseFormGroups(request).get("doseForms").get(0).get("doseForm"));
	}
	
	private Map<String, List<Map<String, Object>>> getDoseFormGroups() {
		return getDoseFormGroups(new MockHttpServletRequest());
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, List<Map<String, Object>>> getDoseFormGroups(MockHttpServletRequest request) {
		return (Map<String, List<Map<String, Object>>>) controller.getDoseFormGroups(request, new MockHttpServletResponse());
	}
	
	/**
	 * Each Concept in the response has been converted to a representation, i.e. a map of properties.
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> asConcept(Object convertedConcept) {
		return (Map<String, Object>) convertedConcept;
	}
	
	private String displayOf(Object convertedConcept) {
		return (String) asConcept(convertedConcept).get("display");
	}
}
