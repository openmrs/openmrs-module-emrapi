/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.patient.reporting.library;

import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Basic patient data columns provided by emr-api module
 */
@OpenmrsProfile(modules = { "reporting:*" })
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
