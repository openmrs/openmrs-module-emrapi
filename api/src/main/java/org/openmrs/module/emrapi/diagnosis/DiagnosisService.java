package org.openmrs.module.emrapi.diagnosis;

import org.openmrs.Concept;
import org.openmrs.Obs;
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
     * @param codedDiagnosis a Concept representing the coded Diagnosis to be entered
     * @return
     */
    Obs codeNonCodedDiagnosis(Obs nonCodedObs, Concept codedDiagnosis);

	/**
	 * Gets diagnoses since date.
	 *
	 * @param fromDate
	 * @return the list of diagnoses
	 */
	List<Diagnosis> getDiagnoses(Date fromDate);
}
