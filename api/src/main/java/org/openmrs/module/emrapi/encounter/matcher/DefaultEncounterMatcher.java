package org.openmrs.module.emrapi.encounter.matcher;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.encounter.EncounterParameters;

/**
 * Find {@link org.openmrs.Encounter} from a {@link org.openmrs.Visit} by matching {@link org.openmrs.EncounterType}.
 */
public class DefaultEncounterMatcher implements BaseEncounterMatcher {

    @Override
    public Encounter findEncounter(Visit visit, EncounterParameters encounterParameters) {
        EncounterType encounterType = encounterParameters.getEncounterType();

        if (encounterType == null){
            throw new IllegalArgumentException("Encounter Type not found");
        }

        for (Encounter encounter : visit.getEncounters()) {
            if (encounterType.equals(encounter.getEncounterType())) {
                return encounter;
            }
        }
        return null;
    }
}
