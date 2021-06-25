package org.openmrs.module.emrapi.exitfromcare;

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.OpenmrsService;

import java.util.Date;

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
    void closePatientPrograms(Patient patient, Concept outcome, Date date);

    /**
     * Reopens (ie, sets outcome and completion date to null) any programs that have an outcome equal to
     * the outcome parameter, and a completion date equal to the completion date parameter
     * @param patient
     * @param outcome
     * @param completionDate
     */
    void reopenPatientPrograms(Patient patient, Concept outcome, Date completionDate);


    /**
     * Close any active visits for the specified patient
     * (Finds all active visits and defers to ADTService.closeAndSaveVisit(visit) method to close
     *
     * @param patient
     */
    void closeActiveVisits(Patient patient);

    /**
     * Marks the patient dead with the specified cause of dead and death date
     * Closes any active programs or visits for that patient
     * TODO: better documentation here
     *
     * @param patient
     * @param causeOfDeath
     * @param deathDate
     */
    void markPatientDead(Patient patient, Concept causeOfDeath, Date deathDate);


    /**
     * Removes the flag that set the patient as dead, and removes any cause of death
     * Reopens any patient programs that have been closed with an outcome of "death" on the date of death
     * TODO: better documentation here
     *
     * @param patient
     */
    void markPatientNotDead(Patient patient);

}
