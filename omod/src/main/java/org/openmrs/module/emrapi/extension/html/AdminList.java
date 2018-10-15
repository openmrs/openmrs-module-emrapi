package org.openmrs.module.emrapi.extension.html;

import java.util.HashMap;
import java.util.Map;

import org.openmrs.api.context.Context;
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
			links.put("module/emrapi/encounterDiagnosisMigrationDashboard.form", "emrapi.migrateDiagnosis.migrateDiagnosisLink.name");
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
