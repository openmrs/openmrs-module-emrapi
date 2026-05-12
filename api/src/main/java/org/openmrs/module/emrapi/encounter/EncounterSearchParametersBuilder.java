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
	
	public EncounterSearchParametersBuilder(EncounterSearchParameters encounterSearchParameters,
	    PatientService patientService, EncounterService encounterService, LocationService locationService,
	    ProviderService providerService, VisitService visitService) {
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
		for (String providerUuid : encounterSearchParameters.getProviderUuids()) {
			providers.add(providerService.getProviderByUuid(providerUuid));
		}
		return providers;
	}
	
	public Collection<VisitType> getVisitTypes() {
		Collection<VisitType> visitTypes = new HashSet<VisitType>();
		for (String visitTypeUuid : encounterSearchParameters.getVisitTypeUuids()) {
			visitTypes.add(visitService.getVisitTypeByUuid(visitTypeUuid));
		}
		return visitTypes;
	}
	
	public Collection<Visit> getVisits() {
		Collection<Visit> visits = new HashSet<Visit>();
		for (String visitUuid : encounterSearchParameters.getVisitUuids()) {
			visits.add(visitService.getVisitByUuid(visitUuid));
		}
		return visits;
	}
	
	public Boolean getIncludeAll() {
		return encounterSearchParameters.getIncludeAll();
	}
}
