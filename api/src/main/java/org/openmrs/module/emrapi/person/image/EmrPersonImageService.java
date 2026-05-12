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
package org.openmrs.module.emrapi.person.image;

import org.openmrs.Person;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

/**
 * Public API for person image functionality.
 */
@Transactional(readOnly = true)
public interface EmrPersonImageService extends OpenmrsService {

    /**
     * Saves the provided base64-encoded string as a jpeg image for the specified <code>Person</code>.
     *
     * @throws org.openmrs.api.APIException
     *          if save fails
     */
    @Transactional
    @Authorized(PrivilegeConstants.EDIT_PATIENTS)
    public PersonImage savePersonImage(PersonImage personImage);

    @Authorized(PrivilegeConstants.GET_PATIENTS)
    public PersonImage getCurrentPersonImage(Person person);

}
