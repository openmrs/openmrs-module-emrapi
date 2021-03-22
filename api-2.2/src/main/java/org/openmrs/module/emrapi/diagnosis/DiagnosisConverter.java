package org.openmrs.module.emrapi.diagnosis;

import java.util.List;

public interface DiagnosisConverter {

    Diagnosis convert(org.openmrs.Diagnosis coreDiagnosis);

    List<Diagnosis> convert(List<org.openmrs.Diagnosis> coreDiagnoses);
}
