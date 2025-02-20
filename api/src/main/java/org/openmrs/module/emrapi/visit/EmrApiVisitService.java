package org.openmrs.module.emrapi.visit;

import java.util.List;

public interface EmrApiVisitService {
    /**
     * Fetches visits with note encounters and diagnoses of a patient by patient ID.
     *
     * @param patientUuid the UUID of the patient
     * @return a list of visits that has note encounters and diagnoses
     */
    List<VisitWithDiagnosesAndNotes> getVisitsWithNotesAndDiagnosesByPatient(String patientUuid, int startIndex, int limit);
}
