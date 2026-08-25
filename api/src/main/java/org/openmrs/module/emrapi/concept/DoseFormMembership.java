/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.concept;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.openmrs.Concept;

/**
 * A dose form and the dose form groups it belongs to, if any.
 */
@Data
@AllArgsConstructor
public class DoseFormMembership {
	
	private Concept doseForm;
	
	/**
	 * Empty when no dose form group claims this dose form. A dose form may belong to more than one
	 * group, e.g. "oral spray" belongs to both the oral spray group and the oral group, in which case
	 * its routes of administration are the union of the routes of all of its groups.
	 */
	private List<Concept> doseFormGroups;
}
