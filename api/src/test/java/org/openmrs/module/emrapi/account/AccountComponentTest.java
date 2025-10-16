/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.emrapi.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.ProviderRole;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccountComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private PersonService personService;

    @Qualifier("providerService")
    @Autowired
    private ProviderService providerService;

    @BeforeEach
    public void beforeAllTests() throws Exception {
        executeDataSet("baseTestDataset.xml");
        executeDataSet("accountComponentTestDataset.xml");
    }

    @Test
    public void shouldSavePerson() {

        Person person = new Person();

        AccountDomainWrapper account = accountService.getAccountByPerson(person);
        account.setGivenName("Mark");
        account.setFamilyName("Jones");
        account.setGender("M");
        account.save();

        Integer personId = account.getPerson().getPersonId();
        assertNotNull(personId);

        Context.flushSession();
        Context.clearSession();

        Person expectedPerson = personService.getPerson(personId);

        assertEquals("Mark", expectedPerson.getGivenName());
        assertEquals("Jones", expectedPerson.getFamilyName());
        assertEquals("M", expectedPerson.getGender());
        assertEquals(Context.getAuthenticatedUser(), expectedPerson.getPersonCreator());
        assertNotNull(expectedPerson.getPersonDateCreated());

        assertNull(account.getUser());

    }

    @Test
    public void shouldSavePersonAndUserAndProvider() {

        Role fullPrivileges = userService.getRole("Privilege Level: Full");
        Role archives = userService.getRole("Application Role: Archives");
        Role registration = userService.getRole("Application Role: Registration");

        ProviderRole nurse = providerService.getProviderRole(1001);

        Person person = new Person();

        AccountDomainWrapper account = accountService.getAccountByPerson(person);
        account.setGivenName("Mark");
        account.setFamilyName("Jones");
        account.setGender("M");

        account.setUserEnabled(true);
        account.setUsername("mjones");
        account.setPassword("Mjones123");
        account.setDefaultLocale(new Locale("fr"));
        account.setPrivilegeLevel(fullPrivileges);

        Set<Role> capabilities = new HashSet<Role>();
        capabilities.add(registration);
        capabilities.add(archives);
        account.setCapabilities(capabilities);

        account.setProviderRole(nurse);

        account.save();

        Integer personId = account.getPerson().getPersonId();
        assertNotNull(personId);

        Integer userId = account.getUser().getUserId();
        assertNotNull(userId);

        Context.flushSession();
        Context.clearSession();

        Person expectedPerson = personService.getPerson(personId);

        assertEquals("Mark", expectedPerson.getGivenName());
        assertEquals("Jones", expectedPerson.getFamilyName());
        assertEquals("M", expectedPerson.getGender());
        assertEquals(Context.getAuthenticatedUser(), expectedPerson.getPersonCreator());
        assertNotNull(expectedPerson.getPersonDateCreated());

        User expectedUser = userService.getUser(userId);

        assertFalse(expectedUser.isRetired());
        assertEquals("mjones", expectedUser.getUsername());
        assertEquals(person, expectedUser.getPerson());
        assertEquals("fr", expectedUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE).toString());
        assertTrue(expectedUser.hasRole(fullPrivileges.toString()));
        assertTrue(expectedUser.hasRole(archives.toString()));
        assertTrue(expectedUser.hasRole(registration.toString()));

        // note that we don't expose the provider object outside of the account domain wrapper; saves confusion between the
        // two Provider object types
        List<Provider> providers = (List<Provider>) providerService.getProvidersByPerson(expectedPerson, false);
        assertEquals(1, providers.size());
        assertEquals(nurse, providers.get(0).getProviderRole());
    }

    @Test
    public void shouldLoadExistingPersonAndUserAndProvider() {

        Role fullPrivileges = userService.getRole("Privilege Level: Full");
        Role archives = userService.getRole("Application Role: Archives");
        Role registration = userService.getRole("Application Role: Registration");

        ProviderRole nurse = providerService.getProviderRole(1001);

        Person person = personService.getPerson(501);  // existing person with user account in test dataset

        AccountDomainWrapper account = accountService.getAccountByPerson(person);

        assertEquals("Bruno", account.getGivenName());
        assertEquals("Otterbourg", account.getFamilyName());
        assertEquals("F", account.getGender());

        assertFalse(account.getUserEnabled());     // this user account happens to be retired in test dataset
        assertEquals("bruno", account.getUsername());
        assertEquals("fr", account.getDefaultLocale().toString());
        assertTrue(account.getPrivilegeLevel().equals(fullPrivileges));
        assertEquals(2, account.getCapabilities().size());
        assertTrue(account.getCapabilities().contains(archives));
        assertTrue(account.getCapabilities().contains(registration));

        assertEquals(nurse, account.getProviderRole());

    }

    @Test
    public void shouldRetireExistingUser() {

        Person person = personService.getPerson(502);  // existing person with active user account in test dataset

        AccountDomainWrapper account = accountService.getAccountByPerson(person);
        account.setUserEnabled(false);
        account.save();

        Context.flushSession();
        Context.clearSession();

        User user = userService.getUser(502);
        assertTrue(user.isRetired());
        assertNotNull(user.getDateRetired());
        assertEquals(Context.getAuthenticatedUser(), user.getRetiredBy());
        assertNotNull(user.getRetireReason());
    }

    @Test
    public void shouldUnretireExistingUser() {

        Person person = personService.getPerson(501);  // existing person with retired user account in test dataset

        AccountDomainWrapper account = accountService.getAccountByPerson(person);
        account.setUserEnabled(true);
        account.save();

        Context.flushSession();
        Context.clearSession();

        User user = userService.getUser(501);
        assertFalse(user.isRetired());
        assertNull(user.getDateRetired());
        assertNull(user.getRetiredBy());
        assertNull(user.getRetireReason());
    }

    @Test
    public void shouldSetProviderRoleToNull() {

        Person person = personService.getPerson(501);  // existing person with active provider account

        AccountDomainWrapper account = accountService.getAccountByPerson(person);
        account.setProviderRole(null);
        account.save();

        Context.flushSession();
        Context.clearSession();

        List<Provider> providers = (List<Provider>) providerService.getProvidersByPerson(person, false);
        assertEquals(1, providers.size());
        assertNull(providers.get(0).getProviderRole());
    }

    @Test
    public void shouldHandlePersonWithoutUser() {

        Person person = personService.getPerson(2);

        AccountDomainWrapper account = accountService.getAccountByPerson(person);

        assertNull(account.getUser());
        assertNull(account.getUsername());
        assertNull(account.getDefaultLocale());
        assertNull(account.getCapabilities());
        assertNull(account.getPrivilegeLevel());
        assertNull(account.getUserEnabled());

    }

    /**
     * @see AccountService#getAccounts(AccountSearchCriteria) ()
     */
    @Test
    public void shouldGetAccountsThatMatchSearchCriteria() throws Exception {
        // user 501:  username = bruno, systemId = 2-6, retired = true, person=501, provider 1001, identifier = 123, retired = false
        // user 502:  username = butch, systemId = 3-4, retired = false, person=502
        // provider 1, person=1, identifier = Test, retired = false
        // provider 5000, person=5000, name = John Smith, set as unknown provider
        // provider 5001, person=5001, name = Mary Smith, no user

        // Should include all accounts except for the unknown provider
        AccountSearchCriteria criteria = new AccountSearchCriteria();
        List<AccountDomainWrapper> allAccounts = accountService.getAccounts(criteria);
        assertThat(allAccounts.size(), equalTo(4));

        criteria.setNameOrIdentifier("bruno");
        allAccounts = accountService.getAccounts(criteria);
        assertThat(allAccounts.size(), equalTo(1));

        criteria.setNameOrIdentifier("Smith");
        allAccounts = accountService.getAccounts(criteria);
        assertThat(allAccounts.size(), equalTo(1));

        criteria.setNameOrIdentifier("3");
        allAccounts = accountService.getAccounts(criteria);
        assertThat(allAccounts.size(), equalTo(2));

        criteria.setNameOrIdentifier("Bob");
        allAccounts = accountService.getAccounts(criteria);
        assertThat(allAccounts.size(), equalTo(0));
    }
}
