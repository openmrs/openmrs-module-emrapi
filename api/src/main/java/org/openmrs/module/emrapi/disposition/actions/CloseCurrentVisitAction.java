package org.openmrs.module.emrapi.disposition.actions;


import org.openmrs.Obs;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Closes the visit that this encounter belongs to
 */
@Component
public class CloseCurrentVisitAction implements Action{

    @Override
    public void action(EncounterDomainWrapper encounterDomainWrapper, Obs dispositionObsGroupBeingCreated, Map<String, String[]> requestParameters){
        encounterDomainWrapper.closeVisit();
    }

}
