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

import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DiagnosisMapperTest {
	
	@Test
	public void shouldMapEmrapiDiagnosisToEncounterTransactionDiagnosis() throws Exception {
		DiagnosisMapper diagnosisMapper = new DiagnosisMapper();
		Diagnosis diagnosis = new Diagnosis();
		diagnosis.setCertainty(Diagnosis.Certainty.CONFIRMED);
		diagnosis.setOrder(Diagnosis.Order.PRIMARY);
		CodedOrFreeTextAnswer freeTextAnswer = new CodedOrFreeTextAnswer();
		freeTextAnswer.setNonCodedAnswer("cold");
		diagnosis.setDiagnosis(freeTextAnswer);
		Obs existingObs = new Obs();
		existingObs.setEncounter(new Encounter());
		existingObs.setComment("comment");
		diagnosis.setExistingObs(existingObs);
		
		EncounterTransaction.Diagnosis etDiagnosis = diagnosisMapper.convert(diagnosis);
		
		assertEquals(Diagnosis.Certainty.CONFIRMED.toString(), etDiagnosis.getCertainty());
		assertEquals(Diagnosis.Order.PRIMARY.toString(), etDiagnosis.getOrder());
		assertEquals("cold", etDiagnosis.getFreeTextAnswer());
		assertNull(etDiagnosis.getCodedAnswer());
		assertEquals("comment", etDiagnosis.getComments());
	}
}
