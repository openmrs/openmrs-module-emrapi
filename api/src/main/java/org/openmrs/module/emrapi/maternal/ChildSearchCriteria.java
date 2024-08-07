package org.openmrs.module.emrapi.maternal;

import java.util.List;

import lombok.Data;
import org.openmrs.Patient;

@Data
public class ChildSearchCriteria {
    private List<String> motherUuids;
    private Boolean motherHasActiveVisit = false;
    private Boolean childHasActiveVisit = false;
    private Boolean childBornDuringMothersActiveVisit = false;

    public ChildSearchCriteria(List<String> motherUuids) {
        this.motherUuids = motherUuids;
    }

    public ChildSearchCriteria(List<String> motherUuids, Boolean motherHasActiveVisit, Boolean childHasActiveVisit, Boolean childBornDuringMothersActiveVisit) {
        this.motherUuids = motherUuids;
        this.motherHasActiveVisit = motherHasActiveVisit;
        this.childHasActiveVisit = childHasActiveVisit;
        this.childBornDuringMothersActiveVisit = childBornDuringMothersActiveVisit;
    }
}


