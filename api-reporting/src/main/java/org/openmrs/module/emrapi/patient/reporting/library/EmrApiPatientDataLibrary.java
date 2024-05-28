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
package org.openmrs.module.emrapi.patient.reporting.library;

import org.openmrs.PatientIdentifier;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Basic patient data columns provided by emr-api module
 */
@Component
public class EmrApiPatientDataLibrary extends BaseDefinitionLibrary<PatientDataDefinition> {

    @Autowired
    private EmrApiProperties emrApiProperties;

    public static final String PREFIX = "emrapi.patientDataDefinition.";

    @Override
    public Class<? super PatientDataDefinition> getDefinitionType() {
            return PatientDataDefinition.class;
    }

    @Override
    public String getKeyPrefix() {
            return PREFIX;
    }

    @DocumentedDefinition("primaryIdentifier")
    public PatientDataDefinition getPrimaryIdentifier() {
        PatientIdentifierDataDefinition def = new PatientIdentifierDataDefinition();
        def.addType(emrApiProperties.getPrimaryIdentifierType());
        def.setIncludeFirstNonNullOnly(true);
        return def;
    }

}
