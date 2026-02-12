package org.openmrs.module.emrapi.rest.resource;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Person;
import org.openmrs.ProviderRole;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.account.AccountDomainWrapper;
import org.openmrs.module.emrapi.account.AccountSearchCriteria;
import org.openmrs.module.emrapi.account.AccountService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Resource(name = RestConstants.VERSION_1 + "/account", supportedClass = AccountDomainWrapper.class, supportedOpenmrsVersions = { "2.8 - 9.*" })
public class AccountResource extends DelegatingCrudResource<AccountDomainWrapper> {

    private AccountService getAccountService() {
        return Context.getService(AccountService.class);
    }

    @Override
    public AccountDomainWrapper getByUniqueId(String personUuid) {
        Person person = Context.getPersonService().getPersonByUuid(personUuid);
        if (person == null) {
            return null;
        }
        return getAccountService().getAccountByPerson(person);
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        AccountSearchCriteria searchCriteria = new AccountSearchCriteria();
        String nameOrIdentifier = context.getParameter("q");
        if (StringUtils.isNotBlank(nameOrIdentifier)) {
            searchCriteria.setNameOrIdentifier(nameOrIdentifier);
        }
        String hasUserParam = context.getParameter("hasUser");
        if (StringUtils.isNotBlank(hasUserParam)) {
            searchCriteria.setHasUser(Boolean.parseBoolean(hasUserParam));
        }
        String hasProviderParam = context.getParameter("hasProvider");
        if (StringUtils.isNotBlank(hasProviderParam)) {
            searchCriteria.setHasProvider(Boolean.parseBoolean(hasProviderParam));
        }
        String providerRolesParam = context.getParameter("providerRoles");
        if (StringUtils.isNotBlank(providerRolesParam)) {
            List<ProviderRole> providerRoles = new ArrayList<>();
            for (String roleUuid : providerRolesParam.split(",")) {
                ProviderRole role = Context.getProviderService().getProviderRoleByUuid(roleUuid);
                providerRoles.add(role);
            }
            searchCriteria.setProviderRoles(providerRoles);
        }
        List<AccountDomainWrapper> accounts = getAccountService().getAccounts(searchCriteria);
        return new NeedsPaging<>(accounts, context);
    }

    @Override
    protected PageableResult doGetAll(RequestContext context) throws ResponseException {
        List<AccountDomainWrapper> accounts =  getAccountService().getAllAccounts();
        return new NeedsPaging<>(accounts, context);
    }

    @Override
    protected void delete(AccountDomainWrapper accountDomainWrapper, String s, RequestContext requestContext) throws ResponseException {
        throw new NotImplementedException();  // TODO
    }

    @Override
    public void purge(AccountDomainWrapper accountDomainWrapper, RequestContext requestContext) throws ResponseException {
        throw new NotImplementedException();  // TODO
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("givenName");
        description.addProperty("familyName");
        description.addProperty("gender");
        description.addProperty("username");
        description.addProperty("password");
        description.addProperty("confirmPassword");
        description.addProperty("passwordChangeRequired");
        description.addProperty("email");
        description.addProperty("phoneNumber");
        description.addProperty("defaultLocale");
        description.addProperty("privilegeLevel", Representation.REF);
        description.addProperty("capabilities", Representation.REF);
        description.addProperty("userEnabled");
        description.addProperty("locked");
        description.addProperty("providerRole", Representation.REF);
        return description;
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        if (rep == null) {
            return null;
        }
        DelegatingResourceDescription d = new  DelegatingResourceDescription();
        d.addProperty("uuid");
        d.addProperty("display");
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            Map<String, DelegatingResourceDescription.Property> props = getCreatableProperties().getProperties();
            for (String property : props.keySet()) {
                d.addProperty(property, props.get(property).getRep());
            }
            if (rep instanceof DefaultRepresentation) {
                d.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            }
            else {
                d.addProperty("person", Representation.REF);
                d.addProperty("user", Representation.REF);
                d.addProperty("provider", Representation.REF);
            }
        }
        d.addSelfLink();
        return d;
    }

    @PropertyGetter("uuid")
    public String getUuid(AccountDomainWrapper accountDomainWrapper) {
        return accountDomainWrapper.getPerson().getUuid();
    }

    @PropertyGetter("display")
    public String getDisplay(AccountDomainWrapper accountDomainWrapper) {
        return accountDomainWrapper.getPerson().getPersonName().getFullName();
    }

    @Override
    public AccountDomainWrapper newDelegate() {
        return getAccountService().getAccountByPerson(new Person());
    }

    @Override
    public AccountDomainWrapper save(AccountDomainWrapper accountDomainWrapper) {
        getAccountService().saveAccount(accountDomainWrapper);
        return accountDomainWrapper;
    }
}
