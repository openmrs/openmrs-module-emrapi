/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.descriptor;

/**
 * Describes the concept set descriptor field name, concept code and if concept is required or
 * optional.
 */
public class ConceptSetDescriptorField {
	
	private final String name;
	
	private final String conceptCode;
	
	private final boolean required;
	
	private ConceptSetDescriptorField(String name, String conceptCode, boolean required) {
		this.name = name;
		this.conceptCode = conceptCode;
		this.required = required;
	}
	
	public String getName() {
		return name;
	}
	
	public String getConceptCode() {
		return conceptCode;
	}
	
	public boolean isRequired() {
		return required;
	}
	
	public static ConceptSetDescriptorField required(String fieldName, String conceptCode) {
		return new ConceptSetDescriptorField(fieldName, conceptCode, true);
	}
	
	public static ConceptSetDescriptorField optional(String fieldName, String conceptCode) {
		return new ConceptSetDescriptorField(fieldName, conceptCode, false);
	}
}
