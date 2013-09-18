/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.emrapi.event;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.event.Event;
import org.openmrs.event.MockEventListener;
import org.openmrs.module.emrapi.EmrApiConstants;

import javax.jms.MapMessage;
import javax.jms.Message;

import static org.junit.Assert.assertEquals;

public class ApplicationEventServiceTest {
	
	private MockEmrEventListener listener = new MockEmrEventListener(1);
	
	private class MockEmrEventListener extends MockEventListener {
		
		String patientUuid;
		
		String userUuid;
		
		MockEmrEventListener(int expectedEventsCount) {
			super(expectedEventsCount);
		}
		
		@Override
		public void onMessage(Message message) {
			try {
				MapMessage mapMessage = (MapMessage) message;
				patientUuid = mapMessage.getString(EmrApiConstants.EVENT_KEY_PATIENT_UUID);
				userUuid = mapMessage.getString(EmrApiConstants.EVENT_KEY_USER_UUID);
			}
			catch (Exception e) {}
		}
	}
	
	@Before
	public void setup() {
		Event.subscribe(EmrApiConstants.EVENT_TOPIC_NAME_PATIENT_VIEWED, listener);
	}
	
	@After
	public void tearDown() {
		Event.unsubscribe(EmrApiConstants.EVENT_TOPIC_NAME_PATIENT_VIEWED, listener);
	}
	
	/**
	 * @verifies publish the patient viewed event
	 * @see ApplicationEventService#patientViewed(org.openmrs.Patient, org.openmrs.User)
	 */
	@Test
    @Ignore
	public void patientViewed_shouldPublishThePatientViewedEvent() throws Exception {
		Patient patient = new Patient();
		User user = new User();
		new ApplicationEventServiceImpl().patientViewed(patient, user);
		
		listener.waitForEvents();
		assertEquals(patient.getUuid(), listener.patientUuid);
		assertEquals(user.getUuid(), listener.userUuid);
	}
}
