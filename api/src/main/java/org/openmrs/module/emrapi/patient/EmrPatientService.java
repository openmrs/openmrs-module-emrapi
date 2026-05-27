/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.patient;

import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Public API for patient EMR-related functionality.
 */
public interface EmrPatientService {
	
	List<Patient> findPatients(String query, Location checkedInAt, Integer start, Integer length);
	
	Patient findPatientByPrimaryId(String primaryId);
	
	/**
	 * @return a List of Visits for the given patient, ordered by startDatetime descending, optionally
	 *         paged
	 */
	List<Visit> getVisitsForPatient(Patient patient, Integer startIndex, Integer limit);
	
	/**
	 * @return a Map from Visit to a List of observations contained in all Visit Note encounters within
	 *         the given Visit
	 */
	Map<Visit, List<Obs>> getVisitNoteObservations(Collection<Visit> visits);
}
