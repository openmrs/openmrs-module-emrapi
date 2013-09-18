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

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.MapMessage;
import javax.jms.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.event.Event;
import org.openmrs.event.EventListener;
import org.openmrs.module.emrapi.EmrApiConstants;

public class ApplicationEventServiceTest {
	
	private MockEmrEventListener listener;
	
	private class MockEmrEventListener implements EventListener {
		
		//Expects one event to get fired
		CountDownLatch latch = new CountDownLatch(1);
		
		String patientUuid;
		
		String userUuid;
		
		/**
		 * Waits for events for at most 2 seconds.
		 * 
		 * @throws InterruptedException
		 */
		public void waitForEvents() throws InterruptedException {
			latch.await(2, TimeUnit.SECONDS);
		}
		
		@Override
		public void onMessage(Message message) {
			try {
				MapMessage mapMessage = (MapMessage) message;
				patientUuid = mapMessage.getString(EmrApiConstants.EVENT_KEY_PATIENT_UUID);
				userUuid = mapMessage.getString(EmrApiConstants.EVENT_KEY_USER_UUID);
				//signal that the listener is done
				latch.countDown();
			}
			catch (Exception e) {}
		}
	}
	
	@Before
	public void setup() {
		listener = new MockEmrEventListener();
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
	public void patientViewed_shouldPublishThePatientViewedEvent() throws Exception {
		Patient patient = new Patient();
		User user = new User();
		new ApplicationEventServiceImpl().patientViewed(patient, user);

		listener.waitForEvents();
		assertEquals(patient.getUuid(), listener.patientUuid);
		assertEquals(user.getUuid(), listener.userUuid);
	}
}
