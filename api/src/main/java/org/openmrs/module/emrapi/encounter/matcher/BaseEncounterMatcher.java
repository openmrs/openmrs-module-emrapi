package org.openmrs.module.emrapi.encounter.matcher;


import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.encounter.EncounterParameters;

/**
 * Chooses a suitable encounter from a {@link org.openmrs.Visit} using various criteria.
 */
public interface BaseEncounterMatcher {

    Encounter findEncounter(Visit visit, EncounterParameters encounterParameters);

}
