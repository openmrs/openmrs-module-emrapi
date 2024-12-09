package org.openmrs.module.emrapi.db;

import org.openmrs.Patient;
import org.openmrs.module.emrapi.visit.VisitWithDiagnoses;

import java.util.List;

public interface VisitDAO {

    List<VisitWithDiagnoses> getVisitsWithNotesAndDiagnosesByPatient(Patient patient, int startIndex, int limit);
}
