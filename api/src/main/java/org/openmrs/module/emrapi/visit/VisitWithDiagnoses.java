package org.openmrs.module.emrapi.visit;

import lombok.Getter;
import lombok.Setter;
import org.openmrs.Diagnosis;
import org.openmrs.Visit;

import java.util.Set;

@Setter
@Getter
public class VisitWithDiagnoses {
    
    private Visit visit;
    private Set<Diagnosis> diagnoses;

    public VisitWithDiagnoses(Visit visit, Set<Diagnosis> diagnoses) {
       this.visit = visit;
       this.diagnoses = diagnoses;
    }
    
}
