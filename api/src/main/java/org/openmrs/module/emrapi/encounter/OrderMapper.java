package org.openmrs.module.emrapi.encounter;

import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface OrderMapper {
    public List<EncounterTransaction.DrugOrder> mapDrugOrders(Encounter encounter);

    public List<EncounterTransaction.Order> mapOrders(Encounter encounter);

    EncounterTransaction.DrugOrder mapDrugOrder(DrugOrder openMRSDrugOrder);

    EncounterTransaction.Order mapOrder(Order order);
}
