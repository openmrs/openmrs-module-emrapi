package org.openmrs.module.emrapi.test;

import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.TestOrder;
import org.openmrs.module.emrapi.encounter.OrderMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component(value = "orderMapper")
public class MockOrderMapper implements OrderMapper {

    @Override
    public List<EncounterTransaction.DrugOrder> mapDrugOrders(Encounter encounter) {
        return new ArrayList<EncounterTransaction.DrugOrder>();
    }

    @Override
    public List<EncounterTransaction.TestOrder> mapTestOrders(Encounter encounter) {
        return new ArrayList<EncounterTransaction.TestOrder>();
    }

    @Override
    public EncounterTransaction.DrugOrder mapDrugOrder(DrugOrder openMRSDrugOrder) {
        return null;
    }

    @Override
    public EncounterTransaction.TestOrder mapTestOrder(TestOrder order) {
        return null;
    }
}
