package org.openmrs.module.emrapi.account;

import org.apache.commons.lang.BooleanUtils;
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
import java.util.LinkedHashMap;
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

    @Override
    public List<AccountDomainWrapper> getAccounts(AccountSearchCriteria criteria) {
        Map<Person, AccountDomainWrapper> byPerson = new LinkedHashMap<>();
        List<User> users;
        List<Provider> providers;
        if (StringUtils.isNotBlank(criteria.getNameOrIdentifier())) {
            Map<String, Object> searchParams = new HashMap<>();
            searchParams.put("search", "%" + criteria.getNameOrIdentifier() + "%");
            users = emrApiDAO.executeHqlFromResource("hql/user_search.hql", searchParams, User.class);
            providers = emrApiDAO.executeHqlFromResource("hql/provider_search.hql", searchParams, Provider.class);
        }
        else {
            users = userService.getAllUsers();
            providers = providerService.getAllProviders();
        }

        Set<Person> userPersons = new HashSet<>();
        Set<Person> providerPersons = new HashSet<>();

        for (User user : users) {
            //exclude daemon user
            if (EmrApiConstants.DAEMON_USER_UUID.equals(user.getUuid())) {
                continue;
            }
            Person person = user.getPerson();
            if (BooleanUtils.isNotTrue(person.getPersonVoided())) {
                byPerson.put(person, domainWrapperFactory.newAccountDomainWrapper(person));
                userPersons.add(person);
            }
        }

        for (Provider provider : providers) {

            // skip the baked-in unknown provider
            if (provider.equals(emrApiProperties.getUnknownProvider())) {
                continue;
            }

            Person person = provider.getPerson();

            if (person == null || BooleanUtils.isTrue(person.getPersonVoided())) {
                log.warn("Providers not associated to a person are not supported");
                continue;
            }

            // filter based on role if provided
            ProviderRole providerRole = provider.getProviderRole();
            if (criteria.getProviderRoles() != null && !criteria.getProviderRoles().isEmpty()) {
                if (providerRole == null || !criteria.getProviderRoles().contains(providerRole)) {
                    continue;
                }
            }

            if (!byPerson.containsKey(person)) {
                byPerson.put(person, domainWrapperFactory.newAccountDomainWrapper(person));
            }
            providerPersons.add(person);
        }

        if (criteria.getHasUser() == Boolean.TRUE) {
            byPerson.keySet().retainAll(userPersons);
        }
        else if (criteria.getHasUser() == Boolean.FALSE) {
            byPerson.keySet().removeAll(userPersons);
        }

        if (criteria.getHasProvider() == Boolean.TRUE) {
            byPerson.keySet().retainAll(providerPersons);
        }
        else if (criteria.getHasProvider() == Boolean.FALSE) {
            byPerson.keySet().removeAll(providerPersons);
        }

        return new ArrayList<>(byPerson.values());
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
