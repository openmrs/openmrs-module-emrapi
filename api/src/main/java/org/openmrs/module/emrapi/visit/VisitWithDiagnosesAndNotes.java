package org.openmrs.module.emrapi.visit;

import lombok.Data;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;

import java.util.List;

@Data
public class VisitWithDiagnosesAndNotes {
    private Visit visit;
    private List<Diagnosis> diagnoses;
    private List<Obs> diagnosesAsObs;
    private List<Obs> visitNotes;
}
