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
