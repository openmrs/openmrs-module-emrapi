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

import org.apache.commons.lang.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Set;

public class EncounterProviderServiceHelper {
	
	private ProviderService providerService;
	
	private EncounterService encounterService;
	
	public EncounterProviderServiceHelper(ProviderService providerService, EncounterService encounterService) {
		this.providerService = providerService;
		this.encounterService = encounterService;
	}
	
	public void update(Encounter encounter, Set<EncounterTransaction.Provider> providers) {
		for (EncounterTransaction.Provider provider : providers) {
			EncounterProvider encounterProvider = findProvider(encounter, provider.getUuid(),
			    provider.getEncounterRoleUuid());
			if (encounterProvider == null) {
				
				EncounterRole encounterRole = null;
				
				if (StringUtils.isNotEmpty(provider.getEncounterRoleUuid())) {
					encounterRole = encounterService.getEncounterRoleByUuid(provider.getEncounterRoleUuid());
				}
				
				if (encounterRole == null) {
					encounterRole = encounterService.getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID);
				}
				
				encounter.addProvider(encounterRole, providerService.getProviderByUuid(provider.getUuid()));
			}
		}
	}
	
	// returns first matching provider by providerUuid and encounterRoleUuid; if encounterRoleUuid is null, just match on provider
	private EncounterProvider findProvider(Encounter encounter, String providerUuid, String encounterRoleUuid) {
		for (EncounterProvider encounterProvider : encounter.getEncounterProviders()) {
			if (StringUtils.equals(encounterProvider.getProvider().getUuid(), providerUuid)
			        && (StringUtils.isEmpty(encounterRoleUuid)
			                || (StringUtils.equals(encounterProvider.getEncounterRole().getUuid(), encounterRoleUuid)))) {
				return encounterProvider;
			}
		}
		return null;
	}
}
