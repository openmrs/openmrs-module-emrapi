package org.openmrs.module.emrapi.exitfromcare;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.OpenmrsService;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Transactional
public interface ExitFromCareService extends OpenmrsService {


    /**
     * Closes all active patient programs for the specified patient with the specified outcome
     * on the specified date.  If "outcome" is not a valid outcome for a program, do not close
     * that program
     *
     * @param patient
     * @param outcome
     * @param date
     */
    @Authorized(PrivilegeConstants.EDIT_PATIENTS)
    void closePatientPrograms(Patient patient, Concept outcome, Date date);

    /**
     * Reopens (ie, sets outcome and completion date to null) any programs that have an outcome equal to
     * the "outcome"
     *
     * @param patient
     * @param outcome
     */
    @Authorized(PrivilegeConstants.EDIT_PATIENTS)
    void reopenPatientPrograms(Patient patient, Concept outcome);


    /**
     * Updates the completion date of all completed programs with the specified outcome to the specified date
     *
     * @param patient
     * @param outcome
     * @param completionDate
     */
    @Authorized(PrivilegeConstants.EDIT_PATIENTS)
    void updatePatientProgramCompletionDate(Patient patient, Concept outcome, Date completionDate);


    /**
     * Close any active visits for the specified patient
     * (Finds all active visits and defers to ADTService.closeAndSaveVisit(visit) method to close
     *
     * @param patient
     */
    @Authorized(PrivilegeConstants.EDIT_PATIENTS)
    void closeActiveVisits(Patient patient);

    /**
     * Marks the patient dead with the specified cause of dead and death date
     * Closes any active programs or visits for that patient
     *
     * Specifically, this method will:
     * * Set the patient "dead" property to "true"
     * * Set the patient "causeOfDeath" property to "causeOfDeath"
     * * Set the patient "deathDate" property to "deathDate", or to the current Date Time if "deathDate" is null
     * * Call ExitFromCare.closeActiveVisits method for the patient
     * * If a concept exists with the mapping "org.openmrs.module.emrapi:Patient Died", call ExitFromCare.closePatientPrograms
     *      with date equal deathDate and outcome equal to the concept mapped by "org.openmrs.module.emrapi:Patient Died"
     *
     * @param patient
     * @param causeOfDeath
     * @param deathDate
     */
    @Authorized(PrivilegeConstants.EDIT_PATIENTS)
    void markPatientDead(Patient patient, Concept causeOfDeath, Date deathDate);


    /**
     * Removes the flag that set the patient as dead, and removes any cause of death
     * Reopens any patient programs that have been closed with an outcome of "death" on the date of death
     *
     * Specifically, this method will:
     * * Set the patient "dead" property to "false"
     * * Set the patient "causeOfDeath" property to "null"
     * * Set the patient "deathDate" property to "null"
     * * If a concept exists with the mapping "org.openmrs.module.emrapi:Patient Died", call ExitFromCare.reopenPatientPrograms
     *      with date equal to patient.deathDate (before it was set to null)
     *
     * @param patient
     */
    @Authorized(PrivilegeConstants.EDIT_PATIENTS)
    void markPatientNotDead(Patient patient);

}
