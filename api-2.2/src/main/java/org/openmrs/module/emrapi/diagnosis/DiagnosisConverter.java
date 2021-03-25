package org.openmrs.module.emrapi.diagnosis;

import java.util.List;

public interface DiagnosisConverter {

//    List<Diagnosis> convert(List<org.openmrs.Diagnosis> coreDiagnoses);

    List<Diagnosis> convert(List<? extends Object> coreDiagnoses);
}
