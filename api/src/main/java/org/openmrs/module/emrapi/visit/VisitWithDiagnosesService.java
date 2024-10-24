package org.openmrs.module.emrapi.visit;

import java.util.List;

public interface VisitWithDiagnosesService {
    /**
     * Fetches visits of a patient by patient ID.
     *
     * @param patientUuid the UUID of the patient
     * @return a list of visits
     */
    List<VisitWithDiagnoses> getVisitsByPatientId(String patientUuid, int startIndex, int limit);
}
