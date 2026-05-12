package org.openmrs.module.emrapi.account;

import org.openmrs.Person;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.annotation.Authorized;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public interface AccountService {

    /**
     * @return
     * @should get all unique accounts
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    List<AccountDomainWrapper> getAllAccounts();

    /**
     * @deprecated use getAccountsByCriteria(AccountSearchCriteria)
     * @should get all unique accounts that match the given criteria
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    List<AccountDomainWrapper> getAccounts(AccountSearchCriteria criteria);

    /**
     * @return accounts that match the given search criteria and limit options
     */
    AccountSearchResult getAccountsByCriteria(AccountSearchCriteria criteria);

    /**
     * Save the account details to the database
     *
     * @param account
     * @return
     */
    @Transactional
    @Authorized(PrivilegeConstants.EDIT_USERS)
    void saveAccount(AccountDomainWrapper account);

    /**
     * Gets an account for the person with the specified personId
     *
     * @return
     * @should return the account for the person with the specified personId
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    AccountDomainWrapper getAccount(Integer personId);

    /**
     * Gets an account for the Specified person object
     *
     * @return
     * @should return the account for the specified person if they are associated to a user
     * @should return the account for the specified person if they are associated to a provider
     */
    @Authorized(PrivilegeConstants.GET_USERS)
    AccountDomainWrapper getAccountByPerson(Person person);

    /**
     * Gets all Capabilities, i.e roles with the {@link org.openmrs.module.emrapi.EmrApiConstants#ROLE_PREFIX_CAPABILITY} prefix
     *
     * @return a list of Roles
     * @should return all roles with the capability prefix
     */
    @Authorized(PrivilegeConstants.GET_ROLES)
    List<Role> getAllCapabilities();

    /**
     * Gets all Privilege Levels, i.e roles with the
     * {@link org.openmrs.module.emrapi.EmrApiConstants#ROLE_PREFIX_PRIVILEGE_LEVEL} prefix
     *
     * @return a list of Roles
     * @should return all roles with the privilege level prefix
     */
    @Authorized(PrivilegeConstants.GET_ROLES)
    List<Role> getAllPrivilegeLevels();

    /**
     * By convention, anything not defined as #getApplicationPrivileges() is an API-level privilege
     *
     * @return all privileges that represent API-level actions
     */
    @Authorized(PrivilegeConstants.GET_PRIVILEGES)
    List<Privilege> getApiPrivileges();

    /**
     * By convention, privileges starting with "App:" or "Task:" are Application-level
     *
     * @return all privileges that represent Application-level actions
     */
    @Authorized(PrivilegeConstants.GET_PRIVILEGES)
    List<Privilege> getApplicationPrivileges();

}
