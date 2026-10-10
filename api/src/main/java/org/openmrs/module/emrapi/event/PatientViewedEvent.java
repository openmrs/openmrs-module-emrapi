/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.event;

/**
 * Published through core's {@link org.openmrs.event.EventPublisher} when a user views a patient,
 * see {@link ApplicationEventService#patientViewed(org.openmrs.Patient, org.openmrs.User)}. Listen
 * for it with a Spring {@link org.springframework.context.event.EventListener}.
 *
 * @since 4.0.0
 */
public class PatientViewedEvent {
	
	private final String patientUuid;
	
	private final String userUuid;
	
	public PatientViewedEvent(String patientUuid, String userUuid) {
		this.patientUuid = patientUuid;
		this.userUuid = userUuid;
	}
	
	public String getPatientUuid() {
		return patientUuid;
	}
	
	public String getUserUuid() {
		return userUuid;
	}
}
