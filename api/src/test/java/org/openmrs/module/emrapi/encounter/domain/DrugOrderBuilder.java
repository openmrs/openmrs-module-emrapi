package org.openmrs.module.emrapi.encounter.domain;

import java.util.Date;

import static org.openmrs.module.emrapi.encounter.domain.EncounterTransaction.DrugOrder;

public class DrugOrderBuilder {

    DrugOrder drugOrder = new DrugOrder();

    public DrugOrder build(){
        return this.drugOrder;
    }

    public DrugOrderBuilder withBasicValues(String uuid, String conceptUuId, Date startDate, Date endDate,
                                            Integer numberPerDosage,
                                            String dosageInstructionUuid,
                                            String dosageFrequencyUuId){
        this.drugOrder.setUuid(uuid);
        this.drugOrder.setConceptUuid(conceptUuId);
        this.drugOrder.setStartDate(startDate);
        this.drugOrder.setEndDate(endDate);
        this.drugOrder.setNumberPerDosage(numberPerDosage);
        this.drugOrder.setDosageInstructionUuid(dosageInstructionUuid);
        this.drugOrder.setDosageFrequencyUuid(dosageFrequencyUuId);
        this.drugOrder.setPrn(false);
        return this;
    }


    public DrugOrderBuilder withNotes(String notes) {
        this.drugOrder.setNotes(notes);
        return this;
    }

    public DrugOrderBuilder withPrn(boolean prn) {
        this.drugOrder.setPrn(prn);
        return this;
    }

    public DrugOrderBuilder withDosageFrequency(String frequencyUuid) {
        this.drugOrder.setDosageFrequencyUuid(frequencyUuid);
        return this;
    }
}
