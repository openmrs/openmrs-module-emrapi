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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@RecordApplicationEvents
public class ApplicationEventServiceTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private ApplicationEvents applicationEvents;
	
	/**
	 * @verifies publish the patient viewed event
	 * @see ApplicationEventService#patientViewed(org.openmrs.Patient, org.openmrs.User)
	 */
	@Test
	public void patientViewed_shouldPublishThePatientViewedEvent() {
		Patient patient = Context.getPatientService().getPatient(2);
		User user = Context.getUserService().getUser(502);
		
		Context.getService(ApplicationEventService.class).patientViewed(patient, user);
		
		List<PatientViewedEvent> events = applicationEvents.stream(PatientViewedEvent.class).collect(Collectors.toList());
		assertEquals(1, events.size());
		assertEquals(patient.getUuid(), events.get(0).getPatientUuid());
		assertEquals(user.getUuid(), events.get(0).getUserUuid());
	}
}
