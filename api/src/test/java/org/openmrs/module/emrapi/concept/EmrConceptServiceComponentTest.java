/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.concept;

import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.test.builder.ConceptBuilder;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 */
public class EmrConceptServiceComponentTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	private EmrConceptService emrConceptService;
	
	@BeforeEach
	public void setUp() throws Exception {
		executeDataSet("conceptMapTypes.xml");
	}
	
	@Test
	public void testGetConceptsSameOrNarrowerThanTerm() throws Exception {
		ConceptSource source = conceptService.getConceptSource(1);
		
		ConceptMapType sameAs = conceptService.getConceptMapTypeByUuid(EmrApiConstants.SAME_AS_CONCEPT_MAP_TYPE_UUID);
		ConceptMapType narrowerThan = conceptService
		        .getConceptMapTypeByUuid(EmrApiConstants.NARROWER_THAN_CONCEPT_MAP_TYPE_UUID);
		ConceptMapType someOtherType = conceptService.getConceptMapType(5);
		
		ConceptReferenceTerm term = new ConceptReferenceTerm(source, "food-assist", null);
		conceptService.saveConceptReferenceTerm(term);
		
		Concept foodAssistance = conceptService.getConcept(18);
		foodAssistance.addConceptMapping(new ConceptMap(term, sameAs));
		conceptService.saveConcept(foodAssistance);
		
		Concept foodAssistanceForEntireFamily = conceptService.getConcept(21);
		foodAssistanceForEntireFamily.addConceptMapping(new ConceptMap(term, narrowerThan));
		conceptService.saveConcept(foodAssistanceForEntireFamily);
		
		Concept anotherConcept = conceptService.getConcept(20);
		anotherConcept.addConceptMapping(new ConceptMap(term, someOtherType));
		conceptService.saveConcept(anotherConcept);
		
		List<Concept> actual = emrConceptService.getConceptsSameOrNarrowerThan(term);
		assertThat(actual.size(), is(2));
		assertThat(actual, IsIterableContainingInAnyOrder.containsInAnyOrder(foodAssistance, foodAssistanceForEntireFamily));
	}
	
	/**
	 * Unlike getConceptsSameOrNarrowerThan(), only SAME-AS mappings count. Note that the term carries
	 * mappings of three different types here: that is the case ConceptService.getConceptByMapping()
	 * rejects, and the reason this method exists.
	 */
	@Test
	public void testGetConceptsSameAsTerm() throws Exception {
		ConceptSource source = conceptService.getConceptSource(1);
		
		ConceptMapType sameAs = conceptService.getConceptMapTypeByUuid(EmrApiConstants.SAME_AS_CONCEPT_MAP_TYPE_UUID);
		ConceptMapType narrowerThan = conceptService
		        .getConceptMapTypeByUuid(EmrApiConstants.NARROWER_THAN_CONCEPT_MAP_TYPE_UUID);
		ConceptMapType someOtherType = conceptService.getConceptMapType(5);
		
		ConceptReferenceTerm term = new ConceptReferenceTerm(source, "food-assist", null);
		conceptService.saveConceptReferenceTerm(term);
		
		Concept foodAssistance = conceptService.getConcept(18);
		foodAssistance.addConceptMapping(new ConceptMap(term, sameAs));
		conceptService.saveConcept(foodAssistance);
		
		Concept foodAssistanceForEntireFamily = conceptService.getConcept(21);
		foodAssistanceForEntireFamily.addConceptMapping(new ConceptMap(term, narrowerThan));
		conceptService.saveConcept(foodAssistanceForEntireFamily);
		
		Concept anotherConcept = conceptService.getConcept(20);
		anotherConcept.addConceptMapping(new ConceptMap(term, someOtherType));
		conceptService.saveConcept(anotherConcept);
		
		List<Concept> actual = emrConceptService.getConceptsSameAs(term);
		assertThat(actual.size(), is(1));
		assertThat(actual.get(0), is(foodAssistance));
	}
	
	@Test
	public void testGetConceptsSameAsRequiresATerm() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> emrConceptService.getConceptsSameAs(null));
	}
	
	/**
	 * "Ointment", which belongs to no group, is still reported, with no dose form groups. Retired dose
	 * forms and retired dose form groups are left out altogether: ConceptService.getConceptsByClass()
	 * returns them, but a retired dose form must not reach a prescriber's dose form picker.
	 */
	@Test
	public void testGetDoseFormMemberships() throws Exception {
		executeDataSet("doseFormGroupDataset.xml");
		
		Map<String, Set<String>> groupsByDoseForm = groupsByDoseForm();
		
		assertThat(groupsByDoseForm.keySet(), is(setOf("Tablet", "Capsule", "Solution for Injection", "Ointment")));
		assertThat(groupsByDoseForm.get("Tablet"), is(setOf("Oral Solid")));
		assertThat(groupsByDoseForm.get("Capsule"), is(setOf("Oral Solid")));
		assertThat(groupsByDoseForm.get("Solution for Injection"), is(setOf("Injectable")));
		assertThat("a dose form in no group has no dose form groups", groupsByDoseForm.get("Ointment"),
		    is(Collections.<String> emptySet()));
	}
	
	/**
	 * A dose form may belong to more than one dose form group, e.g. CIEL has "oral spray" in both the
	 * oral spray group and the oral group.
	 */
	@Test
	public void testGetDoseFormMembershipsForADoseFormInMoreThanOneGroup() throws Exception {
		executeDataSet("doseFormGroupDataset.xml");
		executeDataSet("doseFormGroupMultipleGroupsDataset.xml");
		
		Map<String, Set<String>> groupsByDoseForm = groupsByDoseForm();
		
		assertThat(groupsByDoseForm.get("Tablet"), is(setOf("Oral Solid", "Oral Liquid")));
		assertThat("a form in two groups should not affect the others", groupsByDoseForm.get("Capsule"),
		    is(setOf("Oral Solid")));
	}
	
	/**
	 * A dose form group's routes come from its route-of-administration mappings, resolved to concepts
	 * via a SAME-AS mapping on the same reference term. "Oral Solid" points at a term that concept
	 * "Oral" claims that way, so the route resolves; it also points at a term only the retired concept
	 * "Retired Oral" claims, which is dropped. "Injectable" points at a term no concept claims at all,
	 * and that too is dropped rather than failing the whole lookup.
	 */
	@Test
	public void testGetDoseFormGroupRoutes() throws Exception {
		executeDataSet("doseFormGroupDataset.xml");
		
		Map<String, Set<String>> routesByGroup = new HashMap<String, Set<String>>();
		for (DoseFormGroupRoutes doseFormGroupRoutes : emrConceptService.getDoseFormGroupRoutes()) {
			routesByGroup.put(nameOf(doseFormGroupRoutes.getDoseFormGroup()), namesOf(doseFormGroupRoutes.getRoutes()));
		}
		
		assertThat("a retired dose form group should not be reported", routesByGroup.keySet(),
		    is(setOf("Oral Solid", "Injectable")));
		assertThat("a route whose only SAME-AS mapped concept is retired should be omitted", routesByGroup.get("Oral Solid"),
		    is(setOf("Oral")));
		assertThat("a route with no SAME-AS mapped concept should be omitted", routesByGroup.get("Injectable"),
		    is(Collections.<String> emptySet()));
	}
	
	/**
	 * The routes of a dose form are the union of the routes of every group it belongs to, which is what
	 * a caller holding a drug's dosage form actually wants to know. Retired groups are not among them:
	 * Tablet is also claimed by Retired Group, whose route Rectal resolves like any other, so a missing
	 * retired check on the parent sets would offer a prescriber a route that has been withdrawn.
	 */
	@Test
	public void testGetRoutesOfAdministration() throws Exception {
		executeDataSet("doseFormGroupDataset.xml");
		executeDataSet("doseFormGroupMultipleGroupsDataset.xml");
		
		// Tablet is in Oral Solid, whose route is Oral, and in Oral Liquid, whose routes are Oral and Nasal
		List<Concept> tabletRoutes = emrConceptService.getRoutesOfAdministration(conceptService.getConcept(5011));
		assertThat("Oral is a route of both of Tablet's groups, and should be reported once", tabletRoutes.size(), is(2));
		assertThat("Rectal is reachable only through a retired dose form group", namesOf(tabletRoutes),
		    is(setOf("Oral", "Nasal")));
		assertThat("a dose form in no dose form group has no routes",
		    emrConceptService.getRoutesOfAdministration(conceptService.getConcept(5014)).size(), is(0));
	}
	
	@Test
	public void testGetRoutesOfAdministrationRequiresADoseForm() throws Exception {
		assertThrows(IllegalArgumentException.class, () -> emrConceptService.getRoutesOfAdministration(null));
	}
	
	/**
	 * A dictionary that has not imported the dose form group metadata has neither concept class, which
	 * has to degrade to nothing rather than fail. Note that no dose form dataset is loaded here.
	 */
	@Test
	public void testGetDoseFormsWithoutTheDoseFormConceptClasses() throws Exception {
		assertThat(conceptService.getConceptClassByName(EmrApiConstants.DOSE_FORM_CONCEPT_CLASS_NAME), is(nullValue()));
		assertThat(conceptService.getConceptClassByName(EmrApiConstants.DOSE_FORM_GROUP_CONCEPT_CLASS_NAME),
		    is(nullValue()));
		
		assertThat(emrConceptService.getDoseFormMemberships().size(), is(0));
		assertThat(emrConceptService.getDoseFormGroupRoutes().size(), is(0));
	}
	
	@Test
	public void testConceptSearchByName() throws Exception {
		Map<String, Concept> concepts = setupConcepts();
		ConceptClass diagnosis = conceptService.getConceptClassByName("Diagnosis");
		
		List<ConceptSearchResult> searchResults = emrConceptService.conceptSearch("malaria", Locale.ENGLISH,
		    Collections.singleton(diagnosis), null, null, null);
		
		assertThat(searchResults.size(), is(2));
		
		ConceptSearchResult firstResult = searchResults.get(0);
		ConceptSearchResult otherResult = searchResults.get(1);
		
		assertThat(firstResult.getConcept(), is(concepts.get("malaria")));
		assertThat(firstResult.getConceptName().getName(), is("Malaria"));
		
		assertThat(otherResult.getConcept(), is(concepts.get("cerebral malaria")));
		assertThat(otherResult.getConceptName().getName(), is("Cerebral Malaria"));
	}
	
	@Test
	public void testConceptSearchInAnotherLocale() throws Exception {
		Map<String, Concept> concepts = setupConcepts();
		ConceptClass diagnosis = conceptService.getConceptClassByName("Diagnosis");
		
		List<ConceptSearchResult> searchResults = emrConceptService.conceptSearch("malaria", Locale.FRENCH,
		    Collections.singleton(diagnosis), null, null, null);
		ConceptSearchResult firstResult = searchResults.get(0);
		
		assertThat(searchResults.size(), is(1));
		assertThat(firstResult.getConcept(), is(concepts.get("cerebral malaria")));
		assertThat(firstResult.getConceptName().getName(), is("Malaria célébrale"));
	}
	
	@Test
	public void testConceptSearchByIcd10Code() throws Exception {
		ConceptClass diagnosis = conceptService.getConceptClassByName("Diagnosis");
		ConceptSource icd10 = conceptService.getConceptSource(2);
		
		Map<String, Concept> concepts = setupConcepts();
		
		List<ConceptSearchResult> searchResults = emrConceptService.conceptSearch("E11.9", Locale.ENGLISH,
		    Collections.singleton(diagnosis), null, Collections.singleton(icd10), null);
		ConceptSearchResult firstResult = searchResults.get(0);
		
		assertThat(searchResults.size(), is(1));
		assertThat(firstResult.getConcept(), is(concepts.get("diabetes")));
		assertThat(firstResult.getConceptName(), nullValue());
	}
	
	@Test
	public void testConceptSearchForSetMembers() throws Exception {
		Map<String, Concept> concepts = setupConcepts();
		
		List<ConceptSearchResult> searchResults = emrConceptService.conceptSearch("malar", Locale.ENGLISH, null,
		    Collections.singleton(concepts.get("allowedDiagnoses")), null, null);
		assertThat(searchResults.size(), is(1));
		ConceptSearchResult firstResult = searchResults.get(0);
		assertThat(firstResult.getConcept(), is(concepts.get("malaria")));
		
		searchResults = emrConceptService.conceptSearch("diab", Locale.ENGLISH, null,
		    Collections.singleton(concepts.get("allowedDiagnoses")), null, null);
		assertThat(searchResults.size(), is(1));
		firstResult = searchResults.get(0);
		assertThat(firstResult.getConcept(), is(concepts.get("diabetes")));
	}
	
	@Test
	public void testConceptSearchByNameFromSpecificSources() throws Exception {
		Map<String, Concept> concepts = setupConcepts();
		ConceptClass diagnosis = conceptService.getConceptClassByName("Diagnosis");
		ConceptSource icd10 = conceptService.getConceptSource(2);
		
		List<ConceptSearchResult> searchResults = emrConceptService.conceptSearch("ia", Locale.ENGLISH,
		    Collections.singleton(diagnosis), null, Collections.singleton(icd10), null);
		
		assertThat(searchResults.size(), is(3));
		
		ConceptSearchResult firstResult = searchResults.get(0);
		ConceptSearchResult secondResult = searchResults.get(1);
		ConceptSearchResult thirdResult = searchResults.get(2);
		
		assertThat(firstResult.getConcept(), is(concepts.get("malaria")));
		assertThat(firstResult.getConceptName().getName(), is("Malaria"));
		
		assertThat(secondResult.getConcept(), is(concepts.get("cerebral malaria")));
		assertThat(secondResult.getConceptName().getName(), is("Cerebral Malaria"));
		
		assertThat(thirdResult.getConcept(), is(concepts.get("diabetes")));
		assertThat(thirdResult.getConceptName().getName(), is("Diabetes Mellitus, Type II"));
	}
	
	/**
	 * The name of every dose form, mapped to the names of the dose form groups it belongs to.
	 */
	private Map<String, Set<String>> groupsByDoseForm() {
		Map<String, Set<String>> groupsByDoseForm = new HashMap<String, Set<String>>();
		for (DoseFormMembership membership : emrConceptService.getDoseFormMemberships()) {
			groupsByDoseForm.put(nameOf(membership.getDoseForm()), namesOf(membership.getDoseFormGroups()));
		}
		return groupsByDoseForm;
	}
	
	/**
	 * A Set because the order concepts come back in is not guaranteed.
	 */
	private Set<String> namesOf(List<Concept> concepts) {
		Set<String> names = new HashSet<String>();
		for (Concept concept : concepts) {
			names.add(nameOf(concept));
		}
		return names;
	}
	
	private String nameOf(Concept concept) {
		return concept.getName().getName();
	}
	
	private Set<String> setOf(String... values) {
		return new HashSet<String>(Arrays.asList(values));
	}
	
	private Map<String, Concept> setupConcepts() {
		Map<String, Concept> concepts = new HashMap<String, Concept>();
		
		ConceptMapType sameAs = conceptService.getConceptMapTypeByName("same-as");
		ConceptSource icd10 = conceptService.getConceptSource(2);
		ConceptSource snomed = conceptService.getConceptSource(3);
		
		ConceptDatatype na = conceptService.getConceptDatatypeByName("N/A");
		ConceptClass diagnosis = conceptService.getConceptClassByName("Diagnosis");
		ConceptClass convSet = conceptService.getConceptClassByName("ConvSet");
		
		concepts.put("malaria",
		    conceptService.saveConcept(new ConceptBuilder(conceptService, na, diagnosis)
		            .add(new ConceptName("Malaria", Locale.ENGLISH)).add(new ConceptName("Clinical Malaria", Locale.ENGLISH))
		            .add(new ConceptName("Paludisme", Locale.FRENCH)).addMapping(sameAs, icd10, "B54").get()));
		
		concepts.put("cerebral malaria",
		    conceptService.saveConcept(
		        new ConceptBuilder(conceptService, na, diagnosis).add(new ConceptName("Cerebral Malaria", Locale.ENGLISH))
		                .add(new ConceptName("Malaria célébrale", Locale.FRENCH)).addMapping(sameAs, icd10, "B50.0").get()));
		
		concepts.put("diabetes",
		    conceptService.saveConcept(new ConceptBuilder(conceptService, na, diagnosis)
		            .add(new ConceptName("Diabetes Mellitus, Type II", Locale.ENGLISH))
		            .addVoidedName(new ConceptName("Malaria", Locale.ENGLISH)).addMapping(sameAs, icd10, "E11.9").get()));
		
		concepts.put("preeclampsia", conceptService.saveConcept(new ConceptBuilder(conceptService, na, diagnosis)
		        .add(new ConceptName("Severe Preeclampsia", Locale.ENGLISH)).addMapping(sameAs, snomed, "46764007").get()));
		
		concepts.put("allowedDiagnoses",
		    conceptService.saveConcept(
		        new ConceptBuilder(conceptService, na, convSet).add(new ConceptName("Allowed Diagnoses", Locale.ENGLISH))
		                .addSetMember(concepts.get("malaria")).addSetMember(concepts.get("diabetes")).get()));
		
		return concepts;
	}
	
	private ArgumentMatcher<ConceptSearchResult> searchResultMatcher(final Concept concept, final String nameMatched) {
		return conceptSearchResult -> conceptSearchResult.getConcept().equals(concept)
		        && conceptSearchResult.getConceptName().getName().equals(nameMatched);
	}
	
}
