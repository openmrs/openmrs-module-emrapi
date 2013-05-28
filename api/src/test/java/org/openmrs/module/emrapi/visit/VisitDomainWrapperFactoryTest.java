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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;

@RunWith(MockitoJUnitRunner.class)
public class VisitDomainWrapperFactoryTest {
	
	@InjectMocks
	private VisitDomainWrapperFactory factory;
	
	@MockitoAnnotations.Mock
	private EmrApiProperties emrApiProperties;
	
	@Before
	public void setUp() {
		factory = new VisitDomainWrapperFactory();
		emrApiProperties = mock(EmrApiProperties.class);
		initMocks(this);
	}
	
	@Test
	public void shouldCreateWrapperForNewVisit() throws Exception {
		VisitType visitType = new VisitType();
		when(emrApiProperties.getAtFacilityVisitType()).thenReturn(visitType);
		
		Patient patient = new Patient();
		Location parentLocation = new Location();
		LocationTag locationTag = new LocationTag();
		locationTag.setName(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS);
		parentLocation.addTag(locationTag);
		Location location = new Location();
		location.setParentLocation(parentLocation);
		Date visitTime = new Date();
		VisitDomainWrapper visitWrapper = factory.createNewVisit(patient, location, visitTime);
		
		Visit visit = visitWrapper.getVisit();
		assertThat(visit.getId(), is(nullValue()));
		assertThat(visit.getPatient(), is(patient));
		assertThat(visit.getLocation(), is(parentLocation));
		assertThat(visit.getVisitType(), is(visitType));
		assertThat(visit.getStartDatetime(), is(visitTime));
	}
}
