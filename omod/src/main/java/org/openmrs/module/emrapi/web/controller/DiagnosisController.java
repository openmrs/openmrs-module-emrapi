/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.web.controller;

import java.util.Date;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/**/emrapi")
public class DiagnosisController extends BaseRestController {
	
	@Autowired
	private DiagnosisService diagnosisService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/patientdiagnoses")
	@ResponseBody
	public List<Diagnosis> getDiagnosesList(@RequestParam("patient") Patient patient,
	        @RequestParam(value = "fromDate", required = false) Date fromDate) {
		return diagnosisService.getDiagnoses(patient, fromDate);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/patientuniquediagnoses")
	@ResponseBody
	public List<Diagnosis> getUniqueDiagnosesList(@RequestParam("patient") Patient patient,
	        @RequestParam(value = "fromDate", required = false) Date fromDate) {
		return diagnosisService.getUniqueDiagnoses(patient, fromDate);
	}
}
