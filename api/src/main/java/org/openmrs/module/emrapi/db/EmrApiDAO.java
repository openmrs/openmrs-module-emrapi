package org.openmrs.module.emrapi.db;

import org.openmrs.Patient;
import org.openmrs.module.emrapi.visit.VisitWithDiagnoses;

import java.util.List;
import java.util.Map;

public interface EmrApiDAO {

    <T> List<T> executeHql(String queryString, Map<String, Object> parameters, Class<T> clazz);

    <T> List<T> executeHqlFromResource(String resource, Map<String, Object> parameters, Class<T> clazz);
    
    /**
     * Fetches visits with note encounters and diagnoses of a patient.
     *
     * @param patient the patient
     * @param startIndex the start index of the number of visits to fetch
     * @param limit the limit of the number of visits to fetch
     *
     * @return a list of visits that has note encounters and diagnoses
     */
    List<VisitWithDiagnoses> getVisitsWithNotesAndDiagnosesByPatient(Patient patient, int startIndex, int limit);
}
