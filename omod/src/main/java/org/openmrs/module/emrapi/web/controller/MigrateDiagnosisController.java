package org.openmrs.module.emrapi.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openmrs.module.ModuleUtil;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.util.OpenmrsConstants;
import org.openmrs.web.WebConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import openmrs.module.emrapi.diagnosis.MigrateDiagnosis;

@Controller
public class MigrateDiagnosisController {
	
	@Autowired
	EmrApiProperties emrApiProps;
	
	@RequestMapping(value = "module/emrapi/encounterDiagnosisMigrationDashboard.form", method = RequestMethod.GET)
	public String getEncounterDiagnosisMigrationView() {
		return "module/emrapi/encounterDiagnosisMigrationDashboard";
	}
	
	
	@RequestMapping(value = "module/emrapi/migrateEncounterDiagnosis.form", method = RequestMethod.GET)
	public String doEncounterDiagnosisMigration(HttpSession session, HttpServletRequest request) {
		DiagnosisMetadata diagnosisMetadata = emrApiProps.getDiagnosisMetadata();
		if (ModuleUtil.compareVersion(OpenmrsConstants.OPENMRS_VERSION, "2.2.0") >= 0) {
			if (new MigrateDiagnosis().migrate(diagnosisMetadata)) {
				session.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "emrapi.migrateDiagnosis.success.name");
			} else {
				session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "emrapi.migrateDiagnosis.migration.error.message");
			}
		} else {
			session.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "emrapi.migrateDiagnosis.migration.unsupportedPlatformVersionError.message");
		}	
		return "redirect:encounterDiagnosisMigrationDashboard.form";
	}
	
}
