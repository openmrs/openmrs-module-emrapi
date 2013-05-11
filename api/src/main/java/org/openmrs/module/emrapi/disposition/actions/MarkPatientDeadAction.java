package org.openmrs.module.emrapi.disposition.actions;


import org.codehaus.jackson.annotate.JsonProperty;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.reporting.common.DateUtil;

import java.util.Date;
import java.util.Map;

public class MarkPatientDeadAction implements Action {

    public final String DEATH_DATE_PARAMETER = "deathDate";

    @JsonProperty
    private String name;

   @Override
    public void action(EncounterDomainWrapper encounterDomainWrapper, Obs dispositionObsGroupBeingCreated, Map<String, String[]> requestParameters) {
       Date deathDate = null;
       String[] deathDateParam = requestParameters.get(DEATH_DATE_PARAMETER);
       if (deathDateParam != null) {
           if (deathDateParam.length != 1) {
               throw new IllegalArgumentException("deathDate parameter should only be a single element, but it is: " + deathDateParam);
           }
           try {
               deathDate = DateUtil.parseDate(deathDateParam[0], "yyyy-MM-dd");
           } catch (Exception ex) {
               throw new IllegalArgumentException("cannot parse deathDate", ex);
           }
       }

       Patient patient = encounterDomainWrapper.getEncounter().getPatient();
       patient.setDead(true);
       if (deathDate != null) {
           patient.setDeathDate(deathDate);
       }
       Context.getPatientService().savePatient(patient);
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
