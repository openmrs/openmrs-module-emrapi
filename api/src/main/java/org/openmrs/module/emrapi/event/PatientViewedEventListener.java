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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.Daemon;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.utils.GeneralUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Listens for {@link PatientViewedEvent}s, the viewed patient gets added to the last viewed
 * patients user property of the user who viewed them.
 */
@Component
public class PatientViewedEventListener {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private volatile DaemonToken daemonToken;
	
	/**
	 * Called by {@link org.openmrs.module.emrapi.EmrApiActivator} with the token core passes the
	 * module.
	 */
	public void setDaemonToken(DaemonToken daemonToken) {
		this.daemonToken = daemonToken;
	}
	
	/**
	 * Updates the user property in a daemon thread, so that viewing a patient neither waits for the
	 * update nor fails because of it.
	 */
	@EventListener
	public void onPatientViewed(PatientViewedEvent event) {
		if (daemonToken == null) {
			log.warn("Not updating the last viewed patients of user " + event.getUserUuid()
			        + " because the emrapi module has not been started");
			return;
		}
		try {
			Daemon.runInDaemonThreadWithoutResult(() -> {
				try {
					processEvent(event);
				}
				catch (Exception e) {
					log.error("Failed to update the user's last viewed patients property", e);
				}
			}, daemonToken);
		}
		catch (Exception e) {
			log.error("Failed to start updating the user's last viewed patients property", e);
		}
	}
	
	/**
	 * Processes the specified patient viewed event
	 * 
	 * @should add the patient to the last viewed user property
	 * @should remove the first patient and add the new one to the start if the list is full
	 * @should not add a duplicate and should move the existing patient to the start
	 * @should not remove any patient if a duplicate is added to a full list
	 */
	public void processEvent(PatientViewedEvent event) {
		String patientUuid = event.getPatientUuid();
		Patient patientToAdd = Context.getPatientService().getPatientByUuid(patientUuid);
		if (patientToAdd == null || patientToAdd.getId() == null) {
			throw new APIException("failed to find a patient with uuid:" + patientUuid + " or the patient is not yet saved");
		}
		
		UserService userService = Context.getUserService();
		User user = userService.getUserByUuid(event.getUserUuid());
		if (user != null) {
			EmrApiProperties emrProperties = Context.getRegisteredComponents(EmrApiProperties.class).iterator().next();
			Integer limit = emrProperties.getLastViewedPatientSizeLimit();
			List<Integer> patientIds = new ArrayList<Integer>();
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
			
			String property = StringUtils.join(patientIds, ",");
			if (StringUtils.isNotBlank(property) && property.length() > 255) {
				//exceeded the user property max size and hence needs trimming.
				//find the last comma before index 255 and cut off from there
				//RA-200 Wyclif says patients ids at the end of the string are the most recent
				//so that is why we trim from begining instead of end.
				property = property.substring(property.indexOf(',', property.length() - 255) + 1);
			}
			
			userService.setUserProperty(user, EmrApiConstants.USER_PROPERTY_NAME_LAST_VIEWED_PATIENT_IDS, property);
		}
	}
}
