/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.db;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;

import java.util.List;

/**
 * Useful queries for fetching OpenMRS objects beyond what are provided by the OpenMRS Core
 */
public interface EmrEncounterDAO {
	
	/**
	 * Returns all encounters that have an obs with the specified value text for the specified patient
	 *
	 * @param obsConcept the concept associated with the obs
	 * @param valueText the value text of the obs
	 * @param encounterType optionally limit to encounters of a certain type
	 * @param includeAll whether or not to include voided obs
	 * @return
	 */
	List<Encounter> getEncountersByObsValueText(Patient patient, Concept obsConcept, String valueText,
	        EncounterType encounterType, boolean includeAll);
	
	/**
	 * Returns all encounters that have an obs with the specified value text
	 *
	 * @param obsConcept the concept associated with the obs
	 * @param valueText the value text of the obs
	 * @param encounterType optionally limit to encounters of a certain type
	 * @param includeAll whether or not to include voided obs
	 * @return
	 */
	List<Encounter> getEncountersByObsValueText(Concept obsConcept, String valueText, EncounterType encounterType,
	        boolean includeAll);
	
}
