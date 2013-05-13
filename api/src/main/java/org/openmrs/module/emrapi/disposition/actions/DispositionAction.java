package org.openmrs.module.emrapi.disposition.actions;

import org.openmrs.Obs;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;

import java.util.Map;

public interface DispositionAction {

    /**
     * Actions will be called before the framework saves the encounter, but within the same transaction.
     * A typical implementation would add an additional observation to the obs group representing the disposition.
     *
     * @param encounterDomainWrapper encounter that is being created (has not had dispositionObsGroupBeingCreated added yet)
     * @param dispositionObsGroupBeingCreated the obs group being created for this disposition (has not been added to the encounter yet)
     * @param requestParameters parameters submitted with the HTTP request, which may contain additional data neede by this action
     */
    void action(EncounterDomainWrapper encounterDomainWrapper, Obs dispositionObsGroupBeingCreated, Map<String, String[]> requestParameters);

}
