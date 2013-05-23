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
package org.openmrs.module.emrapi.visit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Visit;
import org.openmrs.api.db.VisitDAO;

@RunWith(MockitoJUnitRunner.class)
public class VisitDomainWrapperRepositoryTest {
	
	@InjectMocks
	private VisitDomainWrapperRepository repository;
	
	@MockitoAnnotations.Mock
	private VisitDAO visitDAO;
	
	@Before
	public void setUp() {
		repository = new VisitDomainWrapperRepository();
		visitDAO = mock(VisitDAO.class);
		
		initMocks(this);
	}
	
	@Test
	public void shouldPersistAVisitDomainWrapper() throws Exception {
		Visit visit = new Visit();
		VisitDomainWrapper wrapper = new VisitDomainWrapper(visit);
		
		repository.persist(wrapper);
		
		verify(visitDAO).saveVisit(visit);
	}
	
}
