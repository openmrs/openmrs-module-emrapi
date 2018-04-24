package org.openmrs.module.emrapi.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Condition;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ConditionService;
import org.openmrs.api.PatientService;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * This class specifies data manipulation methods on a Condition.
 */
@Controller
@RequestMapping(value = "/rest/emrapi")
public class ConditionController extends BaseRestController {

	ConditionService conditionService;

	PatientService patientService;

	ConceptService conceptService;

	/**
	 * Constructor to instantiate the ConditionController.
	 *
	 * @param conditionService - the condition service
	 * @param patientService - the patient service
	 * @param conceptService - the concept service
	 */
	@Autowired
	public ConditionController(ConditionService conditionService, PatientService patientService,
			ConceptService conceptService) {
		this.conditionService = conditionService;
		this.patientService = patientService;
		this.conceptService = conceptService;
	}

	/**
	 * Gets a list of active conditions.
	 *
	 * @param patientUuid - the uuid of a patient
	 * @return a list of active conditions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/conditionhistory")
	@ResponseBody
	public List<Condition> getConditionHistory(@RequestParam("patientUuid") String patientUuid) {

		return conditionService.getActiveConditions(patientService.getPatientByUuid(patientUuid));
	}

	/**
	 * Saves a condition.
	 *
	 * @param conditions - a list of conditions to be saved
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/condition")
	@ResponseBody
	public List<Condition> save(@RequestBody Condition[] conditions) {
		List<Condition> savedConditions = new ArrayList<Condition>();
		for (Condition condition : conditions) {
			Condition savedCondition = conditionService.saveCondition(condition);
			savedConditions.add(savedCondition);
		}
		return savedConditions;
	}
}

