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

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.postprocessor.EncounterTransactionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Component(value = "encounterTransactionMapper")
public class EncounterTransactionMapper {
    private EncounterObservationsMapper encounterObservationsMapper;
    private EncounterProviderMapper encounterProviderMapper;
    private OrderMapper orderMapper;
    private List<EncounterTransactionHandler> encounterTransactionHandlers;

    @Autowired(required = false)
    public EncounterTransactionMapper(EncounterObservationsMapper encounterObservationsMapper, EncounterProviderMapper encounterProviderMapper) {
        this(encounterObservationsMapper, encounterProviderMapper, null);
    }

    @Autowired(required = false)
    public EncounterTransactionMapper(EncounterObservationsMapper encounterObservationsMapper, EncounterProviderMapper encounterProviderMapper, OrderMapper orderMapper) {
        this.encounterObservationsMapper = encounterObservationsMapper;
        this.encounterProviderMapper = encounterProviderMapper;
        this.orderMapper = orderMapper;
    }

    public EncounterTransaction map(Encounter encounter, Boolean includeAll) {
        Visit visit = encounter.getVisit();
        EncounterTransaction encounterTransaction = new EncounterTransaction(visit.getUuid(), encounter.getUuid());
        encounterTransaction.setPatientUuid(encounter.getPatient().getUuid());
        encounterTransaction.setEncounterTypeUuid(encounter.getEncounterType() != null ? encounter.getEncounterType().getUuid() : null);
        encounterTransaction.setLocationUuid(encounter.getLocation() != null ? encounter.getLocation().getUuid() : null);
        encounterTransaction.setLocationName(encounter.getLocation() != null ? encounter.getLocation().getName() : null);
        encounterTransaction.setVisitTypeUuid(visit.getVisitType().getUuid());
        encounterTransaction.setVisitLocationUuid(visit.getLocation() != null ? visit.getLocation().getUuid() : null);
        encounterTransaction.setEncounterDateTime(encounter.getEncounterDatetime());

        encounterProviderMapper.update(encounterTransaction, encounter.getEncounterProviders());
        encounterObservationsMapper.update(encounterTransaction, getSortedTopLevelObservations(encounter, includeAll));

        if (orderMapper != null) {
            encounterTransaction.setDrugOrders(orderMapper.mapDrugOrders(encounter));
            encounterTransaction.setOrders(orderMapper.mapOrders(encounter));
        }

        postProcessEncounter(encounter,encounterTransaction);

        return encounterTransaction;
    }

    private void postProcessEncounter(Encounter encounter,EncounterTransaction encounterTransaction){
        for(EncounterTransactionHandler encounterTransactionHandler: getEncounterTransactionHandlers()){
            encounterTransactionHandler.forRead(encounter, encounterTransaction);
        }
    }


    private Set<Obs> getSortedTopLevelObservations(Encounter encounter, Boolean includeAll) {
        TreeSet<Obs> sortedObservations = new TreeSet<Obs>(new Comparator<Obs>() {
            @Override
            public int compare(Obs o1, Obs o2) {
                if (shouldNotCompareOnCreatedDates(o2.getDateCreated(), o1.getDateCreated())) {
                    if (shouldNotCompareOnIds(o1.getId(), o2.getId()))
                        return 0;
                    return o2.getId().compareTo(o1.getId());
                }
                return o2.getDateCreated().compareTo(o1.getDateCreated());
            }
        });

        Set<Obs> observations = encounter.getObsAtTopLevel(includeAll);
        sortedObservations.addAll(observations);
        return sortedObservations;
    }

    private boolean shouldNotCompareOnIds(Integer firstId, Integer secondId) {
        return firstId == null || secondId == null;
    }

    private boolean shouldNotCompareOnCreatedDates(Date secondDate, Date firstDate) {
        return firstDate == null || secondDate == null || secondDate.equals(firstDate);
    }

    private List<EncounterTransactionHandler> getEncounterTransactionHandlers() {
        if (encounterTransactionHandlers == null) {
            encounterTransactionHandlers = Context.getRegisteredComponents(EncounterTransactionHandler.class);
            if (encounterTransactionHandlers == null) {
                encounterTransactionHandlers = new ArrayList<EncounterTransactionHandler>();
            }
        }
        return encounterTransactionHandlers;
    }
}
