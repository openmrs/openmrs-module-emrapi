package org.openmrs.module.emrapi.encounter;

import org.openmrs.Visit;

public class VisitWrapper {

    private Visit visit;
    private boolean isNewVisit;

    public Visit getVisit() {
        return visit;
    }

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public boolean getNewVisit() {
        return isNewVisit;
    }

    public void setNewVisit(boolean newVisit) {
        isNewVisit = newVisit;
    }
}
