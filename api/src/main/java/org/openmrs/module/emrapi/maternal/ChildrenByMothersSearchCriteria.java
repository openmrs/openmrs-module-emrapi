package org.openmrs.module.emrapi.maternal;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChildrenByMothersSearchCriteria {
    private List<String> motherUuids;  // restrict to children of these mothers
    @Accessors(fluent = true)  private boolean requireMotherHasActiveVisit = false;  // restrict to children of mothers who have an active visit
    @Accessors(fluent = true)  private boolean requireChildHasActiveVisit = false;   // restrict to children who have an active visit
    @Accessors(fluent = true)  private boolean requireChildBornDuringMothersActiveVisit = false;  // restrict to children who were born during their mother's active visit
}


