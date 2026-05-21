/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.diagnosis;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.test.builder.ObsBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class OldDiagnosisBuilder {
	
	private DiagnosisMetadata dmd;
	
	public OldDiagnosisBuilder(DiagnosisMetadata diagnosisMetadata) {
		this.dmd = diagnosisMetadata;
	}
	
	public ObsBuilder buildDiagnosis(Patient patient, String dateYmd, Diagnosis.Order order, Diagnosis.Certainty certainty,
	        Object diagnosis, Encounter encounter) {
		ObsBuilder builder = new ObsBuilder().setPerson(patient).setEncounter(encounter).setObsDatetime(parseYmd(dateYmd))
		        .setConcept(dmd.getDiagnosisSetConcept()).addMember(dmd.getDiagnosisOrderConcept(), dmd.getConceptFor(order))
		        .addMember(dmd.getDiagnosisCertaintyConcept(), dmd.getConceptFor(certainty));
		if (diagnosis instanceof Concept) {
			builder.addMember(dmd.getCodedDiagnosisConcept(), (Concept) diagnosis);
		} else if (diagnosis instanceof String) {
			builder.addMember(dmd.getNonCodedDiagnosisConcept(), (String) diagnosis);
		} else {
			throw new IllegalArgumentException("Diagnosis value must be a Concept or String");
		}
		return builder;
	}
	
	private Date parseYmd(String ymd) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(ymd);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
