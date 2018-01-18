package org.openmrs.module.emrapi.account;

import junit.framework.Assert;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.providermanagement.Provider;
import org.openmrs.module.providermanagement.ProviderRole;
import org.openmrs.module.providermanagement.api.ProviderManagementService;
import org.openmrs.util.OpenmrsConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.util.OpenmrsConstants.USER_PROPERTY_LOCKOUT_TIMESTAMP;
import static org.openmrs.util.OpenmrsConstants.USER_PROPERTY_LOGIN_ATTEMPTS;

public class AccountDomainWrapperTest {

    private AccountService accountService;

    private UserService userService;

    private PersonService personService;

    private ProviderService providerService;

    private ProviderManagementService providerManagementService;

    private ProviderIdentifierGenerator providerIdentifierGenerator = null;

    private Role fullPrivileges;

    private Role limitedPrivileges;

    private Role receptionApp;

    private Role archiveApp;

    private Role adminApp;

    @Before
    public void setup() {
        accountService = mock(AccountService.class);
        userService = mock(UserService.class);
        personService = mock(PersonService.class);
        providerService = mock(ProviderService.class);
        providerManagementService = mock(ProviderManagementService.class);

        fullPrivileges = new Role();
        fullPrivileges.setRole(EmrApiConstants.ROLE_PREFIX_PRIVILEGE_LEVEL + "Full");
        limitedPrivileges = new Role();
        limitedPrivileges.setRole(EmrApiConstants.ROLE_PREFIX_PRIVILEGE_LEVEL + "Limited");
        when(accountService.getAllPrivilegeLevels()).thenReturn(Arrays.asList(fullPrivileges, limitedPrivileges));

        receptionApp = new Role();
        receptionApp.setRole(EmrApiConstants.ROLE_PREFIX_CAPABILITY + "Reception");
        archiveApp = new Role();
        archiveApp.setRole(EmrApiConstants.ROLE_PREFIX_CAPABILITY + "Archives");
        adminApp = new Role();
        adminApp.setRole(EmrApiConstants.ROLE_PREFIX_CAPABILITY + "Admin");
        when(accountService.getAllCapabilities()).thenReturn(Arrays.asList(receptionApp, archiveApp, adminApp));

    }

    @Test
    public void settingAccountDomainWrapperShouldSetPerson() {

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(new Person());
        account.setGivenName("Mark");
        account.setFamilyName("Jones");
        account.setGender("M");

        Person person = account.getPerson();
        Assert.assertEquals("Mark", person.getGivenName());
        Assert.assertEquals("Jones", person.getFamilyName());
        Assert.assertEquals("M", person.getGender());
    }

    @Test
    public void gettingAccountDomainWrapperShouldFetchFromPerson() {

        Person person = new Person();
        person.addName(new PersonName());
        person.getPersonName().setGivenName("Mark");
        person.getPersonName().setFamilyName("Jones");
        person.setGender("M");

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        Assert.assertEquals("Mark", account.getGivenName());
        Assert.assertEquals("Jones", account.getFamilyName());
        Assert.assertEquals("M", account.getGender());
    }

