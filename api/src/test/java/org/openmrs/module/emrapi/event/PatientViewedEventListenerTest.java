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

import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.UserService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.utils.GeneralUtils;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jms.MapMessage;
import javax.jms.Message;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PatientViewedEventListenerTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private AdministrationService adminService;
	
	private User user;
	
	private PatientViewedEventListener listener = new PatientViewedEventListener(null);
	
	@Before
	public void setup() {
		if (user == null)
			user = userService.getUser(502);
	}
	
	private void setInitialLastViewedPatients(List<Integer> patientIds) {
		userService.setUserProperty(user, EmrApiConstants.USER_PROPERTY_NAME_LAST_VIEWED_PATIENT_IDS,
		    StringUtils.join(patientIds, ","));
	}
	
	private MapMessage createMessage(Patient patient, User user) throws Exception {
		MapMessage message = new ActiveMQMapMessage();
		message.setString(EmrApiConstants.EVENT_KEY_PATIENT_UUID, patient.getUuid());
		message.setString(EmrApiConstants.EVENT_KEY_USER_UUID, user.getUuid());
		
		return message;
	}
	
	/**
	 * @verifies add the patient to the last viewed user property
	 * @see PatientViewedEventListener#processMessage(javax.jms.Message)
	 */
	@Test
	public void processMessage_shouldAddThePatientToTheLastViewedUserProperty() throws Exception {
		setInitialLastViewedPatients(Arrays.asList(2, 6, 7));
		final Integer lastViewedPatientId = 8;
		Message message = createMessage(patientService.getPatient(lastViewedPatientId), user);
		listener.processMessage(message);
		
		List<Patient> lastViewed = GeneralUtils.getLastViewedPatients(user);
		assertEquals(lastViewedPatientId, lastViewed.get(0).getId());
		assertEquals(7, lastViewed.get(1).getId().intValue());
		assertEquals(6, lastViewed.get(2).getId().intValue());
		assertEquals(2, lastViewed.get(3).getId().intValue());
		
	}
	
	/**
	 * @verifies remove the first patient and add the new one to the start if the list is full
	 * @see PatientViewedEventListener#processMessage(javax.jms.Message)
	 */
	@Test
	public void processMessage_shouldRemoveTheFirstPatientAndAddTheNewOneToTheStartIfTheListIsFull() throws Exception {
		final Integer newLimit = 3;
		GlobalProperty gp = new GlobalProperty(EmrApiConstants.GP_LAST_VIEWED_PATIENT_SIZE_LIMIT, newLimit.toString());
		adminService.saveGlobalProperty(gp);
		
		final Integer patientIdToRemove = 2;
		setInitialLastViewedPatients(Arrays.asList(patientIdToRemove, 6, 7));
		final Integer lastSeenPatientId = 8;
		MapMessage message = createMessage(patientService.getPatient(lastSeenPatientId), user);
		listener.processMessage(message);
		
		List<Patient> lastViewed = GeneralUtils.getLastViewedPatients(user);
		assertEquals(newLimit.intValue(), lastViewed.size());
		assertEquals(lastSeenPatientId, lastViewed.get(0).getId());
		assertEquals(7, lastViewed.get(1).getId().intValue());
		assertEquals(6, lastViewed.get(2).getId().intValue());
	}
	
	/**
	 * @verifies not add a duplicate and should move the existing patient to the start
	 * @see PatientViewedEventListener#processMessage(javax.jms.Message)
	 */
	@Test
	public void processMessage_shouldNotAddADuplicateAndShouldMoveTheExistingPatientToTheStart() throws Exception {
		final Integer duplicatePatientId = 2;
		List<Integer> initialPatientIds = Arrays.asList(duplicatePatientId, 6, 7, 8);
		final int initialSize = initialPatientIds.size();
		setInitialLastViewedPatients(initialPatientIds);
		MapMessage message = createMessage(patientService.getPatient(duplicatePatientId), user);
		listener.processMessage(message);
		
		List<Patient> lastViewed = GeneralUtils.getLastViewedPatients(user);
		assertEquals(initialSize, lastViewed.size());
		assertEquals(duplicatePatientId, lastViewed.get(0).getId());
		assertEquals(8, lastViewed.get(1).getId().intValue());
		assertEquals(7, lastViewed.get(2).getId().intValue());
		assertEquals(6, lastViewed.get(3).getId().intValue());
	}
	
	/**
	 * @verifies not remove any patient if a duplicate is added to a full list
	 * @see PatientViewedEventListener#processMessage(javax.jms.Message)
	 */
	@Test
	public void processMessage_shouldNotRemoveAnyPatientIfADuplicateIsAddedToAFullList() throws Exception {
		final Integer newLimit = 4;
		GlobalProperty gp = new GlobalProperty(EmrApiConstants.GP_LAST_VIEWED_PATIENT_SIZE_LIMIT, newLimit.toString());
		adminService.saveGlobalProperty(gp);
		
		final Integer duplicatePatientId = 2;
		setInitialLastViewedPatients(Arrays.asList(6, duplicatePatientId, 7, 8));
		MapMessage message = createMessage(patientService.getPatient(duplicatePatientId), user);
		listener.processMessage(message);
		
		List<Patient> lastViewed = GeneralUtils.getLastViewedPatients(user);
		assertEquals(newLimit.intValue(), lastViewed.size());
		//The duplicate should still have been moved to the top of the list
		assertEquals(duplicatePatientId, lastViewed.get(0).getId());
		assertEquals(8, lastViewed.get(1).getId().intValue());
		assertEquals(7, lastViewed.get(2).getId().intValue());
		assertEquals(6, lastViewed.get(3).getId().intValue());
	}
}
