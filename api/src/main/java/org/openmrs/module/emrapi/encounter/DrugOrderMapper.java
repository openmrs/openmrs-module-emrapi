package org.openmrs.module.emrapi.encounter;

import org.openmrs.DrugOrder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public interface DrugOrderMapper {
    EncounterTransaction.DrugOrder map(DrugOrder drugOrder);
}
