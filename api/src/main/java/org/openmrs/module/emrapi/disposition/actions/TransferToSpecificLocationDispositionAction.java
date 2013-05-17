package org.openmrs.module.emrapi.disposition.actions;

import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.LocationService;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.Transfer;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 *
 */
@Component("transferToSpecificLocationDispositionAction")
public class TransferToSpecificLocationDispositionAction implements DispositionAction {

    public static final String TRANSFER_LOCATION_PARAMETER = "transferToLocationId";

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
     * Requires a request parameter of {@link #TRANSFER_LOCATION_PARAMETER}
     * @param encounterDomainWrapper encounter that is being created (has not had dispositionObsGroupBeingCreated added yet)
     * @param dispositionObsGroupBeingCreated the obs group being created for this disposition (has not been added to the encounter yet)
     * @param requestParameters parameters submitted with the HTTP request, which may contain additional data neede by this action
     */
    @Override
    public void action(EncounterDomainWrapper encounterDomainWrapper, Obs dispositionObsGroupBeingCreated, Map<String, String[]> requestParameters) {
        String locationId = DispositionActionUtils.getSingleRequiredParameter(requestParameters, TRANSFER_LOCATION_PARAMETER);
        Location location = locationService.getLocation(Integer.valueOf(locationId));
        adtService.transferPatient(new Transfer(encounterDomainWrapper.getVisit(), location, encounterDomainWrapper.getProviders()));
    }

}
