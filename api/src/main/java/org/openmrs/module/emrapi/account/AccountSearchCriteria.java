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
    private Boolean hasProviderRole;
    private List<ProviderRole> providerRoles;
    private Boolean userEnabled;
    private Integer startIndex;
    private Integer limit;
}
