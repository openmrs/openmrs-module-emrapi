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

import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

public class EncounterSearchParametersBuilder {
    private EncounterSearchParameters encounterSearchParameters;
    private PatientService patientService;
    private EncounterService encounterService;
    private LocationService locationService;
    private ProviderService providerService;
    private VisitService visitService;

    public EncounterSearchParametersBuilder(EncounterSearchParameters encounterSearchParameters, PatientService patientService, EncounterService encounterService, LocationService locationService, ProviderService providerService, VisitService visitService) {
        this.encounterSearchParameters = encounterSearchParameters;
        this.patientService = patientService;
        this.encounterService = encounterService;
        this.locationService = locationService;
        this.providerService = providerService;
        this.visitService = visitService;
    }

    public Patient getPatient() {
       return patientService.getPatientByUuid(encounterSearchParameters.getPatientUuid());
    }

    public Location getLocation() {
        return locationService.getLocationByUuid(encounterSearchParameters.getLocationUuid());

    }

    public Date getStartDate() {
        return encounterSearchParameters.getEncounterDateTimeFrom();

    }

    public Date getEndDate() {
        return encounterSearchParameters.getEncounterDateTimeTo();

    }
    public Collection<EncounterType> getEncounterTypes() {
        Collection<EncounterType> encounterTypes = new HashSet<EncounterType>();
        for (String encounterTypeUuid : encounterSearchParameters.getEncounterTypeUuids()) {
            encounterTypes.add(encounterService.getEncounterTypeByUuid(encounterTypeUuid));
        }
        return encounterTypes;
    }

    public Collection<Provider> getProviders() {
        Collection<Provider> providers = new HashSet<Provider>();
        for (String providerUuid: encounterSearchParameters.getProviderUuids()){
            providers.add(providerService.getProviderByUuid(providerUuid));
        }
        return providers;
    }

    public Collection<VisitType> getVisitTypes() {
        Collection<VisitType> visitTypes = new HashSet<VisitType>();
        for (String visitTypeUuid: encounterSearchParameters.getVisitTypeUuids()){
            visitTypes.add(visitService.getVisitTypeByUuid(visitTypeUuid));
        }
        return visitTypes;
    }

    public Collection<Visit> getVisits() {
        Collection<Visit> visits = new HashSet<Visit>();
        for (String visitUuid: encounterSearchParameters.getVisitUuids()){
            visits.add(visitService.getVisitByUuid(visitUuid));
        }
        return visits;
    }

    public Boolean getIncludeAll() {
        return encounterSearchParameters.getIncludeAll();
    }
}


