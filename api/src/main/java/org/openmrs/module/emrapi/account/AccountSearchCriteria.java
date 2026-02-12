package org.openmrs.module.emrapi.account;

import lombok.Data;
import org.openmrs.ProviderRole;

import java.util.List;

/**
 * Represents criteria for searching for Accounts
 */
@Data
public class AccountSearchCriteria {

    private String nameOrIdentifier;
    private Boolean hasUser;
    private Boolean hasProvider;
    private List<ProviderRole> providerRoles;

}
