package org.openmrs.module.emrapi.encounter.builder;

import org.openmrs.module.emrapi.CareSettingType;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class TestOrderBuilder {
    private final EncounterTransaction.TestOrder testOrder;

    public TestOrderBuilder() {
        testOrder = new EncounterTransaction.TestOrder();
        testOrder.setCareSetting(CareSettingType.OUTPATIENT);
        withConceptUuid(UUID.randomUUID().toString());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        testOrder.setDateCreated(calendar.getTime());
        calendar.add(Calendar.MONTH, 1);
        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setUuid("331c6bf8-7846-11e3-a96a-0800271c1b75");
        testOrder.setAction("NEW");
    }

    public EncounterTransaction.TestOrder build() {
        return testOrder;
    }

    public TestOrderBuilder withConceptUuid(String conceptUuid) {
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept();
        concept.setUuid(conceptUuid);
        testOrder.setConcept(concept);
        return this;
    }

    public TestOrderBuilder withScheduledDate(Date createdDate) {
        testOrder.setDateCreated(createdDate);
        return this;
    }

    public TestOrderBuilder withAction(String action) {
        testOrder.setAction(action);
        return this;
    }
}
