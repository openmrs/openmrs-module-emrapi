/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.adt.util;

import org.openmrs.Concept;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.disposition.Disposition;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.disposition.DispositionType;

import java.util.ArrayList;
import java.util.List;

public class AdtUtil {
	
	public static List<Concept> getAdmissionDispositionsConcepts(EmrConceptService emrConceptService,
	        DispositionService dispositionService) {
		
		List<Disposition> admissionDispositions = dispositionService.getDispositionsByType(DispositionType.ADMIT);
		
		List<Concept> admissionDispositionConcepts = new ArrayList<Concept>();
		
		for (Disposition disposition : admissionDispositions) {
			admissionDispositionConcepts.add(emrConceptService.getConcept(disposition.getConceptCode()));
		}
		
		return admissionDispositionConcepts;
	}
	
}
