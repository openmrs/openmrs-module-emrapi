package org.openmrs.module.emrapi.account;

import org.apache.commons.lang.StringUtils;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.api.PasswordException;
import org.openmrs.api.UserService;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.providermanagement.api.ProviderManagementService;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Handler(supports = {AccountDomainWrapper.class}, order = 50)
public class AccountValidator implements Validator {

    @Autowired
    @Qualifier("messageSourceService")
    private MessageSourceService messageSourceService;

    @Autowired
    @Qualifier("userService")
    private UserService userService;

    @Autowired
    @Qualifier("providerManagementService")
    private ProviderManagementService providerManagementService;

    public static final String USERNAME_MIN_LENGTH = "2";
    public static final String USERNAME_MAX_LENGTH = "50";

    /**
     * @param messageSourceService the messageSourceService to set
     */
    public void setMessageSourceService(MessageSourceService messageSourceService) {
        this.messageSourceService = messageSourceService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setProviderManagementService(ProviderManagementService providerManagementService) {
        this.providerManagementService = providerManagementService;
    }

    /**
     * @see org.springframework.validation.Validator#supports(java.lang.Class)
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return AccountDomainWrapper.class.isAssignableFrom(clazz);
    }

    /**
     * @should reject an empty username if user is not null
     * @should reject an empty givenname
     * @should reject an empty familyname
     * @should reject an empty privilegeLevel if user is not null
     * @should require password if conform password is provided
     * @should require confirm password if password is provided
     * @should reject password and confirm password if they dont match
     * @should pass for a valid account
     * @should pass for a valid account with only person property
     * @should require passwords for a new a user account
     * @see org.springframework.validation.Validator#validate(java.lang.Object,
     *      org.springframework.validation.Errors)
     */
    @Override
    public void validate(Object obj, Errors errors) {
        if (obj == null || !(obj instanceof AccountDomainWrapper))
            throw new IllegalArgumentException("The parameter obj should not be null and must be of type" + AccountDomainWrapper.class);

        AccountDomainWrapper account = (AccountDomainWrapper) obj;

        User user = account.getUser();

        checkIfGivenAndFamilyNameAreNotNull(errors, account);
        checkIfGenderIsNull(errors, account);

        // TODO: we are (probably just temporarily) insisting that all accounts have an associated provider
        //checkIfUserAndProviderRoleAreNull(errors, account);
        checkIfProviderRoleIsNull(errors, account);

        if (account.getUser() != null) {
            checkIfUserNameIsCorrect(errors, account.getUsername());
            checkIfDuplicateUsername(errors, account.getUser());
            checkIfPrivilegeLevelIsCorrect(errors, account);
            checkIfNoCapabilities(errors, account);
        }

        if (checkIfUserWasCreated(user) || StringUtils.isNotBlank(account.getPassword()) || StringUtils.isNotBlank(account.getConfirmPassword())) {
            checkIfPasswordIsCorrect(errors, account);
        }
    }

    private void checkIfPrivilegeLevelIsCorrect(Errors errors, AccountDomainWrapper account) {
        if (account.getPrivilegeLevel() == null) {
            errors.rejectValue("privilegeLevel", "error.required",
                    new Object[]{messageSourceService.getMessage("emr.user.privilegeLevel")}, null);
        }
    }

    private void checkIfPasswordIsCorrect(Errors errors, AccountDomainWrapper account) {
        String password = account.getPassword();
        String confirmPassword = account.getConfirmPassword();

        if (checkIfPasswordWasCreated(password, confirmPassword)) {
            validatePassword(errors, account, password, confirmPassword);
        } else {
            errors.rejectValue("password", "error.required",
                    new Object[]{messageSourceService.getMessage("emr.user.password")}, null);
            errors.rejectValue("confirmPassword", "error.required",
                    new Object[]{messageSourceService.getMessage("emr.user.confirmPassword")}, null);
        }

    }

    private void validatePassword(Errors errors, AccountDomainWrapper account, String password, String confirmPassword) {
        if (password.equals(confirmPassword)) {
            getErrorInPassword(errors, account);
        } else {
            errors.rejectValue("password", "emr.account.error.passwordDontMatch",
                    new Object[]{messageSourceService.getMessage("emr.user.password")}, null);
        }
    }

    private void getErrorInPassword(Errors errors, AccountDomainWrapper account) {
        try {
            OpenmrsUtil.validatePassword(account.getUsername(), account.getPassword(), account.getUser().getSystemId());
        } catch (PasswordException e) {
            errors.rejectValue("password", "emr.account.error.passwordError",
                    new Object[]{messageSourceService.getMessage("emr.account.error.passwordError")}, null);
        }
    }


    private boolean checkIfPasswordWasCreated(String password, String confirmPassword) {
        return (StringUtils.isNotBlank(password) && StringUtils.isNotBlank(confirmPassword));
    }

    private boolean checkIfUserWasCreated(User user) {
        return (user != null && user.getUserId() == null);
    }

    private void checkIfUserNameIsCorrect(Errors errors, String username) {
        if (StringUtils.isNotBlank(username)) {
            if (!username.matches("[A-Za-z0-9\\._\\-]{" + USERNAME_MIN_LENGTH + "," + USERNAME_MAX_LENGTH + "}")) {
                errors.rejectValue("username", "emr.user.username.error",
                        new Object[]{messageSourceService.getMessage("emr.user.username.error")}, null);
            }

        } else {
            errors.rejectValue("username", "error.required",
                    new Object[]{messageSourceService.getMessage("emr.user.username")}, null);
        }

    }

    private void checkIfDuplicateUsername(Errors errors, User user) {
        if (userService.hasDuplicateUsername(user)) {
            errors.rejectValue("username", "emr.user.duplicateUsername",
                    new Object[]{messageSourceService.getMessage("emr.user.duplicateUsername")}, null);
        }
    }

    private void checkIfGivenAndFamilyNameAreNotNull(Errors errors, AccountDomainWrapper account) {
        if (StringUtils.isBlank(account.getGivenName())) {
            errors.rejectValue("givenName", "error.required",
                    new Object[]{messageSourceService.getMessage("emr.person.givenName")}, null);
        }
        if (StringUtils.isBlank(account.getFamilyName())) {
            errors.rejectValue("familyName", "error.required",
                    new Object[]{messageSourceService.getMessage("emr.person.familyName")}, null);
        }
    }

    private void checkIfGenderIsNull(Errors errors, AccountDomainWrapper account) {
        if (StringUtils.isBlank(account.getGender())) {
            errors.rejectValue("gender", "error.required",
                    new Object[]{messageSourceService.getMessage("emr.gender")}, null);
        }
    }

    private void checkIfNoCapabilities(Errors errors, AccountDomainWrapper account) {
        if (account.getCapabilities() == null || account.getCapabilities().size() == 0) {
            errors.rejectValue("capabilities", "emr.user.Capabilities.required",
                    new Object[]{messageSourceService.getMessage("emr.user.Capabilities.required")}, null);
        }
    }

    private void checkIfUserAndProviderRoleAreNull(Errors errors, AccountDomainWrapper accountDomainWrapper) {
        if (accountDomainWrapper.getUser() == null && accountDomainWrapper.getProviderRole() == null) {
            errors.reject("emr.account.requiredFields");
        }
    }

    private void checkIfProviderRoleIsNull(Errors errors, AccountDomainWrapper accountDomainWrapper) {
        if (accountDomainWrapper.getProviderRole() == null) {
            errors.rejectValue("providerRole", "error.required",
                    new Object[]{messageSourceService.getMessage("emr.account.providerRole.label")}, null);
        }
    }
}
