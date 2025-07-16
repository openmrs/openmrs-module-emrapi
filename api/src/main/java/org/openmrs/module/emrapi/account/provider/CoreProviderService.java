package org.openmrs.module.emrapi.account.provider;

import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.ProviderRole;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

/**
 * Provides an abstraction over Provider services
 */
@OpenmrsProfile(modules = {"!providermanagement"})
public class CoreProviderService implements ProviderServiceFacade {

    private final ProviderService providerService;

    @Autowired
    public CoreProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }

    @Override
    public Provider newProvider() {
        return new Provider();
    }

    @Override
    public Collection<Provider> getProvidersByPerson(Person person) {
        return providerService.getProvidersByPerson(person, false);
    }

    @Override
    public Provider saveProvider(Provider provider) {
        return providerService.saveProvider(provider);
    }

    @Override
    public Object getProviderRole(Provider provider) {
        return provider.getProviderRole();
    }

    @Override
    public void setProviderRole(Provider provider, ProviderRole providerRole) {
        provider.setProviderRole(providerRole);
    }
}
