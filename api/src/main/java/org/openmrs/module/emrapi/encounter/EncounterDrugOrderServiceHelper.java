package org.openmrs.module.emrapi.encounter;

import org.openmrs.Encounter;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.List;

public interface EncounterDrugOrderServiceHelper {
    void update(Encounter encounter, List<EncounterTransaction.DrugOrder> drugOrders);
}
