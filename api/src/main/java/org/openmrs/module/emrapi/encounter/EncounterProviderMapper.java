package org.openmrs.module.emrapi.encounter;

import org.openmrs.EncounterProvider;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.HashSet;
import java.util.Set;

public class EncounterProviderMapper {
    public void update(EncounterTransaction encounterTransaction, Set<EncounterProvider> encounterProviders) {
        Set<EncounterTransaction.Provider> providers = convert(encounterProviders);
        encounterTransaction.setProviders(providers);
    }

    public Set<EncounterTransaction.Provider> convert(Set<EncounterProvider> encounterProviders) {
        Set<EncounterTransaction.Provider> providers = new HashSet<EncounterTransaction.Provider>();
        for(EncounterProvider encounterProvider : encounterProviders){
            EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
            provider.setName(encounterProvider.getProvider().getName());
            provider.setUuid(encounterProvider.getProvider().getUuid());
            if(encounterProvider.getEncounterRole() != null) {
                provider.setEncounterRoleUuid(encounterProvider.getEncounterRole().getUuid());
            }
            providers.add(provider);
        }
        return providers;
    }

    public static class EmptyEncounterProviderMapper extends EncounterProviderMapper {

        public void update(EncounterTransaction encounterTransaction, Set<EncounterProvider> encounterProviders) {
            //do Nothing
        }
    }

}
