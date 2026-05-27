/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.encounter;

import org.openmrs.Obs;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.matcher.ObservationTypeMatcher;

import java.util.Set;

public class EncounterObservationsMapper {
	
	private ObservationMapper observationMapper;
	
	private DiagnosisMapper diagnosisMapper;
	
	private DispositionMapper dispositionMapper;
	
	private ObservationTypeMatcher observationTypeMatcher;
	
	private DiagnosisMetadata diagnosisMetadata;
	
	private EmrApiProperties emrApiProperties;
	
	public EncounterObservationsMapper(ObservationMapper observationMapper, DiagnosisMapper diagnosisMapper,
	    DispositionMapper dispositionMapper, EmrApiProperties emrApiProperties,
	    ObservationTypeMatcher observationTypeMatcher) {
		this.observationMapper = observationMapper;
		this.diagnosisMapper = diagnosisMapper;
		this.dispositionMapper = dispositionMapper;
		this.emrApiProperties = emrApiProperties;
		this.observationTypeMatcher = observationTypeMatcher;
	}
	
	public void update(EncounterTransaction encounterTransaction, Set<Obs> allObs) {
		for (Obs obs : allObs) {
			ObservationTypeMatcher.ObservationType observationType = observationTypeMatcher.getObservationType(obs);
			switch (observationType) {
				case DIAGNOSIS:
					if (!obs.isVoided()) {
						encounterTransaction.addDiagnosis(diagnosisMapper.map(obs, getDiagnosisMetadata()));
					}
					break;
				case DISPOSITION:
					encounterTransaction.setDisposition(dispositionMapper.getDisposition(obs));
					break;
				default:
					encounterTransaction.addObservation(observationMapper.map(obs));
					break;
			}
		}
	}
	
	private DiagnosisMetadata getDiagnosisMetadata() {
		if (this.diagnosisMetadata == null) {
			this.diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
		}
		return this.diagnosisMetadata;
	}
	
}
