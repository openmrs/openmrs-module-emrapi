package org.openmrs.module.emrapi.db;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;

import java.util.List;

/**
 * Useful queries for fetching OpenMRS objects beyond what are provided by the OpenMRS Core
 */
public interface EmrEncounterDAO {

    /**
     * Returns all encounters that have an obs with the specified value text
     *
     * @param obsConcept the concept associated with the obs
     * @param valueText the value text of the obs
     * @param encounterType optionally limit to encounters of a certain type
     * @param includeVoided whether or not to include voided obs
     * @return
     */
    List<Encounter> getEncountersByObsValueText(Concept obsConcept, String valueText, EncounterType encounterType, boolean includeVoided);

}
