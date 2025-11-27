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
package org.openmrs.module.emrapi.patient;

import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.annotation.Authorized;
import org.openmrs.util.PrivilegeConstants;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Public API for patient EMR-related functionality.
 */
public interface EmrPatientService {
	
	@Authorized(PrivilegeConstants.VIEW_PATIENTS)
	List<Patient> findPatients(String query, Location checkedInAt, Integer start, Integer length);
	
	@Authorized(PrivilegeConstants.VIEW_PATIENTS)
	Patient findPatientByPrimaryId(String primaryId);

	/**
	 * @return a List of Visits for the given patient, ordered by startDatetime descending, optionally paged
	 */
	@Authorized(PrivilegeConstants.VIEW_PATIENTS)
	List<Visit> getVisitsForPatient(Patient patient, Integer startIndex, Integer limit);

	/**
	 * @return a Map from Visit to a List of observations contained in all Visit Note encounters within the given Visit
	 */
	@Authorized(PrivilegeConstants.VIEW_PATIENTS)
	Map<Visit, List<Obs>> getVisitNoteObservations(Collection<Visit> visits);
}
