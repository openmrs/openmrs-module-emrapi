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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.APIException;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class DoseFormGroupControllerTest extends BaseModuleWebContextSensitiveTest {
	
	private static final String TABLET_UUID = "c3e6a0d4-5b8f-4c1e-d7a9-0f4b2c8e3d15";
	
	@Autowired
	DoseFormGroupController controller;
	
	@Before
	public void setUp() throws Exception {
		executeDataSet("doseFormGroupDataset.xml");
	}
	
	/**
	 * Pins the response contract: the top-level keys, the nesting under each, and the dose form to dose
	 * form group mapping itself. A dose form belongs to at most one group, so "Ointment", which belongs
	 * to none, is still reported, with a null dose form group.
	 */
	@Test
	public void shouldReturnEachDoseFormMappedToItsDoseFormGroup() throws Exception {
		Map<String, List<Map<String, Object>>> response = getDoseFormGroups();
		
		assertNotNull("response should contain a doseForms entry", response.get("doseForms"));
		assertNotNull("response should contain a doseFormGroups entry", response.get("doseFormGroups"));
		
		Map<String, String> groupByDoseForm = new HashMap<String, String>();
		for (Map<String, Object> entry : response.get("doseForms")) {
			groupByDoseForm.put(displayOf(entry.get("doseForm")), displayOf(entry.get("doseFormGroup")));
		}
		
		assertEquals("Oral Solid", groupByDoseForm.get("Tablet"));
		assertEquals("Oral Solid", groupByDoseForm.get("Capsule"));
		assertEquals("Injectable", groupByDoseForm.get("Solution for Injection"));
		assertTrue("a dose form in no group should still be reported", groupByDoseForm.containsKey("Ointment"));
		assertNull("a dose form in no group should have a null dose form group", groupByDoseForm.get("Ointment"));
		assertEquals(4, groupByDoseForm.size());
	}
	
	/**
	 * A dose form group's routes come from its route-of-administration mappings, resolved to concepts
	 * via a SAME-AS mapping on the same reference term. "Oral Solid" points at a term that concept
	 * "Oral" claims that way, so the route resolves. "Injectable" points at a term no concept claims,
	 * and the controller drops it silently rather than failing the whole request.
	 */
	@Test
	public void shouldReturnRoutesResolvedBySameAsMappingAndDropUnresolvableOnes() throws Exception {
		Map<String, List<Map<String, Object>>> response = getDoseFormGroups();
		
		List<Map<String, Object>> doseFormGroups = response.get("doseFormGroups");
		assertEquals(2, doseFormGroups.size());
		
		Map<String, List<String>> routesByGroup = new HashMap<String, List<String>>();
		for (Map<String, Object> entry : doseFormGroups) {
			List<String> routes = new ArrayList<String>();
			for (Object route : (List<?>) entry.get("routes")) {
				routes.add(displayOf(route));
			}
			routesByGroup.put(displayOf(entry.get("doseFormGroup")), routes);
		}
		
		assertEquals(1, routesByGroup.get("Oral Solid").size());
		assertEquals("Oral", routesByGroup.get("Oral Solid").get(0));
		
		assertTrue("a route with no SAME-AS mapped concept should be omitted", routesByGroup.get("Injectable").isEmpty());
	}
	
	/**
	 * The requested representation has to reach the nested Concepts. It only does so because the
	 * response is a bean with a registered Converter: ConversionUtil converts the values of a Map with
	 * a hardcoded Representation.REF, so back when this endpoint returned a SimpleObject every Concept
	 * came back as a REF no matter what was asked for.
	 */
	@Test
	public void shouldReturnConceptsInTheRequestedRepresentation() throws Exception {
		Set<String> refProperties = new HashSet<String>(Arrays.asList("uuid", "display", "links"));
		
		Map<String, Object> asRef = firstDoseForm(representation("ref"));
		assertEquals(refProperties, asRef.keySet());
		
		Map<String, Object> asFull = firstDoseForm(representation("full"));
		assertTrue("expected more than a REF, got " + asFull.keySet(),
		    asFull.keySet().containsAll(Arrays.asList("uuid", "display", "datatype", "conceptClass")));
		
		// the default is what clients that do not ask for a representation actually receive
		Map<String, Object> asDefault = firstDoseForm(new MockHttpServletRequest());
		assertTrue("expected more than a REF, got " + asDefault.keySet(),
		    asDefault.keySet().containsAll(Arrays.asList("uuid", "display", "datatype", "conceptClass")));
		
		// A Concept's default representation includes setMembers, so by default every dose form group
		// also carries a nested list of its own dose forms, i.e. the doseForms list is repeated inside
		// doseFormGroups. Pinned so that trimming the default payload is a deliberate change.
		Map<String, Object> group = asConcept(getDoseFormGroups().get("doseFormGroups").get(0).get("doseFormGroup"));
		assertNotNull("dose form groups carry their set members by default", group.get("setMembers"));
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
	
	@Test
	public void shouldFailWhenADoseFormBelongsToMoreThanOneDoseFormGroup() throws Exception {
		executeDataSet("doseFormGroupDuplicateDataset.xml");
		
		try {
			getDoseFormGroups();
			fail("expected an APIException because Tablet belongs to two dose form groups");
		}
		catch (APIException e) {
			// the group that gets to claim the form first depends on the order concepts come back in,
			// so assert on the parts of the message that do not depend on it
			assertTrue(e.getMessage(), e.getMessage().contains("Tablet"));
			assertTrue(e.getMessage(), e.getMessage().contains(TABLET_UUID));
			assertTrue(e.getMessage(), e.getMessage().contains("belongs to more than one dose form group"));
			assertTrue(e.getMessage(), e.getMessage().contains("Oral Solid"));
			assertTrue(e.getMessage(), e.getMessage().contains("Oral Liquid"));
		}
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
	
	/**
	 * Null for the dose form group of a dose form that belongs to no group.
	 */
	private String displayOf(Object convertedConcept) {
		return convertedConcept == null ? null : (String) asConcept(convertedConcept).get("display");
	}
}
