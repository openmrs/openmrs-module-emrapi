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
