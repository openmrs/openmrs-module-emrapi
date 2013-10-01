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

import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransactionResponse;
import org.openmrs.module.emrapi.encounter.exception.EncounterMatcherNotFoundException;
import org.openmrs.module.emrapi.encounter.matcher.BaseEncounterMatcher;
import org.openmrs.module.emrapi.encounter.matcher.DefaultEncounterMatcher;

import java.util.*;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class EmrEncounterServiceImpl extends BaseOpenmrsService implements EmrEncounterService {

    private PatientService patientService;
    private VisitService visitService;
    private EncounterService encounterService;
    private EncounterObservationServiceHelper encounterObservationServiceHelper;
    private EncounterTestOrderServiceHelper encounterTestOrderServiceHelper;
    private EncounterDrugOrderServiceHelper encounterDrugOrderServiceHelper;
    private LocationService locationService;
    private ProviderService providerService;
    private AdministrationService administrationService;

    private Map<String, BaseEncounterMatcher> encounterMatcherMap = new HashMap<String, BaseEncounterMatcher>();

    public EmrEncounterServiceImpl(PatientService patientService, VisitService visitService, EncounterService encounterService,
                                   EncounterObservationServiceHelper encounterObservationServiceHelper, EncounterTestOrderServiceHelper encounterTestOrderServiceHelper,
                                   LocationService locationService, ProviderService providerService, AdministrationService administrationService, EncounterDrugOrderServiceHelper encounterDrugOrderServiceHelper) {
        this.patientService = patientService;
        this.visitService = visitService;
        this.encounterService = encounterService;
        this.encounterObservationServiceHelper = encounterObservationServiceHelper;
        this.encounterTestOrderServiceHelper = encounterTestOrderServiceHelper;
        this.locationService = locationService;
        this.providerService = providerService;
        this.administrationService = administrationService;
        this.encounterDrugOrderServiceHelper = encounterDrugOrderServiceHelper;
    }

    @Override
    public void onStartup() {
        try {
            super.onStartup();
            List<BaseEncounterMatcher> encounterMatchers = Context.getRegisteredComponents(BaseEncounterMatcher.class);
            for (BaseEncounterMatcher encounterMatcher : encounterMatchers) {
                encounterMatcherMap.put(encounterMatcher.getClass().getCanonicalName(), encounterMatcher);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public EncounterTransactionResponse save(EncounterTransaction encounterTransaction) {
        Patient patient = patientService.getPatientByUuid(encounterTransaction.getPatientUuid());
        Visit visit = findOrCreateVisit(encounterTransaction, patient);
        Encounter encounter = findOrCreateEncounter(encounterTransaction, patient, visit);

        encounterObservationServiceHelper.update(encounter, encounterTransaction.getObservations(), encounterTransaction.getEncounterDateTime());
        encounterObservationServiceHelper.updateDiagnoses(encounter, encounterTransaction.getDiagnoses(), encounterTransaction.getEncounterDateTime());
        encounterTestOrderServiceHelper.update(encounter, encounterTransaction.getTestOrders());
        encounterDrugOrderServiceHelper.update(encounter, encounterTransaction.getDrugOrders());

        visitService.saveVisit(visit);
        return new EncounterTransactionResponse(visit.getUuid(), encounter.getUuid());
    }

    private Encounter findOrCreateEncounter(EncounterTransaction encounterTransaction, Patient patient, Visit visit) {
        EncounterType encounterType = encounterService.getEncounterTypeByUuid(encounterTransaction.getEncounterTypeUuid());
        Location location = locationService.getLocationByUuid(encounterTransaction.getLocationUuid());

        Date encounterDateTime = encounterTransaction.getEncounterDateTime();
        Set<Provider> providers = getProviders(encounterTransaction.getProviderUuids());

        EncounterParameters encounterParameters = EncounterParameters.instance()
                                                    .setLocation(location).setEncounterType(encounterType)
                                                    .setProviders(providers).setEncounterDateTime(encounterDateTime)
                                                    .setPatient(patient);

        String matcherClass = administrationService.getGlobalProperty("emr.encounterMatcher");
        BaseEncounterMatcher encounterMatcher = isNotEmpty(matcherClass)? encounterMatcherMap.get(matcherClass) : new DefaultEncounterMatcher();
        if (encounterMatcher == null) {
            throw new EncounterMatcherNotFoundException();
        }
        Encounter encounter = encounterMatcher.findEncounter(visit, encounterParameters);

        if (encounter == null) {
            encounter = new Encounter();
            encounter.setPatient(patient);
            encounter.setEncounterType(encounterType);
            encounter.setEncounterDatetime(encounterDateTime);
            encounter.setUuid(UUID.randomUUID().toString());
            encounter.setObs(new HashSet<Obs>());
            visit.addEncounter(encounter);
        }
        return encounter;
    }

    private Set<Provider> getProviders(Set<String> providerUuids) {

        if (providerUuids == null){
            return Collections.EMPTY_SET;
        }

        Set<Provider> providers = new HashSet<Provider>();

        for (String providerUuid : providerUuids) {
            Provider provider = providerService.getProviderByUuid(providerUuid);
            providers.add(provider);
        }
        return providers;
    }

    private Visit findOrCreateVisit(EncounterTransaction encounterTransaction, Patient patient) {
        List<Visit> activeVisitsByPatient = visitService.getActiveVisitsByPatient(patient);

        if (!activeVisitsByPatient.isEmpty()) {
            return activeVisitsByPatient.get(0);
        }

        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setVisitType(visitService.getVisitTypeByUuid(encounterTransaction.getVisitTypeUuid()));
        visit.setStartDatetime(encounterTransaction.getEncounterDateTime());
        visit.setEncounters(new HashSet<Encounter>());
        visit.setUuid(UUID.randomUUID().toString());
        return visit;
    }

}
