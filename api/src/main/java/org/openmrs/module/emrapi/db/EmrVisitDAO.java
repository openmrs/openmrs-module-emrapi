package org.openmrs.module.emrapi.db;

import java.util.List;

import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;

public interface EmrVisitDAO {
   
   List<Obs> getDiagnoses(Visit visit, DiagnosisMetadata diagnosisMetadata);
   
   List<Obs> getPrimaryDiagnoses(Visit visit, DiagnosisMetadata diagnosisMetadata);
   
   List<Obs> getConfirmedDiagnoses(Visit visit, DiagnosisMetadata diagnosisMetadata);
   
   List<Obs> getConfirmedPrimaryDiagnoses(Visit visit, DiagnosisMetadata diagnosisMetadata);
   
}
