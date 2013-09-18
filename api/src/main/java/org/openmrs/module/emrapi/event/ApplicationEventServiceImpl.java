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

import org.openmrs.Patient;
import org.openmrs.User;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.event.Event;
import org.openmrs.event.EventMessage;
import org.openmrs.module.emrapi.EmrApiConstants;

public class ApplicationEventServiceImpl extends BaseOpenmrsService implements ApplicationEventService {
	
	/**
	 * @see ApplicationEventService#patientViewed(org.openmrs.Patient, org.openmrs.User)
	 */
	@Override
	public void patientViewed(Patient patient, User user) {
		EventMessage eventMessage = new EventMessage();
		eventMessage.put(EmrApiConstants.EVENT_KEY_PATIENT_UUID, patient.getUuid());
		eventMessage.put(EmrApiConstants.EVENT_KEY_USER_UUID, user.getUuid());
		Event.fireEvent(EmrApiConstants.EVENT_TOPIC_NAME_PATIENT_VIEWED, eventMessage);
	}
}
