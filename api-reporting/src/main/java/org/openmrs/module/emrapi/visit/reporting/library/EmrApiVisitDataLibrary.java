/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.visit.reporting.library;

import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.emrapi.adt.reporting.definition.MostRecentAdmissionRequestVisitDataDefinition;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.springframework.stereotype.Component;

/**
 * Basic visit data columns provided by module
 */
@OpenmrsProfile(modules = { "reporting:*" })
public class EmrApiVisitDataLibrary extends BaseDefinitionLibrary<VisitDataDefinition> {
	
	public static final String PREFIX = "emrapi.visitDataDefinition.";
	
	@Override
	public Class<? super VisitDataDefinition> getDefinitionType() {
		return VisitDataDefinition.class;
	}
	
	@Override
	public String getKeyPrefix() {
		return PREFIX;
	}
	
	@DocumentedDefinition("mostRecentAdmissionRequest")
	public VisitDataDefinition getMostRecentAdmissionRequestVisitDataDefinition() {
		return new MostRecentAdmissionRequestVisitDataDefinition();
	}
	
}
