package org.openmrs.module.emrapi.maternal;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MothersByChildrenSearchCriteria {
    private List<String> childUuids;  // restrict to mothers of these children
    @Accessors(fluent = true) private Boolean requireMotherHasActiveVisit = false;  // restrict to mothers who have an active visit
    @Accessors(fluent = true) private Boolean requireChildHasActiveVisit = false;  // restrict to mothers of children who have an active visit
    @Accessors(fluent = true)  private boolean requireChildBornDuringMothersActiveVisit = false;  // restrict to mothers who had a child born during their active visit

}
