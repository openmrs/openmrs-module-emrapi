package org.openmrs.module.emrapi.encounter.builder;

import org.openmrs.module.emrapi.CareSettingType;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class OrderBuilder {
    private final EncounterTransaction.Order order;

    public OrderBuilder() {
        order = new EncounterTransaction.Order();
        order.setCareSetting(CareSettingType.OUTPATIENT);
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

    public OrderBuilder withComment(String comment) {
        order.setCommentToFulfiller(comment);
        return this;
    }

    public OrderBuilder withAction(String action) {
        order.setAction(action);
        return this;
    }

    public OrderBuilder withUuid(String orderUuid) {
        order.setUuid(orderUuid);
        return this;
    }

    public OrderBuilder withConcept(EncounterTransaction.Concept concept) {
        order.setConcept(concept);
        return this;
    }

    public OrderBuilder withPreviousOrderUuid(String previousOrderUuid) {
        order.setPreviousOrderUuid(previousOrderUuid);
        return this;
    }

    public OrderBuilder withDateCreated(Date createdDate) {
        order.setDateCreated(createdDate);
        return this;
    }

    public OrderBuilder withOrderGroup(String orderSetUuid) {
        EncounterTransaction.OrderSet orderSet = new EncounterTransaction.OrderSet();
        orderSet.setUuid(orderSetUuid);

        EncounterTransaction.OrderGroup orderGroup = new EncounterTransaction.OrderGroup();
        orderGroup.setOrderSet(orderSet);
        order.setOrderGroup(orderGroup);
        return this;
    }
}
