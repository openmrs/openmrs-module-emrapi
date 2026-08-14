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
 * A dose form group and its routes of administration.
 */
@Data
@AllArgsConstructor
public class DoseFormGroupRoutes {
	
	private Concept doseFormGroup;
	
	private List<Concept> routes;
}
