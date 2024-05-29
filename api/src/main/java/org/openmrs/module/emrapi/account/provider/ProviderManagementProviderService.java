package org.openmrs.module.emrapi.account.provider;

import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.ProviderService;
import org.openmrs.module.providermanagement.ProviderRole;
import org.openmrs.module.providermanagement.api.ProviderManagementService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

/**
 * Provides an abstraction over Provider services
 */
@OpenmrsProfile(modules = {"providermanagement:*"})
public class ProviderManagementProviderService implements ProviderServiceFacade {

    private final ProviderService providerService;

    private final ProviderManagementService providerManagementService;

    @Autowired
    public ProviderManagementProviderService(ProviderService providerService, ProviderManagementService providerManagementService) {
        this.providerService = providerService;
        this.providerManagementService = providerManagementService;
    }

    @Override
    public Provider newProvider() {
        return new org.openmrs.module.providermanagement.Provider();
    }

    @Override
    public Collection<org.openmrs.module.providermanagement.Provider> getProvidersByPerson(Person person) {
        return providerManagementService.getProvidersByPerson(person, false);
    }

    @Override
    public Provider saveProvider(Provider provider) {
        return providerService.saveProvider(provider);
    }

    @Override
    public Object getProviderRole(Provider provider) {
        if (provider instanceof org.openmrs.module.providermanagement.Provider) {
            return ((org.openmrs.module.providermanagement.Provider)provider).getProviderRole();
        }
        return null;
    }

    @Override
    public void setProviderRole(Provider provider, Object providerRole) {
        if (provider instanceof org.openmrs.module.providermanagement.Provider) {
            org.openmrs.module.providermanagement.Provider p = (org.openmrs.module.providermanagement.Provider) provider;
            if (providerRole == null) {
                p.setProviderRole(null);
            }
            else {
                if (providerRole instanceof ProviderRole) {
                    p.setProviderRole((ProviderRole) providerRole);
                }
                else if (providerRole instanceof String) {
                    p.setProviderRole(providerManagementService.getProviderRoleByUuid((String) providerRole));
                }
                else if (providerRole instanceof Integer) {
                    p.setProviderRole(providerManagementService.getProviderRole((Integer) providerRole));
                }
                else if (providerRole instanceof String[]) {
                    String[] uuids = (String[]) providerRole;
                    if (uuids.length > 1) {
                        throw new IllegalArgumentException("Only one provider role may be provided");
                    }
                    p.setProviderRole(providerManagementService.getProviderRoleByUuid(uuids[0]));
                }
                else {
                    throw new IllegalArgumentException("Unable to set provider role from " + providerRole);
                }
            }
        }
    }
}
