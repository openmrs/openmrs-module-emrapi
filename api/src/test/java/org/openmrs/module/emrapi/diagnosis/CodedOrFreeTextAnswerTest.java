/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.diagnosis;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptNameType;

import java.util.Locale;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CodedOrFreeTextAnswerTest {
	
	@Test
	public void testFormatFreeText() throws Exception {
		String actual = new CodedOrFreeTextAnswer("Free text").format(Locale.ENGLISH);
		assertThat(actual, is("\"Free text\""));
	}
	
	@Test
	public void testFormatCoded() throws Exception {
		Concept concept = new Concept();
		concept.addName(new ConceptName("English", Locale.ENGLISH));
		String actual = new CodedOrFreeTextAnswer(concept).format(Locale.ENGLISH);
		assertThat(actual, is("English"));
	}
	
	@Test
	public void testFormatSpecificCoded() throws Exception {
		ConceptName preferredEnglishName = new ConceptName("English", Locale.ENGLISH);
		preferredEnglishName.setLocalePreferred(true);
		preferredEnglishName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		ConceptName frenchSynonym = new ConceptName("Français", Locale.FRENCH);
		
		Concept concept = new Concept();
		concept.addName(preferredEnglishName);
		concept.addName(frenchSynonym);
		String actual = new CodedOrFreeTextAnswer(frenchSynonym).format(Locale.ENGLISH);
		assertThat(actual, is("Français → English"));
	}
	
	@Test
	public void testFormatSpecificCodedWhenItIsLocalePreferred() throws Exception {
		ConceptName preferred = new ConceptName("Preferred", Locale.ENGLISH);
		preferred.setLocalePreferred(true);
		preferred.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		ConceptName synonym = new ConceptName("Synonym", Locale.ENGLISH);
		
		Concept concept = new Concept();
		concept.addName(preferred);
		concept.addName(synonym);
		String actual = new CodedOrFreeTextAnswer(preferred).format(Locale.ENGLISH);
		assertThat(actual, is(preferred.getName()));
	}
	
	@Test
	public void testFormatSpecificCodedWithIdenticalName() throws Exception {
		ConceptName preferredEnglishName = new ConceptName("English", Locale.ENGLISH);
		preferredEnglishName.setLocalePreferred(true);
		preferredEnglishName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
		ConceptName ukEnglishSynonym = new ConceptName("English", Locale.UK);
		
		Concept concept = new Concept();
		concept.addName(preferredEnglishName);
		concept.addName(ukEnglishSynonym);
		String actual = new CodedOrFreeTextAnswer(ukEnglishSynonym).format(Locale.ENGLISH);
		assertThat(actual, is("English"));
	}
	
}
