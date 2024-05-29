package org.openmrs.module.emrapi.account;

import org.openmrs.OpenmrsMetadata;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.module.emrapi.account.provider.ProviderServiceFacade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides an abstraction over Provider services
 */
public class MockProviderServiceFacade implements ProviderServiceFacade {

    private final ProviderServiceFacade providerServiceFacade;
    private final List<Provider> providers = new ArrayList<>();

    public MockProviderServiceFacade(ProviderServiceFacade providerServiceFacade) {
        this.providerServiceFacade = providerServiceFacade;
    }

    @Override
    public Provider newProvider() {
        return providerServiceFacade.newProvider();
    }

    @Override
    public Collection<Provider> getProvidersByPerson(Person person) {
        List<Provider> ret = new ArrayList<>();
        for (Provider provider : providers) {
            if (provider.getPerson() != null && provider.getPerson().equals(person)) {
                ret.add(provider);
            }
        }
        return ret;
    }

    @Override
    public Provider saveProvider(Provider provider) {
        providers.add(provider);
        return provider;
    }

    @Override
    public Object getProviderRole(Provider provider) {
        return providerServiceFacade.getProviderRole(provider);
    }

    @Override
    public void setProviderRole(Provider provider, Object providerRole) {
        providerServiceFacade.setProviderRole(provider, providerRole);
    }
}
