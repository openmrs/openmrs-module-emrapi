package org.openmrs.module.emrapi.visit;

import lombok.Getter;
import lombok.Setter;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Visit;

import java.util.Set;

@Setter
@Getter
public class VisitWithDiagnosesAndNotes {
    
    private Visit visit;
    private Set<Diagnosis> diagnoses;
    private Set<Encounter> visitNotes;

    public VisitWithDiagnosesAndNotes(Visit visit, Set<Diagnosis> diagnoses, Set<Encounter> visitNotes) {
       this.visit = visit;
       this.diagnoses = diagnoses;
       this.visitNotes = visitNotes;
    }
}
