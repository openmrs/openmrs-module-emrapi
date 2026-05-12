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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Obs;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.matcher.ObservationTypeMatcher;

import java.util.Arrays;
import java.util.HashSet;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterObservationsMapperTest {
	
	private EncounterObservationsMapper encounterObservationsMapper;
	
	@Mock
	private DiagnosisMetadata diagnosisMetadata;
	
	@Mock
	private ObservationMapper observationMapper;
	
	@Mock
	private DiagnosisMapper diagnosisMapper;
	
	@Mock
	private DispositionMapper dispositionMapper;
	
	@Mock
	private EmrApiProperties emrApiProperties;
	
	@Mock
	private ObservationTypeMatcher observationTypeMatcher;
	
	@Before
	public void setUp() {
		initMocks(this);
		encounterObservationsMapper = new EncounterObservationsMapper(observationMapper, diagnosisMapper, dispositionMapper,
		        emrApiProperties, observationTypeMatcher);
		when(emrApiProperties.getDiagnosisMetadata()).thenReturn(diagnosisMetadata);
	}
	
	@Test
	public void testUpdateMapsDiagnosis() throws Exception {
		EncounterTransaction encounterTransaction = new EncounterTransaction();
		Obs obs1 = new Obs();
		Obs obs2 = new Obs();
		Obs obs3 = new Obs();
		Obs obs4 = new Obs();
		HashSet<Obs> allObs = new HashSet<Obs>(Arrays.asList(obs1, obs2, obs3, obs4));
		when(observationTypeMatcher.getObservationType(obs1)).thenReturn(ObservationTypeMatcher.ObservationType.DIAGNOSIS);
		when(observationTypeMatcher.getObservationType(obs2)).thenReturn(ObservationTypeMatcher.ObservationType.OBSERVATION);
		when(observationTypeMatcher.getObservationType(obs3)).thenReturn(ObservationTypeMatcher.ObservationType.DIAGNOSIS);
		
		EncounterTransaction.Disposition disposition = new EncounterTransaction.Disposition();
		when(observationTypeMatcher.getObservationType(obs4)).thenReturn(ObservationTypeMatcher.ObservationType.DISPOSITION);
		when(dispositionMapper.getDisposition(obs4)).thenReturn(disposition);
		
		encounterObservationsMapper.update(encounterTransaction, allObs);
		
		assertEquals(2, encounterTransaction.getDiagnoses().size());
		assertEquals(disposition, encounterTransaction.getDisposition());
		assertEquals(1, encounterTransaction.getObservations().size());
	}
	
	@Test
	public void updateShouldNotMapVoidedDiagnosis() throws Exception {
		EncounterTransaction encounterTransaction = new EncounterTransaction();
		Obs obs1 = new Obs();
		Obs obs2 = new Obs();
		obs2.setVoided(Boolean.TRUE);
		HashSet<Obs> allObs = new HashSet<Obs>(Arrays.asList(obs1, obs2));
		when(observationTypeMatcher.getObservationType(obs1)).thenReturn(ObservationTypeMatcher.ObservationType.DIAGNOSIS);
		when(observationTypeMatcher.getObservationType(obs2)).thenReturn(ObservationTypeMatcher.ObservationType.DIAGNOSIS);
		encounterObservationsMapper.update(encounterTransaction, allObs);
		
		assertEquals(1, encounterTransaction.getDiagnoses().size());
	}
}