    @Test
    public void settingAccountDomainWrapperShouldSetUser() {

        Person person = new Person();

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setUsername("mjones");
        account.setDefaultLocale(new Locale("fr"));
        account.setPrivilegeLevel(fullPrivileges);

        Set<Role> capabilities = new HashSet<Role>();
        capabilities.add(archiveApp);
        capabilities.add(receptionApp);
        account.setCapabilities(capabilities);

        User user = account.getUser();
        Assert.assertEquals("mjones", user.getUsername());
        Assert.assertEquals(person, user.getPerson());
        Assert.assertEquals("fr", user.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE).toString());
        Assert.assertTrue(user.hasRole(fullPrivileges.toString()));
        Assert.assertTrue(user.hasRole(archiveApp.toString()));
        Assert.assertTrue(user.hasRole(receptionApp.toString()));
    }

    @Test
    public void gettingAccountDomainWrapperShouldFetchFromUser() {

        Person person = new Person();
        person.setId(1);
        User user = new User();
        user.setPerson(person);
        user.setUsername("mjones");
        user.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE, new Locale("fr").toString());
        user.addRole(fullPrivileges);
        user.addRole(archiveApp);
        user.addRole(receptionApp);

        when(userService.getUsersByPerson(eq(person), eq(false))).thenReturn(Collections.singletonList(user));

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        Assert.assertEquals("mjones", account.getUsername());
        Assert.assertEquals("fr", account.getDefaultLocale().toString());
        Assert.assertEquals(fullPrivileges, account.getPrivilegeLevel());
        Assert.assertTrue(account.getCapabilities().contains(receptionApp));
        Assert.assertTrue(account.getCapabilities().contains(archiveApp));
    }


    @Test
    public void testCreatingPersonWithoutCreatingUser() {

        Person person = new Person();

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setGivenName("Mark");
        account.setFamilyName("Jones");
        account.setGender("M");

        // mimic spring binding blanks
        account.setCapabilities(null);
        account.setDefaultLocale(null);
        account.setPrivilegeLevel(null);
        account.setUsername("");

        // make sure the person has been created, but not the user or provider
        Assert.assertNotNull(account.getPerson());
        Assert.assertNull(account.getUser());
    }

    @Test
    public void shouldDisableExistingUserAccount() {

        Person person = new Person();
        person.setId(1);
        User user = new User();
        user.setPerson(person);
        user.setUsername("mjones");

        when(userService.getUsersByPerson(eq(person), eq(false))).thenReturn(Collections.singletonList(user));

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setUserEnabled(false);

        Assert.assertTrue(user.getRetired());
        // TODO: figure out how to set retired by
        //Assert.assertNotNull(user.getRetiredBy());
        Assert.assertNotNull(user.getRetireReason());
        Assert.assertNotNull(user.getDateRetired());
    }

    @Test
    public void shouldEnablePreviouslyRetiredUserAccount() {

        Person person = new Person();
        person.setId(1);
        User user = new User();
        user.setPerson(person);
        user.setUsername("mjones");
        user.setRetired(true);
        // TODO: figure out how to handle retired by
        //provider.setRetiredBy();
        user.setDateRetired(new Date());
        user.setRetireReason("test");

        when(userService.getUsersByPerson(eq(person), eq(false))).thenReturn(Collections.singletonList(user));

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setUserEnabled(true);

        Assert.assertFalse(user.getRetired());
        Assert.assertNull(user.getRetiredBy());
        Assert.assertNull(user.getRetireReason());
        Assert.assertNull(user.getDateRetired());
    }

    @Test
    public void shouldEnableNewUserAccount() {

        Person person = new Person();

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setUserEnabled(true);

        Assert.assertNotNull(account.getUser());
    }

    @Test
    public void shouldReturnFalseIfUserRetired() {

        Person person = new Person();
        person.setId(1);
        User user = new User();
        user.setPerson(person);
        user.setRetired(true);

        when(userService.getUsersByPerson(eq(person), eq(false))).thenReturn(Collections.singletonList(user));

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        Assert.assertFalse(account.getUserEnabled());

    }

    @Test
    public void shouldReturnTrueIfUserNotRetired() {

        Person person = new Person();
        person.setId(1);
        User user = new User();
        user.setPerson(person);
        user.setRetired(false);

        when(userService.getUsersByPerson(eq(person), eq(false))).thenReturn(Collections.singletonList(user));

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        Assert.assertTrue(account.getUserEnabled());

    }

    @Test
    public void shouldReturnNullIfNoUser() {

        Person person = new Person();

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        Assert.assertNull(account.getUserEnabled());

    }

    @Test
    public void shouldChangeExistingUserInformation() {

        Person person = new Person();
        person.setId(1);
        User user = new User();
        user.setPerson(person);
        user.setUsername("mjones");
        user.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE, new Locale("fr").toString());
        user.addRole(fullPrivileges);
        user.addRole(archiveApp);
        user.addRole(receptionApp);

        when(userService.getUsersByPerson(eq(person), eq(false))).thenReturn(Collections.singletonList(user));

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);

        account.setUsername("msmith");
        account.setPrivilegeLevel(limitedPrivileges);
        Set<Role> roles = new HashSet<Role>();
        roles.add(archiveApp);
        roles.add(adminApp);
        account.setCapabilities(roles);

        Assert.assertEquals("msmith", user.getUsername());
        Assert.assertTrue(user.getRoles().contains(limitedPrivileges));
        Assert.assertTrue(user.getRoles().contains(archiveApp));
        Assert.assertTrue(user.getRoles().contains(adminApp));
        Assert.assertFalse(user.getRoles().contains(receptionApp));
        Assert.assertFalse(user.getRoles().contains(fullPrivileges));

    }

    @Test
    public void shouldChangeExistingProviderRole() {

        Person person = new Person();
        person.setId(1);
        Provider provider = new Provider();
        ProviderRole originalProviderRole = new ProviderRole();
        provider.setProviderRole(originalProviderRole);

        when(providerManagementService.getProvidersByPerson(eq(person), eq(false))).thenReturn(Collections.singletonList(provider));

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        ProviderRole newProviderRole = new ProviderRole();
        account.setProviderRole(newProviderRole);

        Assert.assertEquals(newProviderRole, account.getProviderRole());
    }

    @Test
    public void shouldChangeExistingProviderRoleToNull() {

        Person person = new Person();
        person.setId(1);
        Provider provider = new Provider();
        ProviderRole originalProviderRole = new ProviderRole();
        provider.setProviderRole(originalProviderRole);

        when(providerManagementService.getProvidersByPerson(eq(person), eq(false))).thenReturn(Collections.singletonList(provider));

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setProviderRole(null);

        Assert.assertNull(account.getProviderRole());
    }

    @Test
    public void shouldRemoveAllExistingCapabilities() {

        Person person = new Person();
        person.setId(1);
        User user = new User();
        user.setPerson(person);
        user.setUsername("mjones");
        user.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE, new Locale("fr").toString());
        user.addRole(fullPrivileges);
        user.addRole(archiveApp);
        user.addRole(receptionApp);

        when(userService.getUsersByPerson(eq(person), eq(false))).thenReturn(Collections.singletonList(user));

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setCapabilities(null);

        Assert.assertFalse(user.getRoles().contains(receptionApp));
        Assert.assertFalse(user.getRoles().contains(archiveApp));
    }

    @Test
    public void testThatAccountIsNotLockedWhenNeverLocked() throws Exception {
        AccountDomainWrapper account = initializeNewAccountDomainWrapper(new Person());
        account.setUserEnabled(true);
        assertFalse(account.isLocked());
    }

    @Test
    public void testThatAccountIsNotLockedWhenLockedALongTimeAgo() throws Exception {
        AccountDomainWrapper account = initializeNewAccountDomainWrapper(new Person());
        account.setUserEnabled(true);
        account.getUser().setUserProperty(USER_PROPERTY_LOCKOUT_TIMESTAMP, "" + DateUtils.addDays(new Date(), -1).getTime());
        assertFalse(account.isLocked());
    }

    @Test
    public void testThatAccountIsLockedWhenStillLocked() throws Exception {
        AccountDomainWrapper account = initializeNewAccountDomainWrapper(new Person());
        account.setUserEnabled(true);
        account.getUser().setUserProperty(USER_PROPERTY_LOCKOUT_TIMESTAMP, "" + DateUtils.addMinutes(new Date(), 5).getTime());
        assertTrue(account.isLocked());
    }

    @Test
    public void testUnlockingAccount() throws Exception {
        AccountDomainWrapper account = initializeNewAccountDomainWrapper(new Person());
        account.setUserEnabled(true);
        account.getUser().setUserProperty(USER_PROPERTY_LOCKOUT_TIMESTAMP, "" + DateUtils.addMinutes(new Date(), 5).getTime());

        account.unlock();

        assertThat(account.getUser().getUserProperty(USER_PROPERTY_LOCKOUT_TIMESTAMP), is(""));
        assertThat(account.getUser().getUserProperty(USER_PROPERTY_LOGIN_ATTEMPTS), is(""));

        verify(userService).createUser(account.getUser(), null);
    }


    @Test
    public void testSaveAccountWithOnlyPerson() throws Exception {

        Person person = new Person();

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.save();

        verify(personService).savePerson(person);

        verify(userService, never()).createUser(any(User.class), anyString());
    }

    @Test
    public void testSaveAccountWithNewPersonAndUser() throws Exception {

        Person person = new Person();

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setUserEnabled(true);
        account.setPassword("abc");
        account.setConfirmPassword("abc");
        account.save();

        verify(personService).savePerson(person);
        verify(userService).createUser(account.getUser(), "abc");

        verify(userService, never()).changePassword(account.getUser(), "abc");
    }

    @Test
    public void testSaveAccountWithPersonAndProvider() throws Exception {

        Person person = new Person();
        ProviderRole providerRole = new ProviderRole();

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setProviderRole(providerRole);
        account.save();

        Provider expectedProvider = new Provider();
        expectedProvider.setProviderRole(providerRole);

        verify(personService).savePerson(person);
        verify(providerService).saveProvider(argThat(new IsExpectedProvider(expectedProvider)));
        verify(userService, never()).createUser(any(User.class), anyString());
    }

    @Test
    public void testSaveAccountWithPasswordChangeForExistingUser() throws Exception {

        Person person = new Person();

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setUserEnabled(true);
        account.setPassword("abc");
        account.setConfirmPassword("abc");
        account.getUser().setUserId(1);    // mimic making this user persistent
        account.save();

        verify(userService).createUser(account.getUser(), "abc");
        verify(userService).changePassword(account.getUser(), "abc");
    }

    @Test
    public void testSaveAccountShouldNotPersistProviderIfRoleSetToNull() throws Exception {

        Person person = new Person();
        person.setId(1);

        when(providerManagementService.getProvidersByPerson(eq(person), eq(false))).thenReturn(null);

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setProviderRole(null);
        account.save();

        verify(providerService, never()).saveProvider(any(Provider.class));

    }

    @Test
    public void testSaveAccountShouldNotRetireProviderIfProviderRoleSetToNull() throws Exception {

        // we used to retire a provider in this case, but we've switched it so we don't
        // we will probably want to add an explicit way to retire a provider

        Person person = new Person();
        person.setId(1);
        Provider provider = new Provider();
        provider.setId(1);  // to mimic persistence
        ProviderRole originalProviderRole = new ProviderRole();
        provider.setProviderRole(originalProviderRole);

        when(providerManagementService.getProvidersByPerson(eq(person), eq(false))).thenReturn(Collections.singletonList(provider));

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setProviderRole(null);
        account.save();

        verify(providerService, never()).retireProvider(any(Provider.class), anyString());
    }

    @Test
    public void testSaveAccountShouldNotRetireProviderIfProviderNotPersisted() throws Exception {

        Person person = new Person();
        person.setId(1);
        Provider provider = new Provider();
        ProviderRole originalProviderRole = new ProviderRole();
        provider.setProviderRole(originalProviderRole);

        when(providerManagementService.getProvidersByPerson(eq(person), eq(false))).thenReturn(Collections.singletonList(provider));

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setProviderRole(null);
        account.save();

        verify(providerService, never()).retireProvider(any(Provider.class), anyString());
    }

    @Test
    public void testShouldGenerateProviderIdentifierIfProviderIdentifierGeneratorSet() throws Exception {

        Person person = new Person();
        ProviderRole providerRole = new ProviderRole();

        providerIdentifierGenerator = mock(ProviderIdentifierGenerator.class);
        when(providerIdentifierGenerator.generateIdentifier(any(Provider.class))).thenReturn("123");

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setProviderRole(providerRole);
        account.save();

        Provider expectedProvider = new Provider();
        expectedProvider.setProviderRole(providerRole);

        Provider expectedProviderWithIdentifier = new Provider();
        expectedProviderWithIdentifier.setProviderRole(providerRole);
        expectedProviderWithIdentifier.setIdentifier("123");

        verify(personService).savePerson(person);
        verify(providerService, atLeast(1)).saveProvider(argThat(new IsExpectedProvider(expectedProviderWithIdentifier)));

    }

    @Test
    public void testShouldNotModifyExistingProviderIdentifier() throws Exception {

        Person person = new Person();
        ProviderRole providerRole = new ProviderRole();

        providerIdentifierGenerator = mock(ProviderIdentifierGenerator.class);
        when(providerIdentifierGenerator.generateIdentifier(any(Provider.class))).thenReturn("456");

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setProviderRole(providerRole);
        account.getProvider().setIdentifier("123");  // existing identifier
        account.save();

        Provider expectedProvider = new Provider();
        expectedProvider.setProviderRole(providerRole);
        expectedProvider.setIdentifier("123");

        verify(personService).savePerson(person);
        verify(providerService, times(1)).saveProvider(argThat(new IsExpectedProvider(expectedProvider)));
    }

    @Test
    public void testShouldNotFailIfProviderIdentifierGeneratorDefinedButNoProvider() throws Exception {

        providerIdentifierGenerator = mock(ProviderIdentifierGenerator.class);
        when(providerIdentifierGenerator.generateIdentifier(any(Provider.class))).thenReturn("123");

        Person person = new Person();

        AccountDomainWrapper account = initializeNewAccountDomainWrapper(person);
        account.setUserEnabled(true);
        account.setPassword("abc");
        account.setConfirmPassword("abc");
        account.save();

        verify(personService).savePerson(person);
        verify(userService).createUser(account.getUser(), "abc");
        verify(userService, never()).changePassword(account.getUser(), "abc");
    }

    private AccountDomainWrapper initializeNewAccountDomainWrapper(Person person) {
        return new AccountDomainWrapper(person, accountService, userService,
                providerService, providerManagementService, personService, providerIdentifierGenerator);
    }

    private class IsExpectedProvider extends ArgumentMatcher<Provider> {

        private Provider expectedProvider;

        public IsExpectedProvider(Provider provider) {
            this.expectedProvider = provider;
        }

        @Override
        public boolean matches(Object o) {

            Provider provider = (Provider) o;

            try {
                assertThat(provider.getId(), is(expectedProvider.getId()));
                assertThat(provider.getProviderRole(), is(expectedProvider.getProviderRole()));
                assertThat(provider.getIdentifier(), is(expectedProvider.getIdentifier()));
                return true;
            } catch (AssertionError e) {
                return false;
            }
        }
    }
}
