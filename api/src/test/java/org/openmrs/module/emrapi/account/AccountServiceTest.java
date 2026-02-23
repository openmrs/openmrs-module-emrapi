package org.openmrs.module.emrapi.account;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.hamcrest.MockitoHamcrest;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.TestUtils;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.openmrs.util.OpenmrsConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AccountServiceTest  {

    private AccountServiceImpl accountService;

    private UserService userService;

    private PersonService personService;

    private ProviderService providerService;


    private EmrApiProperties emrApiProperties;

    private DomainWrapperFactory domainWrapperFactory;

    @BeforeEach
    public void setup() {
        userService = mock(UserService.class);
        personService = mock(PersonService.class);
        providerService = mock(ProviderService.class);
        emrApiProperties = mock(EmrApiProperties.class);

        domainWrapperFactory = new MockDomainWrapperFactory();

        accountService = new AccountServiceImpl();
        accountService.setUserService(userService);
        accountService.setPersonService(personService);
        accountService.setProviderService(providerService);
        accountService.setEmrApiProperties(emrApiProperties);
        accountService.setDomainWrapperFactory(domainWrapperFactory);
    }

    /**
     * @verifies get all unique accounts
     * @see AccountService#getAllAccounts()
     */
    @Test
    public void getAllAccounts_shouldGetAllUniqueAccounts() throws Exception {
        Person person1 = new Person();
        person1.addName(new PersonName("John", "","Doe"));
        User user1 = new User();
        user1.setPerson(person1);
        User user2 = new User();
        Person person2 = new Person();
        person2.addName(new PersonName("Jane", "","Doe"));
        user2.setPerson(person2);
        User daemonUser = new User();
        daemonUser.setUuid(EmrApiConstants.DAEMON_USER_UUID);
        Person daemonPerson = new Person();
        daemonUser.setPerson(daemonPerson);

        Provider provider1 = new Provider();
        provider1.setPerson(person1);//duplicate
        Provider provider2 = new Provider();
        Person person3 = new Person();
        person3.addName(new PersonName("Doc", "","Brown"));
        provider2.setPerson(person3);

        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2, daemonUser));
        when(providerService.getAllProviders()).thenReturn(Arrays.asList(provider1, provider2));

        List<AccountDomainWrapper> accounts = accountService.getAllAccounts();
        Assertions.assertEquals(3, accounts.size());

        List<Person> persons = accounts.stream().map(AccountDomainWrapper::getPerson).collect(Collectors.toList());
        assertThat(persons, containsInAnyOrder(person1, person2, person3));
    }

    @Test
    public void getAccount_shouldNotReturnUnknownProvider() throws Exception {
        Provider unknownProvider = new Provider();
        Person unknownProviderPerson = new Person();
        unknownProvider.setPerson(unknownProviderPerson);
        when(emrApiProperties.getUnknownProvider()).thenReturn(unknownProvider);
        when(providerService.getAllProviders()).thenReturn(Collections.singletonList(unknownProvider));

        List<AccountDomainWrapper> accounts = accountService.getAllAccounts();
        Assertions.assertEquals(0, accounts.size());
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
        when(personService.getPerson(MockitoHamcrest.argThat(TestUtils.equalsMatcher(personId)))).thenReturn(person);
        when(userService.getUsersByPerson(MockitoHamcrest.argThat(TestUtils.equalsMatcher(person)), any(Boolean.class))).thenReturn(
                Arrays.asList(user));

        AccountDomainWrapper account = accountService.getAccount(personId);
        Assertions.assertNotNull(account);
        Assertions.assertEquals(person, account.getPerson());
        Assertions.assertEquals(username, account.getUsername());
        Assertions.assertEquals("ht", account.getDefaultLocale().toString());
        Assertions.assertEquals(fullPrivilegeLevel, account.getPrivilegeLevel());
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
        when(userService.getUsersByPerson(MockitoHamcrest.argThat(TestUtils.equalsMatcher(person)), any(Boolean.class))).thenReturn(
                Arrays.asList(user));
        AccountDomainWrapper account = accountService.getAccountByPerson(person);
        Assertions.assertNotNull(account);
        Assertions.assertEquals(person, account.getPerson());
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
        when(providerService.getProvidersByPerson(MockitoHamcrest.argThat(TestUtils.equalsMatcher(person)), any(Boolean.class))).thenReturn(
                Arrays.asList(provider));
        AccountDomainWrapper account = accountService.getAccountByPerson(person);
        Assertions.assertNotNull(account);
        Assertions.assertEquals(person, account.getPerson());
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
        Assertions.assertEquals(2, capabilities.size());

        List<String> capabilitiesStr = capabilities.stream().map(Role::getName).collect(Collectors.toList());

        assertThat(capabilitiesStr, containsInAnyOrder(
                EmrApiConstants.ROLE_PREFIX_CAPABILITY + "role1",
                EmrApiConstants.ROLE_PREFIX_CAPABILITY + "role3"));
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
        Assertions.assertEquals(2, privilegeLevels.size());

        List<String> roleNames = privilegeLevels.stream()
                .map(Role::getRole)
                .collect(Collectors.toList());

        assertThat(roleNames, containsInAnyOrder(
                EmrApiConstants.ROLE_PREFIX_PRIVILEGE_LEVEL + "role1",
                EmrApiConstants.ROLE_PREFIX_PRIVILEGE_LEVEL + "role3"
        ));
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

    private class MockDomainWrapperFactory extends DomainWrapperFactory{

        @Override
        public AccountDomainWrapper newAccountDomainWrapper() {
            AccountDomainWrapper accountDomainWrapper = new AccountDomainWrapper();
            accountDomainWrapper.setAccountService(accountService);
            accountDomainWrapper.setUserService(userService);
            accountDomainWrapper.setPersonService(personService);
            accountDomainWrapper.setProviderService(providerService);
            return accountDomainWrapper;
        }

        @Override
        public AccountDomainWrapper newAccountDomainWrapper(Person person) {
            AccountDomainWrapper accountDomainWrapper = newAccountDomainWrapper();
            accountDomainWrapper.initializeWithPerson(person);
            return accountDomainWrapper;
        }
    }

}
