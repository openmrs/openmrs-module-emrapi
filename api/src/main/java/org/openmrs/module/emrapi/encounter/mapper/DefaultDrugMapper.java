package org.openmrs.module.emrapi.encounter.mapper;

import org.openmrs.Drug;
import org.openmrs.module.emrapi.encounter.DrugMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.stereotype.Component;

@Component("drugMapper")
public class DefaultDrugMapper implements DrugMapper {

    @Override
    public EncounterTransaction.Drug map(Drug drug){
        EncounterTransaction.Drug encounterTransactionDrug = new EncounterTransaction.Drug();
        encounterTransactionDrug.setName(drug.getDisplayName());
        if (drug.getDosageForm() != null) {
            encounterTransactionDrug.setForm(drug.getDosageForm().getName().getName());
        }
        encounterTransactionDrug.setStrength(drug.getStrength());
        encounterTransactionDrug.setUuid(drug.getUuid());
        return encounterTransactionDrug;
    }
}
