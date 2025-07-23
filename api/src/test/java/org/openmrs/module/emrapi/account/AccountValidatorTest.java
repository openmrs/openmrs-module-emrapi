package org.openmrs.module.emrapi.account;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.ProviderRole;
import org.openmrs.Role;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;


public class AccountValidatorTest {

    private AccountValidator validator;

    private AccountDomainWrapper account;

    private AccountService accountService;

    private UserService userService;

    private ProviderService providerService;


    private PersonService personService;

    private ProviderIdentifierGenerator providerIdentifierGenerator;

    private Role fullPrivileges;

    private Role someCapability;

    private Set<Role> someCapabilitySet;

    private ProviderRole someProviderRole;

    @Before
    public void setValidator() {

        accountService = Mockito.mock(AccountService.class);
        userService = Mockito.mock(UserService.class);
        providerService = Mockito.mock(ProviderService.class);
        personService = Mockito.mock(PersonService.class);

        validator = new AccountValidator();
        validator.setMessageSourceService(Mockito.mock(MessageSourceService.class));
        validator.setUserService(userService);

        fullPrivileges = new Role(EmrApiConstants.PRIVILEGE_LEVEL_FULL_ROLE);
        when(accountService.getAllPrivilegeLevels()).thenReturn(Collections.singletonList(fullPrivileges));

        someCapability = new Role(EmrApiConstants.ROLE_PREFIX_CAPABILITY + "Some Capability");
        someCapabilitySet = new HashSet<Role>();
        someCapabilitySet.add(someCapability);
        when(accountService.getAllCapabilities()).thenReturn(Collections.singletonList(someCapability));

        Person person = new Person();
        person.addName(new PersonName());

        someProviderRole = new ProviderRole();

        account = new AccountDomainWrapper(person, accountService, userService, providerService,
                 personService, providerIdentifierGenerator);
    }

    /**
     * @verifies reject an empty givenname
     * @see AccountValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldRejectAnEmptyGivenname() throws Exception {
        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertTrue(errors.hasFieldErrors("givenName"));
    }

    /**
     * @verifies reject an empty familyname
     * @see AccountValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldRejectAnEmptyFamilyname() throws Exception {
        account.setGivenName("give name");

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertTrue(errors.hasFieldErrors("familyName"));
    }

    @Test
    public void validate_shouldRejectAnEmptyGender() throws Exception {
        account.setGivenName("givenName");
        account.setFamilyName("familyName");

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertTrue(errors.hasFieldErrors("gender"));
    }

    /**
     * @verifies reject an empty privilegeLevel if user is not null
     * @see AccountValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldRejectAnEmptyPrivilegeLevelIfUserIsNotNull() throws Exception {
        account.setGivenName("give name");
        account.setFamilyName("family name");
        account.setGender("M");
        account.setUsername("username");

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertTrue(errors.hasFieldErrors("privilegeLevel"));
    }

    /**
     * @verifies reject an empty privilegeLevel if user is not null
     * @see AccountValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldRejectAnEmptyPrivilegeLevelIfUserIsPersisted() throws Exception {
        account.setGivenName("give name");
        account.setFamilyName("family name");
        account.setGender("M");
        account.setUsername("username");
        account.getUser().setUserId(1);    // mimick persistence

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertTrue(errors.hasFieldErrors("privilegeLevel"));
    }

    /**
     * @verifies reject an empty username if user is not null
     * @see AccountValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldRejectAnEmptyUsernameIfUserIsNotNull() throws Exception {
        account.setGivenName("give name");
        account.setFamilyName("family name");
        account.setGender("M");
        account.setPrivilegeLevel(fullPrivileges);

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertTrue(errors.hasFieldErrors("username"));
    }

    /**
     * @verifies reject password and confirm password if they dont match
     * @see AccountValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldRejectPasswordAndConfirmPasswordIfTheyDontMatch() throws Exception {
        account.setGivenName("give name");
        account.setFamilyName("family name");
        account.setGender("M");
        account.setUsername("username");
        account.setPassword("password");
        account.setConfirmPassword("confirm password");

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertTrue(errors.hasFieldErrors("password"));
    }

    /**
     * @verifies require confirm password if password is provided
     * @see AccountValidator#validate(Object, Errors)
     */
    @Test
    public void shouldCreateErrorWhenConfirmPasswordIsNotProvided() throws Exception {
        account.setGivenName("give name");
        account.setFamilyName("family name");
        account.setGender("M");
        account.setUsername("username");
        account.setPassword("password");

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertTrue(errors.hasFieldErrors("password"));
        assertTrue(errors.hasFieldErrors("confirmPassword"));
    }

    /**
     * @verifies require confirm password if password is provided
     * @see AccountValidator#validate(Object, Errors)
     */
    @Test
    public void shouldCreateErrorWhenPasswordIsNotProvided() throws Exception {
        account.setGivenName("give name");
        account.setFamilyName("family name");
        account.setGender("M");
        account.setUsername("username");
        account.setConfirmPassword("password");

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertTrue(errors.hasFieldErrors("password"));
        assertTrue(errors.hasFieldErrors("confirmPassword"));
    }


