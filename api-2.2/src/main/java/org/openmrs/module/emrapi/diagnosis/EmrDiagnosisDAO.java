package org.openmrs.module.emrapi.diagnosis;

import org.openmrs.Visit;

import java.util.List;

public interface EmrDiagnosisDAO {

    List<org.openmrs.Diagnosis> getDiagnoses(Visit visit, boolean primaryOnly, boolean confirmedOnly);
}
