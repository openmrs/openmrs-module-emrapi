package org.openmrs.module.emrapi.disposition.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.LocationService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtAction;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.openmrs.module.emrapi.adt.AdtAction.Type.TRANSFER;

/**
 *
 */
@Component("transferToSpecificLocationDispositionAction")
public class TransferToSpecificLocationDispositionAction implements DispositionAction {

    private final Log log = LogFactory.getLog(getClass());

    @Autowired
    private LocationService locationService;

    @Autowired
    private AdtService adtService;

    @Autowired
    private DispositionService dispositionService;

    /**
     * For unit testing
     * @param locationService
     */
    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }


    /**
     * @param encounterDomainWrapper encounter that is being created (has not had dispositionObsGroupBeingCreated added yet)
     * @param dispositionObsGroupBeingCreated the obs group being created for this disposition (has not been added to the encounter yet)
     * @param requestParameters parameters submitted with the HTTP request, which may contain additional data neede by this action
     */
    @Override
    public void action(EncounterDomainWrapper encounterDomainWrapper, Obs dispositionObsGroupBeingCreated, Map<String, String[]> requestParameters) {

        VisitDomainWrapper visitDomainWrapper  = adtService.wrap(encounterDomainWrapper.getVisit());
        Location transferLocation = dispositionService.getDispositionDescriptor().getInternalTransferLocation(dispositionObsGroupBeingCreated, locationService);
        Location currentInpatientLocation = visitDomainWrapper.getInpatientLocation(encounterDomainWrapper.getEncounterDatetime());

        if (transferLocation == null) {
            log.warn("Unable to create transfer action, no transfer location specified in obsgroup " + dispositionObsGroupBeingCreated);
            return;
        }
        // transfer the patient if a) they are not admitted (and therefore have no inpatient location) or b) the inpatient location is other than the transfer location
        if (currentInpatientLocation == null || !currentInpatientLocation.equals(transferLocation)) {
            AdtAction transfer = new AdtAction(encounterDomainWrapper.getVisit(), transferLocation, encounterDomainWrapper.getProviders(), TRANSFER);
            transfer.setActionDatetime(encounterDomainWrapper.getEncounter().getEncounterDatetime());
            adtService.createAdtEncounterFor(transfer);
        }

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
