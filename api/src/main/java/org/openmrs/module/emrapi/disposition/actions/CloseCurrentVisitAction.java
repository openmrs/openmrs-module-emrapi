package org.openmrs.module.emrapi.disposition.actions;


import org.codehaus.jackson.annotate.JsonProperty;
import org.openmrs.Obs;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;

import java.util.Map;

public class CloseCurrentVisitAction implements Action{

    @JsonProperty
    private String name;


    @Override
    public void action(EncounterDomainWrapper encounterDomainWrapper, Obs dispositionObsGroupBeingCreated, Map<String, String[]> requestParameters){
        encounterDomainWrapper.closeVisit();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CloseCurrentVisitAction that = (CloseCurrentVisitAction) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
