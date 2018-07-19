package org.openmrs.module.emrapi.web.controller;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.util.MigrateDiagnosis;
import org.openmrs.web.WebConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
public class MigrateDiagnosisController {
	
    @Autowired
	EmrApiProperties emrApiProperties ;
    
	@RequestMapping(value = "module/emrapi/MigrateDiagnosis.form", method = RequestMethod.GET)
	public String Migrate( HttpServletRequest request , HttpSession sesion) {
		 
		
		 DiagnosisMetadata diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
		 
		
		 MigrateDiagnosis migrateDiagnosis = new MigrateDiagnosis();
		 migrateDiagnosis.migrate(diagnosisMetadata);
	    
		
		sesion.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "SUCCESFULLY MIGRATED");
				
		return "module/emrapi/MigrateDiagnosis";
	}

}
