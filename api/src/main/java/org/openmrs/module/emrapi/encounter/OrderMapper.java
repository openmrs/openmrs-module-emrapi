package org.openmrs.module.emrapi.encounter;

import java.util.List;

import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public interface OrderMapper {
    public List<EncounterTransaction.DrugOrder> mapDrugOrders(Encounter encounter);

    public List<EncounterTransaction.Order> mapOrders(Encounter encounter);

    EncounterTransaction.DrugOrder mapDrugOrder(DrugOrder openMRSDrugOrder);

    EncounterTransaction.Order mapOrder(Order order);
}
