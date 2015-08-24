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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.LocationService;
import org.openmrs.module.emrapi.adt.AdtAction;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.openmrs.module.emrapi.adt.AdtAction.Type.ADMISSION;

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

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private LocationService locationService;

    @Autowired
    private AdtService adtService;

    @Autowired
    private DispositionService dispositionService;

    /**
     * @param encounterDomainWrapper encounter that is being created (has not had dispositionObsGroupBeingCreated added yet)
     * @param dispositionObsGroupBeingCreated the obs group being created for this disposition (has not been added to the encounter yet)
     * @param requestParameters parameters submitted with the HTTP request, which may contain additional data neede by this action
     */
    @Override
    public void action(EncounterDomainWrapper encounterDomainWrapper, Obs dispositionObsGroupBeingCreated, Map<String, String[]> requestParameters) {

        VisitDomainWrapper visitDomainWrapper = adtService.wrap(encounterDomainWrapper.getVisit());

        // TODO note that we really want to only test if the patient is admitted at the encounter datetime; we also test against visitDomainWrapper.isAdmitted()
        // TODO for now because the "createAdtEncounterFor" method will throw an exception if isAdmitted() returns true; see https://minglehosting.thoughtworks.com/unicef/projects/pih_mirebalais/cards/938
        if (visitDomainWrapper.isAdmitted(encounterDomainWrapper.getEncounterDatetime()) || visitDomainWrapper.isAdmitted()) {
            // consider doing a transfer-within-hospital here
            return;
        }
        else {
            Location admissionLocation = dispositionService.getDispositionDescriptor().getAdmissionLocation(dispositionObsGroupBeingCreated, locationService);
            if (admissionLocation != null) {
                AdtAction admission = new AdtAction(encounterDomainWrapper.getVisit(), admissionLocation, encounterDomainWrapper.getProviders(), ADMISSION);
                admission.setActionDatetime(encounterDomainWrapper.getEncounter().getEncounterDatetime());
                adtService.createAdtEncounterFor(admission);
            }
            else {
                log.warn("Unable to create admission action, no admission location specified in obsgroup " + dispositionObsGroupBeingCreated);
            }
        }

    }

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

    public void setDispositionService(DispositionService dispositionService) {
        this.dispositionService = dispositionService;
    }

}
