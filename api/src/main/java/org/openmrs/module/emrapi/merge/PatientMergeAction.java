package org.openmrs.module.emrapi.merge;

import org.openmrs.Patient;

/**
 * {@link org.openmrs.module.emrapi.adt.AdtService#mergePatients(org.openmrs.Patient, org.openmrs.Patient)} will invoke all
 * Spring beans that implement this within the same transaction that it uses to merge patients.
 */
public interface PatientMergeAction {

    /**
     * This method will be called before calling the underlying OpenMRS
     * {@link org.openmrs.api.PatientService#mergePatients(org.openmrs.Patient, org.openmrs.Patient)} method, but in the
     * same transaction. Any thrown exception will cancel the merge
     *
     * @param preferred
     * @param notPreferred
     */
    void beforeMergingPatients(Patient preferred, Patient notPreferred);

    /**
     * This method will be called after calling the underlying OpenMRS
     * {@link org.openmrs.api.PatientService#mergePatients(org.openmrs.Patient, org.openmrs.Patient)} method, but in the
     * same transaction.
     *
     * @param preferred
     * @param notPreferred
     */
    void afterMergingPatients(Patient preferred, Patient notPreferred);

}
