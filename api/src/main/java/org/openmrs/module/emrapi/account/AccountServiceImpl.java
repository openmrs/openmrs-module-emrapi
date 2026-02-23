package org.openmrs.module.emrapi.account;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Person;
import org.openmrs.Privilege;
import org.openmrs.Provider;
import org.openmrs.ProviderRole;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.db.EmrApiDAO;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Transactional
public class AccountServiceImpl extends BaseOpenmrsService implements AccountService {

    private Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);

    private UserService userService;

    private PersonService personService;

    private ProviderService providerService;

    private DomainWrapperFactory domainWrapperFactory;

    private EmrApiProperties emrApiProperties;

    private EmrApiDAO emrApiDAO;

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

    public void setEmrApiDAO(EmrApiDAO emrApiDAO) {
        this.emrApiDAO = emrApiDAO;
    }

    /**
     * @see org.openmrs.module.emrapi.account.AccountService#getAllAccounts()
     */
    @Override
    @Transactional(readOnly = true)
    public List<AccountDomainWrapper> getAllAccounts() {
        return getAccounts(new AccountSearchCriteria());
    }

    /**
     * @see org.openmrs.module.emrapi.account.AccountService#getAccountsByCriteria(AccountSearchCriteria)
     */
    public AccountSearchResult getAccountsByCriteria(AccountSearchCriteria criteria) {
        Set<Person> persons = new HashSet<>();
        List<User> users;
        List<Provider> providers;
        log.debug("Retreiving accounts: " + criteria);
        if (StringUtils.isNotBlank(criteria.getNameOrIdentifier())) {
            Map<String, Object> searchParams = new HashMap<>();
            searchParams.put("search", "%" + criteria.getNameOrIdentifier() + "%");
            users = emrApiDAO.executeHqlFromResource("hql/user_search.hql", searchParams, User.class);
            log.debug("Retrieved {} users with search: {}", users.size(), criteria.getNameOrIdentifier());
            providers = emrApiDAO.executeHqlFromResource("hql/provider_search.hql", searchParams, Provider.class);
            log.debug("Retrieved {} providers with search {}", providers.size(), criteria.getNameOrIdentifier());
        }
        else {
            users = userService.getAllUsers();
            log.debug("Retrieved {} users", users.size());
            providers = providerService.getAllProviders();
            log.debug("Retrieved {} providers", providers.size());
        }

        // Get the base set of persons
        for (User user : users) {
            if (user.getPerson() == null) {
                log.warn("Users not associated to a person are not supported.  Excluding {}", user.getUuid());
            }
            else {
                persons.add(user.getPerson());
            }
        }
        log.debug("Mapped users to AccountDomainWrappers.  Total accounts: {}", persons.size());

        for (Provider provider : providers) {
            if (provider.getPerson() == null) {
                log.warn("Providers not associated to a person are not supported.  Excluding {}", provider.getUuid());
            }
            else {
                persons.add(provider.getPerson());
            }
        }
        log.debug("Mapped providers to AccountDomainWrappers.  Total accounts: {}", persons.size());

        // Exclude persons based on user search criteria
        for (User user : users) {
            //exclude daemon user
            if (EmrApiConstants.DAEMON_USER_UUID.equals(user.getUuid())) {
                persons.remove(user.getPerson());
            }
            else if (criteria.getUserEnabled() == Boolean.TRUE && user.isRetired()) {
                persons.remove(user.getPerson());
            }
            else if (criteria.getUserEnabled() == Boolean.FALSE && !user.isRetired()) {
                persons.remove(user.getPerson());
            }
        }
        log.debug("Excluded users based on search criteria.  Total accounts: {}", persons.size());

        // Exclude persons based on provider search criteria
        Provider unknownProvider = emrApiProperties.getUnknownProvider();
        for (Provider provider : providers) {
            // skip the baked-in unknown provider
            if (provider.equals(unknownProvider)) {
                persons.remove(provider.getPerson());
            }
            if (criteria.getHasProviderRole() == Boolean.TRUE && provider.getProviderRole() == null) {
                persons.remove(provider.getPerson());
            }
            if (criteria.getHasProviderRole() == Boolean.FALSE && provider.getProviderRole() != null) {
                persons.remove(provider.getPerson());
            }
            if (criteria.getProviderRoles() != null && !criteria.getProviderRoles().isEmpty()) {
                ProviderRole providerRole = provider.getProviderRole();
                if (providerRole == null || !criteria.getProviderRoles().contains(providerRole)) {
                    persons.remove(provider.getPerson());
                }
            }
        }
        log.debug("Excluded providers based on search criteria.  Total accounts: {}", persons.size());

        List<Person> sortedPersons = new ArrayList<>(persons);
        sortedPersons.sort((o1, o2) -> o1.getPersonName().getFullName().compareToIgnoreCase(o2.getPersonName().getFullName()));
        log.debug("Sorted persons based on name");

        long totalCount = persons.size();
        int startIndex = criteria.getStartIndex() == null ? 0 : criteria.getStartIndex();
        int endIndex = criteria.getLimit() == null ? sortedPersons.size() : Math.min(startIndex + criteria.getLimit(), persons.size());
        sortedPersons = sortedPersons.subList(startIndex, endIndex);
        log.debug("Limited results to those from {} to {}, total results: {}", startIndex, endIndex, sortedPersons.size());

        AccountSearchResult result = new AccountSearchResult();
        for (Person p : sortedPersons) {
            result.getAccounts().add(domainWrapperFactory.newAccountDomainWrapper(p));
        }
        result.setTotalCount(totalCount);
        log.debug("Returning {} accounts", result.getAccounts().size());
        return result;
    }

    @Override
    @Deprecated
    public List<AccountDomainWrapper> getAccounts(AccountSearchCriteria criteria) {
        return getAccountsByCriteria(criteria).getAccounts();
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
