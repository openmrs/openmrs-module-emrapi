package org.openmrs.module.emrapi.encounter;

import org.openmrs.DrugOrder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.stereotype.Component;

@Component (value = "drugOrderMapper")
public class MockDrugOrderMapper implements DrugOrderMapper{
    @Override
    public EncounterTransaction.DrugOrder map(DrugOrder drugOrder) {
        return new EncounterTransaction.DrugOrder();
    }
}
