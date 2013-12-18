package org.openmrs.module.emrapi.disposition.actions;


import org.joda.time.DateMidnight;
import org.openmrs.Obs;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Closes the visit that this encounter belongs to
 */
@Component("closeCurrentVisitDispositionAction")
public class CloseCurrentVisitDispositionAction implements DispositionAction {

    @Autowired
    private AdtService adtService;

    @Autowired
    private VisitService visitService;

    @Override
    public void action(EncounterDomainWrapper encounterDomainWrapper, Obs dispositionObsGroupBeingCreated, Map<String, String[]> requestParameters){

        if (encounterDomainWrapper.getVisit() == null) {
            return;
        }

        VisitDomainWrapper visitDomainWrapper = adtService.wrap(encounterDomainWrapper.getVisit());

        // if this is an active visit, we close it... *as long as* there are no subsequent encounters on following days
        // (if there are subsequent encounters on following days, this is some sort of retrospective entry, and we can't
        // determine exactly what should happen, so just do nothing)

        if (visitDomainWrapper.isActive()) {
            Date mostRecentEncounterDatetime  = visitDomainWrapper.getMostRecentEncounter().getEncounterDatetime();

            if (!new DateMidnight(mostRecentEncounterDatetime).isAfter(new DateMidnight(encounterDomainWrapper.getEncounterDatetime()))) {
                visitDomainWrapper.closeOnLastEncounterDatetime();
                visitService.saveVisit(visitDomainWrapper.getVisit());
            }

        }
    }

    /**
     * For unit testing to inject mocks
     */
    public void setAdtService(AdtService adtService) {
        this.adtService = adtService;
    }

    public void setVisitService(VisitService visitService) {
        this.visitService = visitService;
    }


}

