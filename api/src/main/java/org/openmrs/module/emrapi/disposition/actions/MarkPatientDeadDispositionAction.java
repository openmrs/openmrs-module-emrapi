/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.disposition.actions;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.emrapi.exitfromcare.ExitFromCareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Sets the death and deathDate fields on a Patient, and saves those changes. TODO implement an API
 * method for recording a patient's death in this module (based on, but cleaner than, the
 * PatientService.processDeath method)
 */
@Component("markPatientDeadDispositionAction")
public class MarkPatientDeadDispositionAction implements DispositionAction {
	
	@Autowired
	private ExitFromCareService exitFromCareService;
	
	@Autowired
	private DispositionService dispositionService;
	
	@Autowired
	private EmrApiProperties emrApiProperties;
	
	@Override
	public void action(EncounterDomainWrapper encounterDomainWrapper, Obs dispositionObsGroupBeingCreated,
	        Map<String, String[]> requestParameters) {
		
		Date deathDate = dispositionService.getDispositionDescriptor().getDateOfDeath(dispositionObsGroupBeingCreated);
		
		// TODO: support pulling cause of death from the disposition
		Concept causeOfDeath = emrApiProperties.getUnknownCauseOfDeathConcept();
		
		Patient patient = encounterDomainWrapper.getEncounter().getPatient();
		exitFromCareService.markPatientDead(patient, causeOfDeath, deathDate);
	}
	
	public void setExitFromCareService(ExitFromCareService exitFromCareService) {
		this.exitFromCareService = exitFromCareService;
	}
	
	public void setDispositionService(DispositionService dispositionService) {
		this.dispositionService = dispositionService;
	}
	
	public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
		this.emrApiProperties = emrApiProperties;
	}
}
