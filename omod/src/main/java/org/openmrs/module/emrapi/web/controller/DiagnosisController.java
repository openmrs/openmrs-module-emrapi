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
@RequestMapping(value = "/rest/emrapi")
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
