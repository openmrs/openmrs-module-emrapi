/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.person.image;

import org.openmrs.Person;
import org.openmrs.api.OpenmrsService;

/**
 * Public API for person image functionality.
 */
public interface EmrPersonImageService extends OpenmrsService {
	
	/**
	 * Saves the provided base64-encoded string as a jpeg image for the specified <code>Person</code>.
	 *
	 * @throws org.openmrs.api.APIException if save fails
	 */
	public PersonImage savePersonImage(PersonImage personImage);
	
	public PersonImage getCurrentPersonImage(Person person);
	
}
