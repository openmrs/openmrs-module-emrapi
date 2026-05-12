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

import org.openmrs.EncounterProvider;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.HashSet;
import java.util.Set;

public class EncounterProviderMapper {
	
	public void update(EncounterTransaction encounterTransaction, Set<EncounterProvider> encounterProviders) {
		Set<EncounterTransaction.Provider> providers = convert(encounterProviders);
		encounterTransaction.setProviders(providers);
	}
	
	public Set<EncounterTransaction.Provider> convert(Set<EncounterProvider> encounterProviders) {
		Set<EncounterTransaction.Provider> providers = new HashSet<EncounterTransaction.Provider>();
		for (EncounterProvider encounterProvider : encounterProviders) {
			EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
			provider.setName(encounterProvider.getProvider().getName());
			provider.setUuid(encounterProvider.getProvider().getUuid());
			if (encounterProvider.getEncounterRole() != null) {
				provider.setEncounterRoleUuid(encounterProvider.getEncounterRole().getUuid());
			}
			providers.add(provider);
		}
		return providers;
	}
	
	public static class EmptyEncounterProviderMapper extends EncounterProviderMapper {
		
		public void update(EncounterTransaction encounterTransaction, Set<EncounterProvider> encounterProviders) {
			//do Nothing
		}
	}
	
}
