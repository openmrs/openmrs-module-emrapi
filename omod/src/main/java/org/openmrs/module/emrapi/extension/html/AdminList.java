/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.extension.html;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.module.Extension;
import org.openmrs.module.ModuleUtil;
import org.openmrs.module.web.extension.AdministrationSectionExt;
import org.openmrs.util.OpenmrsConstants;

public class AdminList extends AdministrationSectionExt {
	
	private static final String DIAGNOSIS_MIGRATION_LEAST_SUPPORTED_VERSION = "2.2.0";
	
	@Override
	public Map<String, String> getLinks() {
		Map<String, String> links = new HashMap<String, String>();
		
		if (ModuleUtil.compareVersion(OpenmrsConstants.OPENMRS_VERSION, DIAGNOSIS_MIGRATION_LEAST_SUPPORTED_VERSION) >= 0) {
			links.put("module/emrapi/encounterDiagnosisMigrationDashboard.form",
			    "emrapi.migrateDiagnosis.migrateDiagnosisLink.name");
		}
		return links;
	}
	
	@Override
	public String getTitle() {
		return "emrapi.title";
	}
	
	@Override
	public Extension.MEDIA_TYPE getMediaType() {
		return Extension.MEDIA_TYPE.html;
	}
}
