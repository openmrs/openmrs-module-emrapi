package org.openmrs.module.emrapi.visit;

import lombok.Getter;
import lombok.Setter;
import org.openmrs.*;

import java.util.Set;

@Setter
@Getter
public class VisitWithDiagnoses extends Visit {


    public VisitWithDiagnoses(Visit visit, Set<Diagnosis> diagnoses) {
        super();
        this.setVisitId(visit.getVisitId());
        this.setPatient(visit.getPatient());
        this.setVisitType(visit.getVisitType());
        this.setIndication(visit.getIndication());
        this.setLocation(visit.getLocation());
        this.setStartDatetime(visit.getStartDatetime());
        this.setStopDatetime(visit.getStopDatetime());
        this.setEncounters(visit.getEncounters());
        this.diagnoses = diagnoses;
    }

    private Set<Diagnosis> diagnoses;


}
