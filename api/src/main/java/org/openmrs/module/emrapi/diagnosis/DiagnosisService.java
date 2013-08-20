package org.openmrs.module.emrapi.diagnosis;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.OpenmrsService;

/**
 * <pre>
 * API methods for managing diagnoses
 *
 *
 * </pre>
 */
public interface DiagnosisService extends OpenmrsService {

    /**
     * Changes a non-coded diagnosis to coded diagnosis
     * @param nonCodedObsId an Integer representing the ID of the Obs that contains the non-coded diagnosis
     * @param codedDiagnosis a Concept representing the coded Diagnosis to be entered
     * @return
     */
    Obs codeNonCodedDiagnosis(Integer nonCodedObsId, Concept codedDiagnosis);
}
