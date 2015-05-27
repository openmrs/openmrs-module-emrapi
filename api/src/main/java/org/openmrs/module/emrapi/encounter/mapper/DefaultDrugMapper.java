package org.openmrs.module.emrapi.encounter.mapper;

import org.openmrs.Drug;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.module.emrapi.encounter.DrugMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.stereotype.Component;

@Component("drugMapper")
@OpenmrsProfile(openmrsVersion = "1.9.*")
public class DefaultDrugMapper implements DrugMapper {
    @Override
    public EncounterTransaction.Drug map(Drug drug) {
        return null;
    }
}
