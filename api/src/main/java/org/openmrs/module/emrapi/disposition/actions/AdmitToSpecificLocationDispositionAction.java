/*
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

package org.openmrs.module.emrapi.disposition.actions;

import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.LocationService;
import org.openmrs.module.emrapi.adt.Admission;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Will actually admit the patient to inpatient (using {@link AdtService#admitPatient(org.openmrs.module.emrapi.adt.Admission)}).
 *
 * This is not the recommended workflow -- normally you would expect a disposition of Admission to be an order for
 * admission, not the official admission itself, as this should be driven by the inpatient ward receiving the patient.
 *
 * However in a hacky shortcut workflow you can use this action to have an Admission disposition immediately admit a
 * patient.
 */
@Component("admitToSpecificLocationDispositionAction")
public class AdmitToSpecificLocationDispositionAction implements DispositionAction {

    public final static String ADMISSION_LOCATION_PARAMETER = "admitToLocationId";

    @Autowired
    LocationService locationService;

    @Autowired
    AdtService adtService;

    /**
     * For unit testing
     * @param locationService
     */
    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    /**
     * For unit testing
     * @param adtService
     */
    public void setAdtService(AdtService adtService) {
        this.adtService = adtService;
    }

    /**
     * Requires a request parameter of {@link #ADMISSION_LOCATION_PARAMETER}
     * @param encounterDomainWrapper encounter that is being created (has not had dispositionObsGroupBeingCreated added yet)
     * @param dispositionObsGroupBeingCreated the obs group being created for this disposition (has not been added to the encounter yet)
     * @param requestParameters parameters submitted with the HTTP request, which may contain additional data neede by this action
     */
    @Override
    public void action(EncounterDomainWrapper encounterDomainWrapper, Obs dispositionObsGroupBeingCreated, Map<String, String[]> requestParameters) {
        String locationId = DispositionActionUtils.getSingleRequiredParameter(requestParameters, ADMISSION_LOCATION_PARAMETER);
        Location location = locationService.getLocation(Integer.valueOf(locationId));
        adtService.admitPatient(new Admission(encounterDomainWrapper.getEncounter().getPatient(), location, encounterDomainWrapper.getProviders()));
    }

}
