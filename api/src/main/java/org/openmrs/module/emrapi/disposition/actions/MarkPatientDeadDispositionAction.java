package org.openmrs.module.emrapi.disposition.actions;


import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.reporting.common.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

/**
 * Sets the death and deathDate fields on a Patient, and saves those changes.
 */
@Component("markPatientDeadDispositionAction")
public class MarkPatientDeadDispositionAction implements DispositionAction {

    public final String DEATH_DATE_PARAMETER = "deathDate";

    @Autowired
    PatientService patientService;

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
       patientService.savePatient(patient);
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

}
