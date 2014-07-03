package org.openmrs.module.emrapi.encounter;

import org.openmrs.Encounter;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component (value = "encounterDrugOrderServiceHelper")
public class MockEncounterDrugOrderServiceHelper implements EncounterDrugOrderServiceHelper{
    @Override
    public void update(Encounter encounter, List<EncounterTransaction.DrugOrder> drugOrders) {

    }
}
