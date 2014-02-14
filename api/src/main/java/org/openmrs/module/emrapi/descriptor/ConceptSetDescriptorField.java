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

package org.openmrs.module.emrapi.descriptor;

/**
 * Describes the concept set descriptor field name, concept code and if concept is required or optional.
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
