package org.openmrs.module.emrapi.maternal;

import java.util.List;

import lombok.Data;

@Data
public class MotherSearchCriteria {
    private List<String> childUuids;
    private Boolean motherHasActiveVisit = false;
    private Boolean childHasActiveVisit = false;

    public MotherSearchCriteria(List<String> childUuids) {
        this.childUuids = childUuids;
    }

    public MotherSearchCriteria(List<String> childUuids, Boolean motherHasActiveVisit, Boolean childHasActiveVisit) {
        this.childUuids = childUuids;
        this.motherHasActiveVisit = motherHasActiveVisit;
        this.childHasActiveVisit = childHasActiveVisit;
    }
}
