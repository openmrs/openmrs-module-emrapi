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

/**
 * Public API for Application level events
 * 
 * @since 1.0
 */
public interface ApplicationEventService {
	
	/**
	 * Fires an application level event that the specified patient has been viewed by the specified
	 * user.
	 * 
	 * @should publish the patient viewed event
	 */
	public void patientViewed(Patient patient, User user);
}
