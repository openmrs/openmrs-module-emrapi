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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jms.MapMessage;
import javax.jms.Message;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.event.EventListener;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.utils.GeneralUtils;

/**
 * Listens for patient viewed events, the patient found in the message payload gets added to the
 * last viewed patients user property of the specified user,
 */
public class PatientViewedEventListener implements EventListener {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private DaemonToken daemonToken;
	
	public PatientViewedEventListener(DaemonToken token) {
		daemonToken = token;
	}
	
	/**
	 * @see EventListener#onMessage(javax.jms.Message)
	 * @param message
	 */
	@Override
	public void onMessage(final Message message) {
		Daemon.runInDaemonThread(new Runnable() {
			
			@Override
			public void run() {
				try {
					processMessage(message);
				}
				catch (Exception e) {
					log.error("Failed to update the user's last viewed patients property", e);
				}
			}
		}, daemonToken);
	}
	
	/**
	 * Processes the specified jms message
	 * 
	 * @should add the patient to the last viewed user property
	 * @should remove the first patient and add the new one to the start if the list is full
	 * @should not add a duplicate and should move the existing patient to the start
	 * @should not remove any patient if a duplicate is added to a full list
	 */
	public void processMessage(Message message) throws Exception {
		MapMessage mapMessage = (MapMessage) message;
		String patientUuid = mapMessage.getString(EmrApiConstants.EVENT_KEY_PATIENT_UUID);
		String userUuid = mapMessage.getString(EmrApiConstants.EVENT_KEY_USER_UUID);
		Patient patientToAdd = Context.getPatientService().getPatientByUuid(patientUuid);
		if (patientToAdd == null || patientToAdd.getId() == null) {
			throw new APIException("failed to find a patient with uuid:" + patientUuid + " or the patient is not yet saved");
		}
		
		UserService userService = Context.getUserService();
		User user = userService.getUserByUuid(userUuid);
		if (user != null && patientToAdd != null) {
			EmrApiProperties emrProperties = Context.getRegisteredComponents(EmrApiProperties.class).iterator().next();
			Integer limit = emrProperties.getLastViewedPatientSizeLimit();
			List<Integer> patientIds = new ArrayList<Integer>(limit);
			if (limit > 0) {
				List<Patient> lastViewedPatients = GeneralUtils.getLastViewedPatients(user);
				patientIds.add(patientToAdd.getId());
				for (Patient p : lastViewedPatients) {
					if (patientIds.size() == limit)
						break;
					if (patientIds.contains(p.getId()))
						continue;
					
					patientIds.add(p.getId());
				}
				
				Collections.reverse(patientIds);
			}
			
			String property = StringUtils.join(patientIds, ",");
			if (StringUtils.isNotBlank(property) && property.length() > 255) {
				//exceeded the user property max size and hence needs trimming.
				//find the last comma before index 255 and cut off from there
				//RA-200 Wyclif says patients ids at the end of the string are the most recent
				//so that is why we trim from begining instead of end.
				property = property.substring(property.indexOf(',', property.length() - 255) + 1);
			}
			
			userService.setUserProperty(user, EmrApiConstants.USER_PROPERTY_NAME_LAST_VIEWED_PATIENT_IDS,
				property);
		}
	}
}
