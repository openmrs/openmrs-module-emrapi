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
            if (isProviderRoleNull(providerRole)) {
                p.setProviderRole(null);
            }
            else {
                ProviderRole role = null;
                if (providerRole instanceof ProviderRole) {
                    role = (ProviderRole) providerRole;
                }
                else if (providerRole instanceof String) {
                    role = getProviderRole((String) providerRole);
                }
                else if (providerRole instanceof Integer) {
                    role = providerManagementService.getProviderRole((Integer) providerRole);
                }
                else if (providerRole instanceof String[]) {
                    String[] roles = (String[]) providerRole;
                    if (roles.length > 1) {
                        throw new IllegalArgumentException("Only one provider role may be provided");
                    }
                    role = getProviderRole(roles[0]);
                }
                if (role == null) {
                    throw new IllegalArgumentException("Unable to set provider role from " + providerRole);
                }
                ((org.openmrs.module.providermanagement.Provider) provider).setProviderRole(role);
            }
        }
    }

    private ProviderRole getProviderRole(String ref) {
        ProviderRole role = providerManagementService.getProviderRoleByUuid(ref);
        if (role == null) {
            try {
                Integer roleId = Integer.parseInt(ref);
                role = providerManagementService.getProviderRole(roleId);
            }
            catch (Exception ignored) {
            }
        }
        return role;
    }

    private boolean isProviderRoleNull(Object ref) {
        if (ref == null) {
            return true;
        }
        if (ref instanceof String && ((String) ref).isEmpty()) {
            return true;
        }
        if (ref instanceof String[] && (((String[]) ref).length == 0 || ((String[])ref)[0].isEmpty())) {
            return true;
        }
        return false;
    }
}
