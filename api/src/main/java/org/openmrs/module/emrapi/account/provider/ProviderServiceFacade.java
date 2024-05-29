package org.openmrs.module.emrapi.account.provider;

import org.openmrs.OpenmrsMetadata;
import org.openmrs.Person;
import org.openmrs.Provider;

import java.util.Collection;

/**
 * Provides an abstraction over Provider services
 */
public interface ProviderServiceFacade {

    Provider newProvider();

    Collection<? extends Provider> getProvidersByPerson(Person person);

    Provider saveProvider(Provider provider);

    Object getProviderRole(Provider provider);

    void setProviderRole(Provider provider, Object providerRole);
}