    /**
     * @verifies pass for a valid account
     * @see AccountValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldFailIfNoProviderRoleSpecified() throws Exception {
        account.setUsername("username");
        account.setGivenName("give name");
        account.setFamilyName("family name");
        account.setGender("M");
        account.setPassword("Password123");
        account.setConfirmPassword("Password123");
        account.setPrivilegeLevel(fullPrivileges);
        account.setCapabilities(someCapabilitySet);

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertTrue(errors.hasFieldErrors("providerRole"));
    }


    /**
     * @verifies pass for a valid account
     * @see AccountValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldPassForAValidAccount() throws Exception {
        account.setUsername("username");
        account.setGivenName("give name");
        account.setFamilyName("family name");
        account.setGender("M");
        account.setPassword("Password123");
        account.setConfirmPassword("Password123");
        account.setPrivilegeLevel(fullPrivileges);
        account.setCapabilities(someCapabilitySet);
        account.setProviderRole(someProviderRole);

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertFalse(errors.hasErrors());
    }

    /**
     * @verifies require passwords for a new a user account
     * @see AccountValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldRequirePasswordsForNewAUserAccount() throws Exception {
        account.setUsername("username");
        account.setGivenName("give name");
        account.setFamilyName("family name");
        account.setGender("M");
        account.setUsername("username");
        account.setPrivilegeLevel(fullPrivileges);

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertTrue(errors.hasFieldErrors("password"));
        assertTrue(errors.hasFieldErrors("confirmPassword"));
    }

    /**
     * @verifies don't require a password for an existing user account
     * @see AccountValidator#validate(Object, Errors)
     */
    @Test
    public void validate_shouldNotRequirePasswordsForExistingUserAccount() throws Exception {
        account.setUsername("username");
        account.setGivenName("give name");
        account.setFamilyName("family name");
        account.setGender("M");
        account.setUsername("username");
        account.setPrivilegeLevel(fullPrivileges);
        account.getUser().setUserId(1);  // mock making this user persistent

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);
        assertFalse(errors.hasFieldErrors("password"));
        assertFalse(errors.hasFieldErrors("confirmPassword"));
    }

    @Test
    public void shouldVerifyIfPasswordIsBeingValidated() {

        createAccountWithUsernameAs("username");

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        OpenmrsUtil.validatePassword("username", "Password123", "systemId");
    }

    @Test
    public void shouldCreateAnErrorMessageWhenPasswordIsWrong() {
        createAccountWithUsernameAs("username");
        account.setPassword("1");

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertTrue(errors.hasErrors());

        List<FieldError> errorList = errors.getFieldErrors("password");

        assertThat(errorList.size(), is(1));
    }

    @Test
    public void shouldCreateAnErrorMessageWhenUsernameHasOnlyOneCharacter() {

        createAccountWithUsernameAs("a");
        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertTrue(errors.hasErrors());
        List<FieldError> errorList = errors.getFieldErrors("username");
        assertThat(errorList.size(), is(1));

    }

    @Test
    public void shouldCreateAnErrorMessageWhenUsernameHasMoreThanFiftyCharacters() {

        createAccountWithUsernameAs("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertTrue(errors.hasErrors());
        List<FieldError> errorList = errors.getFieldErrors("username");
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void shouldCreateAnErrorMessageWhenUserNameCharactersAreNotValid() {

        createAccountWithUsernameAs("usern@me");
        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertTrue(errors.hasErrors());
        List<FieldError> errorList = errors.getFieldErrors("username");
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void shouldValidateIfUserNameCharactersAreValid() {

        createAccountWithUsernameAs("usern1");
        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertFalse(errors.hasErrors());
    }

    @Test
    public void shouldCreateAnErrorMessageWhenUserIsNullAndNoProviderRole() {

        account.setFamilyName("family name");
        account.setGivenName("given Name");

        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertTrue(errors.hasErrors());
    }

    @Test
    public void shouldCreateErrorMessageIfUserWithNoCapabilities() {

        createAccountWithUsernameAs("username");
        account.setCapabilities(new HashSet<Role>());
        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertTrue(errors.hasErrors());
        List<FieldError> errorList = errors.getFieldErrors("capabilities");
        assertThat(errorList.size(), is(1));
    }

    @Test
    public void shouldCreateErrorMessageIfDuplicateUsername() {

        createAccountWithUsernameAs("username");
        when(userService.hasDuplicateUsername(account.getUser())).thenReturn(true);
        Errors errors = new BindException(account, "account");
        validator.validate(account, errors);

        assertTrue(errors.hasErrors());
        List<FieldError> errorList = errors.getFieldErrors("username");
        assertThat(errorList.size(), is(1));
    }

    private void createAccountWithUsernameAs(String username) {
        account.setUsername(username);
        account.setPassword("Password123");
        account.setConfirmPassword("Password123");
        account.setFamilyName("family name");
        account.setGivenName("Given Name");
        account.setGender("M");
        account.setPrivilegeLevel(fullPrivileges);
        account.getUser().setSystemId("systemId");
        account.setCapabilities(someCapabilitySet);
        account.setProviderRole(someProviderRole);
    }
}
