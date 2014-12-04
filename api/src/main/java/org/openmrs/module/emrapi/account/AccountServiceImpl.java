package org.openmrs.module.emrapi.account;

import org.openmrs.Person;
import org.openmrs.Privilege;
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.APIException;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.openmrs.module.providermanagement.api.ProviderManagementService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Transactional
public class AccountServiceImpl extends BaseOpenmrsService implements AccountService {

    private UserService userService;

    private PersonService personService;

    private ProviderService providerService;

    private ProviderManagementService providerManagementService;

    private DomainWrapperFactory domainWrapperFactory;

    private EmrApiProperties emrApiProperties;

    /**
     * @param userService the userService to set
     */
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    /**
     * @param providerService
     */
    public void setProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }

    /**
     * @param providerManagementService
     */
    public void setProviderManagementService(ProviderManagementService providerManagementService) {
        this.providerManagementService = providerManagementService;
    }

    /**
     * @param personService the personService to set
     */
    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setDomainWrapperFactory(DomainWrapperFactory domainWrapperFactory) {
        this.domainWrapperFactory = domainWrapperFactory;
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    /**
     * @see org.openmrs.module.emrapi.account.AccountService#getAllAccounts()
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountDomainWrapper> getAllAccounts() {

        Map<Person, AccountDomainWrapper> byPerson = new LinkedHashMap<Person, AccountDomainWrapper>();

        for (User user : userService.getAllUsers()) {
            //exclude daemon user
            if (EmrApiConstants.DAEMON_USER_UUID.equals(user.getUuid()))
                continue;

            if (!user.getPerson().isPersonVoided()) {
                byPerson.put(user.getPerson(), domainWrapperFactory.newAccountDomainWrapper(user.getPerson()));
            }
        }

        for (Provider provider : providerService.getAllProviders()) {

            // skip the baked-in unknown provider
            if (provider.equals(emrApiProperties.getUnknownProvider())) {
                continue;
            }

            if (provider.getPerson() == null)
                throw new APIException("Providers not associated to a person are not supported");

            AccountDomainWrapper account = byPerson.get(provider.getPerson());
            if (account == null && !provider.getPerson().isPersonVoided()) {
                byPerson.put(provider.getPerson(), domainWrapperFactory.newAccountDomainWrapper(provider.getPerson()));
            }
        }

        List<AccountDomainWrapper> accounts = new ArrayList<AccountDomainWrapper>();
        for (AccountDomainWrapper account : byPerson.values()) {
            accounts.add(account);
        }

        return accounts;
    }

    /**
     * @see org.openmrs.module.emrapi.account.AccountService#saveAccount(org.openmrs.module.emrapi.account.AccountDomainWrapper)
     */
    @Override
    @Transactional
    public void saveAccount(AccountDomainWrapper account) {
        account.save();
    }

    /**
     * @see org.openmrs.module.emrapi.account.AccountService#getAccount(java.lang.Integer)
     */
    @Override
    @Transactional(readOnly = true)
    public AccountDomainWrapper getAccount(Integer personId) {
        return getAccountByPerson(personService.getPerson(personId));
    }

    /**
     * @see org.openmrs.module.emrapi.account.AccountService#getAccountByPerson(org.openmrs.Person)
     */
    @Override
    @Transactional(readOnly = true)
    public AccountDomainWrapper getAccountByPerson(Person person) {
        return domainWrapperFactory.newAccountDomainWrapper(person);
    }

    /**
     * @see org.openmrs.module.emrapi.account.AccountService#getAllCapabilities()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllCapabilities() {
        List<Role> capabilities = new ArrayList<Role>();
        for (Role candidate : userService.getAllRoles()) {
            if (candidate.getName().startsWith(EmrApiConstants.ROLE_PREFIX_CAPABILITY))
                capabilities.add(candidate);
        }
        return capabilities;
    }

    /**
     * @see org.openmrs.module.emrapi.account.AccountService#getAllPrivilegeLevels()
     */
    @Override
    @Transactional(readOnly = true)
    public List<Role> getAllPrivilegeLevels() {
        List<Role> privilegeLevels = new ArrayList<Role>();
        for (Role candidate : userService.getAllRoles()) {
            if (candidate.getName().startsWith(EmrApiConstants.ROLE_PREFIX_PRIVILEGE_LEVEL))
                privilegeLevels.add(candidate);
        }
        return privilegeLevels;
    }

    @Override
    public List<Privilege> getApiPrivileges() {
        List<Privilege> privileges = new ArrayList<Privilege>();
        for (Privilege candidate : userService.getAllPrivileges()) {
            if (!isApplicationPrivilege(candidate)) {
                privileges.add(candidate);
            }
        }
        return privileges;
    }

    @Override
    public List<Privilege> getApplicationPrivileges() {
        List<Privilege> privileges = new ArrayList<Privilege>();
        for (Privilege candidate : userService.getAllPrivileges()) {
            if (isApplicationPrivilege(candidate)) {
                privileges.add(candidate);
            }
        }
        return privileges;
    }

    private boolean isApplicationPrivilege(Privilege privilege) {
        return privilege.getPrivilege().startsWith(EmrApiConstants.PRIVILEGE_PREFIX_APP)
                || privilege.getPrivilege().startsWith(EmrApiConstants.PRIVILEGE_PREFIX_TASK);
    }


}
