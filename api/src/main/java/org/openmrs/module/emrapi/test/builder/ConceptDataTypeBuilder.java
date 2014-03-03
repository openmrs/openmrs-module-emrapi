/**
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
