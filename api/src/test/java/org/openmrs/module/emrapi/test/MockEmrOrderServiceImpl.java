package org.openmrs.module.emrapi.test;

import org.openmrs.Encounter;
import org.openmrs.module.emrapi.encounter.EmrOrderService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class MockEmrOrderServiceImpl implements EmrOrderService {

    @Override
    public void save(List<EncounterTransaction.DrugOrder> drugOrders, Encounter encounter) {
    }

    @Override
    public List<EncounterTransaction.DrugOrder> getDrugOrders(Encounter encounter) {
        return new ArrayList<EncounterTransaction.DrugOrder>();
    }
}
