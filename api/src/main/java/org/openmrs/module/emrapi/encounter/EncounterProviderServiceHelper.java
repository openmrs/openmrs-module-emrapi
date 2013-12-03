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
package org.openmrs.module.emrapi.encounter;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Provider;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.ProviderService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.openmrs.module.emrapi.printer.PrinterService;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
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
            EncounterProvider encounterProvider = findProvider(encounter, provider.getUuid());
            if(encounterProvider == null) {
                encounter.addProvider(encounterService.getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID), providerService.getProviderByUuid(provider.getUuid()));
            }
        }
    }

    private EncounterProvider findProvider(Encounter encounter, String providerUuid) {
        for (EncounterProvider encounterProvider : encounter.getEncounterProviders()) {
            if(StringUtils.equals(encounterProvider.getProvider().getUuid(), providerUuid))
                return encounterProvider;
        }
        return null;
    }
}