package org.openmrs.module.emrapi.account;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.Person;
import org.openmrs.Privilege;
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.TestUtils;
import org.openmrs.module.providermanagement.api.ProviderManagementService;
import org.openmrs.util.OpenmrsConstants;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountServiceTest {

    private AccountServiceImpl accountService;

    private UserService userService;

    private PersonService personService;

    private ProviderService providerService;

    private ProviderManagementService providerManagementService;

    @Before
    public void setup() {
        userService = mock(UserService.class);
        personService = mock(PersonService.class);
        providerService = mock(ProviderService.class);
        providerManagementService = mock(ProviderManagementService.class);

        accountService = new AccountServiceImpl();
        accountService.setUserService(userService);
        accountService.setPersonService(personService);
        accountService.setProviderService(providerService);
        accountService.setProviderManagementService(providerManagementService);
    }

    /**
     * @verifies get all unique accounts
     * @see AccountService#getAllAccounts()
     */
    @Test
    public void getAllAccounts_shouldGetAllUniqueAccounts() throws Exception {
        Person person1 = new Person();
        User user1 = new User();
        user1.setPerson(person1);
        User user2 = new User();
        Person person2 = new Person();
        user2.setPerson(person2);
        User daemonUser = new User();
        daemonUser.setUuid(EmrApiConstants.DAEMON_USER_UUID);
        Person daemonPerson = new Person();
        daemonUser.setPerson(daemonPerson);

        Provider provider1 = new Provider();
        provider1.setPerson(person1);//duplicate
        Provider provider2 = new Provider();
        Person person3 = new Person();
        provider2.setPerson(person3);

        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2, daemonUser));
        when(providerService.getAllProviders()).thenReturn(Arrays.asList(provider1, provider2));

        List<AccountDomainWrapper> accounts = accountService.getAllAccounts();
        Assert.assertEquals(3, accounts.size());
        assertThat(accounts,
                TestUtils.isCollectionOfExactlyElementsWithProperties("person", person1, person2, person3));
    }

    /**
     * @verifies return the account for the person with the specified personId
     * @see AccountService#getAccount(Integer)
     */
    @Test
    public void getAccount_shouldReturnTheAccountForThePersonWithTheSpecifiedPersonId() throws Exception {
        final Integer personId = 1;
        Person person = new Person(personId);
        person.setPersonId(1);

        final String username = "tester";
        User user = new User();
        user.setPerson(person);
        user.setUsername(username);
        user.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE, "ht");

        Role fullPrivilegeLevel = new Role();
        fullPrivilegeLevel.setRole(EmrApiConstants.ROLE_PREFIX_PRIVILEGE_LEVEL + "Full");
        user.addRole(fullPrivilegeLevel);

        PersonService personService = Mockito.mock(PersonService.class);
        accountService.setPersonService(personService);
        when(personService.getPerson(argThat(TestUtils.equalsMatcher(personId)))).thenReturn(person);
        when(userService.getUsersByPerson(argThat(TestUtils.equalsMatcher(person)), any(Boolean.class))).thenReturn(
                Arrays.asList(user));

        AccountDomainWrapper account = accountService.getAccount(personId);
        Assert.assertNotNull(account);
        Assert.assertEquals(person, account.getPerson());
        Assert.assertEquals(username, account.getUsername());
        Assert.assertEquals("ht", account.getDefaultLocale().toString());
        Assert.assertEquals(fullPrivilegeLevel, account.getPrivilegeLevel());
    }

    /**
     * @verifies return the account for the specified person if they are associated to a user
     * @see AccountService#getAccountByPerson(Person)
     */
    @Test
    public void getAccountByPerson_shouldReturnTheAccountForTheSpecifiedPersonIfTheyAreAssociatedToAUser() throws Exception {
        User user = new User();
        Person person = new Person();
        person.setPersonId(1);
        user.setPerson(person);
        when(userService.getUsersByPerson(argThat(TestUtils.equalsMatcher(person)), any(Boolean.class))).thenReturn(
                Arrays.asList(user));
        AccountDomainWrapper account = accountService.getAccountByPerson(person);
        Assert.assertNotNull(account);
        Assert.assertEquals(person, account.getPerson());
    }

    /**
     * @verifies return the account for the specified person if they are associated to a provider
     * @see AccountService#getAccountByPerson(Person)
     */
    @Test
    public void getAccountByPerson_shouldReturnTheAccountForTheSpecifiedPersonIfTheyAreAssociatedToAProvider()
            throws Exception {
        Person person = new Person();
        person.setPersonId(1);
        Provider provider = new Provider();
        provider.setPerson(person);
        when(providerService.getProvidersByPerson(argThat(TestUtils.equalsMatcher(person)), any(Boolean.class))).thenReturn(
                Arrays.asList(provider));
        AccountDomainWrapper account = accountService.getAccountByPerson(person);
        Assert.assertNotNull(account);
        Assert.assertEquals(person, account.getPerson());
    }

    /**
     * @verifies return all roles with the capability prefix
     * @see AccountService#getAllCapabilities()
     */
    @Test
    public void getAllCapabilities_shouldReturnAllRolesWithTheCapabilityPrefix() throws Exception {
        Role role1 = new Role(EmrApiConstants.ROLE_PREFIX_CAPABILITY + "role1");
        Role role3 = new Role("role2");
        Role role2 = new Role(EmrApiConstants.ROLE_PREFIX_CAPABILITY + "role3");

        when(userService.getAllRoles()).thenReturn(Arrays.asList(role1, role2, role3));
        List<Role> capabilities = accountService.getAllCapabilities();
        Assert.assertEquals(2, capabilities.size());
        assertThat(capabilities, TestUtils.isCollectionOfExactlyElementsWithProperties("role",
                EmrApiConstants.ROLE_PREFIX_CAPABILITY + "role1", EmrApiConstants.ROLE_PREFIX_CAPABILITY + "role3"));
    }

    /**
     * @verifies return all roles with the privilege level prefix
     * @see AccountService#getAllPrivilegeLevels()
     */
    @Test
    public void getAllPrivilegeLevels_shouldReturnAllRolesWithThePrivilegeLevelPrefix() throws Exception {
        Role role1 = new Role(EmrApiConstants.ROLE_PREFIX_PRIVILEGE_LEVEL + "role1");
        Role role3 = new Role("role2");
        Role role2 = new Role(EmrApiConstants.ROLE_PREFIX_PRIVILEGE_LEVEL + "role3");

        when(userService.getAllRoles()).thenReturn(Arrays.asList(role1, role2, role3));
        List<Role> privilegeLevels = accountService.getAllPrivilegeLevels();
        Assert.assertEquals(2, privilegeLevels.size());
        assertThat(privilegeLevels, TestUtils.isCollectionOfExactlyElementsWithProperties("role",
                EmrApiConstants.ROLE_PREFIX_PRIVILEGE_LEVEL + "role1", EmrApiConstants.ROLE_PREFIX_PRIVILEGE_LEVEL + "role3"));
    }

    @Test
    public void getApiPrivileges_shouldExcludeApplicationPrivileges() throws Exception {
        Privilege getPatients = new Privilege("Get Patients");
        Privilege deletePatients = new Privilege("Delete Patients");
        Privilege vitalsApp = new Privilege(EmrApiConstants.PRIVILEGE_PREFIX_APP + " emr.vitals");
        Privilege orderEntryTask = new Privilege(EmrApiConstants.PRIVILEGE_PREFIX_TASK + " emr.orderEntry");

        when(userService.getAllPrivileges()).thenReturn(Arrays.asList(getPatients, deletePatients, vitalsApp, orderEntryTask));

        List<Privilege> apiPrivileges = accountService.getApiPrivileges();
        assertThat(apiPrivileges.size(), is(2));
        assertThat(apiPrivileges, containsInAnyOrder(getPatients, deletePatients));
    }

    @Test
    public void getApplicationPrivileges_shouldIncludeOnlyApplicationPrivileges() throws Exception {
        Privilege getPatients = new Privilege("Get Patients");
        Privilege deletePatients = new Privilege("Delete Patients");
        Privilege vitalsApp = new Privilege(EmrApiConstants.PRIVILEGE_PREFIX_APP + " emr.vitals");
        Privilege orderEntryTask = new Privilege(EmrApiConstants.PRIVILEGE_PREFIX_TASK + " emr.orderEntry");

        when(userService.getAllPrivileges()).thenReturn(Arrays.asList(getPatients, deletePatients, vitalsApp, orderEntryTask));

        List<Privilege> applicationPrivileges = accountService.getApplicationPrivileges();
        assertThat(applicationPrivileges.size(), is(2));
        assertThat(applicationPrivileges, containsInAnyOrder(vitalsApp, orderEntryTask));
    }

}
