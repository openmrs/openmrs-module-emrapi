package org.openmrs.module.emrapi.visit;

import lombok.Getter;
import lombok.Setter;
import org.openmrs.Diagnosis;
import org.openmrs.Obs;
import org.openmrs.Visit;

import java.util.List;

@Setter
@Getter
public class VisitWithDiagnosesAndNotes {
    
    private Visit visit;
    private List<Diagnosis> diagnoses;
    private List<Obs> visitNotes;

    public VisitWithDiagnosesAndNotes(Visit visit, List<Diagnosis> diagnoses, List<Obs> visitNotes) {
       this.visit = visit;
       this.diagnoses = diagnoses;
       this.visitNotes = visitNotes;
    }
}
