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
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.module.emrapi.visit.VisitWithDiagnosesAndNotes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Setter
public class EmrPatientServiceImpl extends BaseOpenmrsService implements EmrPatientService {
	
	private EmrPatientDAO dao;
	
	private EmrApiProperties emrApiProperties;
	
	private PatientService patientService;
	
	private AdtService adtService;

	private DiagnosisService emrDiagnosisService;
	
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
	public Map<Visit, List<Obs>> getVisitNoteObservations(List<Visit> visits) {
		Map<Visit, List<Obs>> ret = new HashMap<>();
		List<Obs> observations = dao.getVisitNoteObservations(visits);
		for (Obs obs : observations) {
			Visit visit = obs.getEncounter().getVisit();
			List<Obs> observationsForVisit = ret.computeIfAbsent(visit, k -> new ArrayList<>());
			observationsForVisit.add(obs);
		}
		return ret;
	}

	@Override
	public List<VisitWithDiagnosesAndNotes> getVisitsWithDiagnosesAndNotesByPatient(Patient patient, Integer startIndex, Integer limit) {
		List<Visit> visits = getVisitsForPatient(patient, startIndex, limit);
		Map<Visit, List<Obs>> notesObs = getVisitNoteObservations(visits);
		Map<Visit, List<Diagnosis>> diagnoses = emrDiagnosisService.getDiagnoses(visits);
        return visits.stream().map(visit -> {
			VisitWithDiagnosesAndNotes note = new VisitWithDiagnosesAndNotes();
			note.setVisit(visit);
			note.setDiagnoses(diagnoses.computeIfAbsent(visit, k -> new ArrayList<>()));
			note.setVisitNotes(notesObs.computeIfAbsent(visit, k -> new ArrayList<>()));
			return note;
		}).collect(Collectors.toList());
	}
}
