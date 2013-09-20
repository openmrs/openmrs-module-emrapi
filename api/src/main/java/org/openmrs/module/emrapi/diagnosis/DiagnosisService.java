package org.openmrs.module.emrapi.diagnosis;

import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.OpenmrsService;


import java.util.Date;
import java.util.List;

/**
 * <pre>
 * API methods for managing diagnoses
 * </pre>
 */
public interface DiagnosisService extends OpenmrsService {

    /**
     * Changes a non-coded diagnosis to coded diagnosis
     * @param nonCodedObs an Obs that contains the non-coded diagnosis
     * @param diagnoses a List of Diagnosis representing the new diagnoses
     * @return
     */
    List<Obs> codeNonCodedDiagnosis(Obs nonCodedObs, List<Diagnosis> diagnoses);

	/**
	 * Gets diagnoses since date.
	 *
	 * @param patient
	 * @param fromDate
	 * @return the list of diagnoses
	 */
	List<Diagnosis> getDiagnoses(Patient patient, Date fromDate);

	/**
	 * Gets unique diagnoses since date.
	 *
	 * @param patient
	 * @param fromDate
	 * @return the list of diagnoses
	 */
	List<Diagnosis> getUniqueDiagnoses(Patient patient, Date fromDate);
}
