/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.test.builder;

import org.openmrs.ConceptDatatype;

/**
 * Helper for building concept data types in unit tests
 */
public class ConceptDataTypeBuilder {
	
	public ConceptDatatype text() {
		return getConceptDataType(ConceptDatatype.TEXT_UUID, ConceptDatatype.TEXT);
	}
	
	public ConceptDatatype numeric() {
		return getConceptDataType(ConceptDatatype.NUMERIC_UUID, ConceptDatatype.NUMERIC);
	}
	
	public ConceptDatatype coded() {
		return getConceptDataType(ConceptDatatype.CODED_UUID, ConceptDatatype.CODED);
	}
	
	private ConceptDatatype getConceptDataType(String uuid, String hl7) {
		ConceptDatatype conceptDatatype = new ConceptDatatype();
		conceptDatatype.setUuid(uuid);
		conceptDatatype.setHl7Abbreviation(hl7);
		return conceptDatatype;
	}
}
