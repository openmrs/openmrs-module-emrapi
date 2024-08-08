package org.openmrs.module.emrapi.maternal;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MothersAndChildrenSearchCriteria {
    private List<String> motherUuids;  // restrict to children of these mothers
    private List<String> childUuids;  // restrict to mothers of these children
    private boolean requireMotherHasActiveVisit = false;  // restrict to mothers who have an active visit
    private boolean requireChildHasActiveVisit = false;  // restrict to mothers of children who have an active visit
    private boolean requireChildBornDuringMothersActiveVisit = false;  // restrict to mothers who had a child born during their active visit

}
