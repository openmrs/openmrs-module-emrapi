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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Person;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.api.PersonService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.providermanagement.Provider;
import org.openmrs.module.providermanagement.ProviderRole;
import org.openmrs.module.providermanagement.api.ProviderManagementService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AccountComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private PersonService personService;

    @Autowired
    private ProviderManagementService providerManagementService;

    @Before
    public void beforeAllTests() throws Exception {
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
        Assert.assertNotNull(personId);

        Context.flushSession();
        Context.clearSession();

        Person expectedPerson = personService.getPerson(personId);

        Assert.assertEquals("Mark", expectedPerson.getGivenName());
        Assert.assertEquals("Jones", expectedPerson.getFamilyName());
        Assert.assertEquals("M", expectedPerson.getGender());
        Assert.assertEquals(Context.getAuthenticatedUser(), expectedPerson.getPersonCreator());
        Assert.assertNotNull(expectedPerson.getPersonDateCreated());

        Assert.assertNull(account.getUser());

    }

    @Test
    public void shouldSavePersonAndUserAndProvider() {

        Role fullPrivileges = userService.getRole("Privilege Level: Full");
        Role archives = userService.getRole("Application Role: Archives");
        Role registration = userService.getRole("Application Role: Registration");

        ProviderRole nurse = providerManagementService.getProviderRole(1001);

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
        Assert.assertNotNull(personId);

        Integer userId = account.getUser().getUserId();
        Assert.assertNotNull(userId);

        Context.flushSession();
        Context.clearSession();

        Person expectedPerson = personService.getPerson(personId);

        Assert.assertEquals("Mark", expectedPerson.getGivenName());
        Assert.assertEquals("Jones", expectedPerson.getFamilyName());
        Assert.assertEquals("M", expectedPerson.getGender());
        Assert.assertEquals(Context.getAuthenticatedUser(), expectedPerson.getPersonCreator());
        Assert.assertNotNull(expectedPerson.getPersonDateCreated());

        User expectedUser = userService.getUser(userId);

        Assert.assertFalse(expectedUser.isRetired());
        Assert.assertEquals("mjones", expectedUser.getUsername());
        Assert.assertEquals(person, expectedUser.getPerson());
        Assert.assertEquals("fr", expectedUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE).toString());
        Assert.assertTrue(expectedUser.hasRole(fullPrivileges.toString()));
        Assert.assertTrue(expectedUser.hasRole(archives.toString()));
        Assert.assertTrue(expectedUser.hasRole(registration.toString()));

        // note that we don't expose the provider object outside of the account domain wrapper; saves confusion between the
        // two Provider object types
        List<Provider> providers = providerManagementService.getProvidersByPerson(expectedPerson, false);
        Assert.assertEquals(1, providers.size());
        Assert.assertEquals(nurse, providers.get(0).getProviderRole());
    }

    @Test
    public void shouldLoadExistingPersonAndUserAndProvider() {

        Role fullPrivileges = userService.getRole("Privilege Level: Full");
        Role archives = userService.getRole("Application Role: Archives");
        Role registration = userService.getRole("Application Role: Registration");

        ProviderRole nurse = providerManagementService.getProviderRole(1001);

        Person person = personService.getPerson(501);  // existing person with user account in test dataset

        AccountDomainWrapper account = accountService.getAccountByPerson(person);

        Assert.assertEquals("Bruno", account.getGivenName());
        Assert.assertEquals("Otterbourg", account.getFamilyName());
        Assert.assertEquals("F", account.getGender());

        Assert.assertFalse(account.getUserEnabled());     // this user account happens to be retired in test dataset
        Assert.assertEquals("bruno", account.getUsername());
        Assert.assertEquals("fr", account.getDefaultLocale().toString());
        Assert.assertTrue(account.getPrivilegeLevel().equals(fullPrivileges));
        Assert.assertEquals(2, account.getCapabilities().size());
        Assert.assertTrue(account.getCapabilities().contains(archives));
        Assert.assertTrue(account.getCapabilities().contains(registration));

        Assert.assertEquals(nurse, account.getProviderRole());

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
        Assert.assertTrue(user.isRetired());
        Assert.assertNotNull(user.getDateRetired());
        Assert.assertEquals(Context.getAuthenticatedUser(), user.getRetiredBy());
        Assert.assertNotNull(user.getRetireReason());
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
        Assert.assertFalse(user.isRetired());
        Assert.assertNull(user.getDateRetired());
        Assert.assertNull(user.getRetiredBy());
        Assert.assertNull(user.getRetireReason());
    }

    @Test
    public void shouldSetProviderRoleToNull() {

        Person person = personService.getPerson(501);  // existing person with active provider account

        AccountDomainWrapper account = accountService.getAccountByPerson(person);
        account.setProviderRole(null);
        account.save();

        Context.flushSession();
        Context.clearSession();

        List<Provider> providers = providerManagementService.getProvidersByPerson(person, false);
        Assert.assertEquals(1, providers.size());
        Assert.assertNull(providers.get(0).getProviderRole());
    }

    @Test
    public void shouldHandlePersonWithoutUser() {

        Person person = personService.getPerson(2);

        AccountDomainWrapper account = accountService.getAccountByPerson(person);

        Assert.assertNull(account.getUser());
        Assert.assertNull(account.getUsername());
        Assert.assertNull(account.getDefaultLocale());
        Assert.assertNull(account.getCapabilities());
        Assert.assertNull(account.getPrivilegeLevel());
        Assert.assertNull(account.getUserEnabled());

    }

}
