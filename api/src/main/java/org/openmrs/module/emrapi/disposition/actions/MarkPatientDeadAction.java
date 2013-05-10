package org.openmrs.module.emrapi.disposition.actions;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Date;

public class MarkPatientDeadAction implements Action {

    @JsonProperty
    private String name;


   @Override
    public void action(EncounterDomainWrapper encounterDomainWrapper) {
       Patient patient = encounterDomainWrapper.getVisit().getPatient();
       Context.getPatientService().processDeath(patient, new Date(), null, "");
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

        MarkPatientDeadAction that = (MarkPatientDeadAction) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
