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
import org.openmrs.Order;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class EncounterTransactionMapper {
    protected EncounterObservationsMapper encounterObservationsMapper;
    protected  EncounterOrdersMapper encounterOrdersMapper;
    protected  EncounterProviderMapper encounterProviderMapper;

    public EncounterTransactionMapper(EncounterObservationsMapper encounterObservationsMapper, EncounterOrdersMapper encounterOrdersMapper, EncounterProviderMapper encounterProviderMapper) {
        this.encounterObservationsMapper = encounterObservationsMapper;
        this.encounterOrdersMapper = encounterOrdersMapper;
        this.encounterProviderMapper = encounterProviderMapper;
    }


    public EncounterTransaction map(Encounter encounter, Boolean includeAll) {
        EncounterTransaction encounterTransaction = new EncounterTransaction(encounter.getVisit().getUuid(), encounter.getUuid());
        encounterTransaction.setPatientUuid(encounter.getPatient().getUuid());
        encounterTransaction.setEncounterTypeUuid(encounter.getEncounterType().getUuid());
        encounterTransaction.setLocationUuid(encounter.getLocation() != null ? encounter.getLocation().getUuid() : null);
        encounterTransaction.setVisitTypeUuid(encounter.getVisit().getVisitType().getUuid());
        encounterTransaction.setEncounterDateTime(encounter.getEncounterDatetime());

        encounterProviderMapper.update(encounterTransaction, encounter.getEncounterProviders());

        encounterObservationsMapper.update(encounterTransaction, getSortedTopLevelObservations(encounter, includeAll));
        encounterOrdersMapper.update(encounterTransaction, getSortedOrders(encounter));
        return encounterTransaction;
    }

    private Set<Order> getSortedOrders(Encounter encounter) {
        Comparator<Order> comparator = new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return o2.getDateCreated().compareTo(o1.getDateCreated());
            }
        };
        ArrayList<Order> list = new ArrayList<Order>(encounter.getOrders());
        Collections.sort(list, comparator);
        return new LinkedHashSet<Order>(list);
    }

    private Set<Obs> getSortedTopLevelObservations(Encounter encounter, Boolean includeAll) {
        Comparator<Obs> comparator = new Comparator<Obs>() {
            @Override
            public int compare(Obs o1, Obs o2) {
                return o2.getObsDatetime().compareTo(o1.getObsDatetime());
            }
        };
        ArrayList<Obs> list = new ArrayList<Obs>(encounter.getObsAtTopLevel(includeAll));
        Collections.sort(list, comparator);
        return new LinkedHashSet<Obs>(list);
    }

}
