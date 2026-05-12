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

import com.thoughtworks.xstream.mapper.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.EncounterProvider;
import org.openmrs.module.emrapi.encounter.builder.EncounterProviderBuilder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class EncounterProviderMapperTest {
	
	private EncounterProviderMapper encounterProviderMapper;
	
	@Before
	public void setUp() {
		encounterProviderMapper = new EncounterProviderMapper();
	}
	
	@Test
	public void shouldMapProviders() {
		EncounterTransaction encounterTransaction = new EncounterTransaction();
		Set<EncounterProvider> encounterProviders = new HashSet<EncounterProvider>();
		EncounterProvider encounterProvider = new EncounterProviderBuilder().build();
		encounterProviders.add(encounterProvider);
		
		encounterProviderMapper.update(encounterTransaction, encounterProviders);
		
		Set<EncounterTransaction.Provider> mappedProviders = encounterTransaction.getProviders();
		assertThat(mappedProviders.size(), is(1));
		EncounterTransaction.Provider provider = mappedProviders.iterator().next();
		assertThat(provider.getName(), is(equalTo(encounterProvider.getProvider().getName())));
		assertThat(provider.getEncounterRoleUuid(), is(equalTo(encounterProvider.getEncounterRole().getUuid())));
	}
	
	@Test
	public void shouldMapProvidersWithoutEncounterRole() {
		EncounterTransaction encounterTransaction = new EncounterTransaction();
		Set<EncounterProvider> encounterProviders = new HashSet<EncounterProvider>();
		EncounterProvider encounterProvider = new EncounterProviderBuilder().build();
		encounterProvider.setEncounterRole(null);
		encounterProviders.add(encounterProvider);
		
		encounterProviderMapper.update(encounterTransaction, encounterProviders);
		
		Set<EncounterTransaction.Provider> mappedProviders = encounterTransaction.getProviders();
		assertThat(mappedProviders.size(), is(1));
		EncounterTransaction.Provider provider = mappedProviders.iterator().next();
		assertThat(provider.getName(), is(equalTo(encounterProvider.getProvider().getName())));
		assertThat(provider.getEncounterRoleUuid(), is(nullValue()));
	}
}
