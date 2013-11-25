package org.openmrs.module.emrapi.encounter;

import org.openmrs.EncounterProvider;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.HashSet;
import java.util.Set;

public class EncounterProviderMapper {
    public void update(EncounterTransaction encounterTransaction, Set<EncounterProvider> encounterProviders) {
        Set<EncounterTransaction.Provider> providers = new HashSet<EncounterTransaction.Provider>();
        for(EncounterProvider encounterProvider : encounterProviders){
            EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
            provider.setName(encounterProvider.getProvider().getName());
            provider.setUuid(encounterProvider.getProvider().getUuid());
            providers.add(provider);
        }
        encounterTransaction.setProviders(providers);
    }
}