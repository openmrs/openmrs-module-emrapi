package org.openmrs.module.emrapi.encounter.postprocessor;

import org.openmrs.Encounter;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public interface EncounterTransactionHandler {
    void forRead(Encounter encounter, EncounterTransaction encounterTransaction);
    void forSave(Encounter encounter, EncounterTransaction encounterTransaction);
}
