/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.visit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.visit.contract.VisitResponse;

import static org.mockito.MockitoAnnotations.initMocks;

public class VisitResponseMapperTest {
	
	@Mock
	private EncounterTransactionMapper encounterTransactionMapper;
	
	private VisitResponseMapper visitResponseMapper;
	
	@BeforeEach
	public void setUp() {
		initMocks(this);
		visitResponseMapper = new VisitResponseMapper(encounterTransactionMapper);
	}
	
	@Test
	public void testMapsVisit() throws Exception {
		Visit visit = new Visit();
		visit.addEncounter(new Encounter());
		
		VisitResponse visitResponse = visitResponseMapper.map(visit);
		
		Assertions.assertEquals(visit.getUuid(), visitResponse.getVisitUuid());
		Assertions.assertEquals(visit.getEncounters().size(), visitResponse.getEncounters().size());
	}
	
	@Test
	public void testMapsNullVisitToNull() throws Exception {
		Assertions.assertNull(visitResponseMapper.map(null));
	}
}
