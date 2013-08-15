package org.openmrs.module.emrapi.disposition.actions;


import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Closes the visit that this encounter belongs to
 */
@Component("closeCurrentVisitDispositionAction")
public class CloseCurrentVisitDispositionAction implements DispositionAction {

    @Override
    public void action(EncounterDomainWrapper encounterDomainWrapper, Obs dispositionObsGroupBeingCreated, Map<String, String[]> requestParameters){

        Visit visit = encounterDomainWrapper.getVisit();

        if (visit != null) {
            new VisitDomainWrapper(visit).closeOnLastEncounterDatetime();
        }
    }
}
