/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.rest;

import org.openmrs.module.emrapi.patient.EmrPatientProfileService;
import org.openmrs.module.emrapi.person.image.EmrPersonImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmrModuleContext {
	
	private static EmrPersonImageService emrPersonImageService;
	
	private static EmrPatientProfileService emrPatientProfileService;
	
	@Autowired
	public void setEmrPersonImageService(EmrPersonImageService emrPersonImageService,
	        EmrPatientProfileService emrPatientProfileService) {
		EmrModuleContext.emrPersonImageService = emrPersonImageService;
		EmrModuleContext.emrPatientProfileService = emrPatientProfileService;
	}
	
	public static EmrPersonImageService getEmrPersonImageService() {
		return EmrModuleContext.emrPersonImageService;
	}
	
	public static EmrPatientProfileService getEmrPatientProfileService() {
		return EmrModuleContext.emrPatientProfileService;
	}
	
}
