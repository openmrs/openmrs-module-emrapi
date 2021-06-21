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
     * Close any active visits for the specified patient
     * (Finds all active visits and defers to ADTService.closeAndSaveVisit(visit) method to close
     *
     * @param patient
     */
    void closeActiveVisits(Patient patient);

    /**
     * Marks the patient date with the specified cause of dead and death date
     * Closes any active programs or visits for that patient
     * TODO: better documentation here
     *
     * @param patient
     * @param causeOfDeath
     * @param deathDate
     */
    void markPatientDied(Patient patient, Concept causeOfDeath, Date deathDate);

}
