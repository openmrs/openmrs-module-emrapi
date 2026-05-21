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

import lombok.Setter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
public class EmrPatientServiceImpl extends BaseOpenmrsService implements EmrPatientService {
	
	private EmrPatientDAO dao;
	
	private EmrApiProperties emrApiProperties;
	
	private PatientService patientService;
	
	private AdtService adtService;
	
	@Override
	public List<Patient> findPatients(String query, Location checkedInAt, Integer start, Integer length) {
		if (checkedInAt != null) {
			checkedInAt = adtService.getLocationThatSupportsVisits(checkedInAt);
		}
		return dao.findPatients(query, checkedInAt, start, length);
	}
	
	@Override
	public Patient findPatientByPrimaryId(String primaryId) {
		if (primaryId == null) {
			throw new IllegalArgumentException("primary ID should not be null");
		}
		
		PatientIdentifierType primaryIdentifierType = emrApiProperties.getPrimaryIdentifierType();
		
		if (primaryIdentifierType == null) {
			throw new RuntimeException("primary identifier is not configured");
		}
		
		List<PatientIdentifierType> patientIdentifierTypes = new ArrayList<PatientIdentifierType>();
		patientIdentifierTypes.add(primaryIdentifierType);
		
		List<Patient> patients = patientService.getPatients(null, primaryId, patientIdentifierTypes, true);
		
		if (patients.isEmpty()) {
			throw new APIException("no such patient");
		}
		
		return patients.get(0);
	}
	
	@Override
	public List<Visit> getVisitsForPatient(Patient patient, Integer startIndex, Integer limit) {
		return dao.getVisitsForPatient(patient, startIndex, limit);
	}
	
	@Override
	public Map<Visit, List<Obs>> getVisitNoteObservations(Collection<Visit> visits) {
		Map<Visit, List<Obs>> ret = new HashMap<>();
		List<Obs> observations = dao.getVisitNoteObservations(visits);
		for (Visit visit : visits) {
			ret.put(visit, new ArrayList<>());
		}
		for (Obs obs : observations) {
			ret.get(obs.getEncounter().getVisit()).add(obs);
		}
		return ret;
	}
}
