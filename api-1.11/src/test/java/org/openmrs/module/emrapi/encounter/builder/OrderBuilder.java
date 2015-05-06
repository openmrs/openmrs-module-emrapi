package org.openmrs.module.emrapi.encounter.builder;

import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class OrderBuilder {
    
    private final EncounterTransaction.Order order;

    public OrderBuilder() {
        order = new EncounterTransaction.Order();
        order.setCareSetting("OUTPATIENT");
        order.setOrderType("Lab Orders");
        withConceptUuid(UUID.randomUUID().toString());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        order.setDateCreated(calendar.getTime());
        calendar.add(Calendar.MONTH, 1);
        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setUuid("331c6bf8-7846-11e3-a96a-0800271c1b75");
        order.setAction("NEW");
    }

    public EncounterTransaction.Order build() {
        return order;
    }

    public OrderBuilder withConceptUuid(String conceptUuid) {
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept();
        concept.setUuid(conceptUuid);
        order.setConcept(concept);
        return this;
    }

    public OrderBuilder withScheduledDate(Date createdDate) {
        order.setDateCreated(createdDate);
        return this;
    }

    public OrderBuilder withAction(String action) {
        order.setAction(action);
        return this;
    }
    
}
