/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.web.controller.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.openmrs.Concept;

/**
 * A dose form and the dose form group it belongs to, if any.
 * <p>
 * Converted to a representation by SimpleBeanConverter, which is registered for this class.
 */
@Data
@AllArgsConstructor
public class DoseFormMembership {
	
	private Concept doseForm;
	
	/**
	 * Null when no dose form group claims this dose form. A dose form belongs to at most one group.
	 */
	private Concept doseFormGroup;
}
