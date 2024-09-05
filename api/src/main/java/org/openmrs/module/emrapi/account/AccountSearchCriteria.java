package org.openmrs.module.emrapi.account;

import lombok.Data;

/**
 * Represents criteria for searching for Accounts
 */
@Data
public class AccountSearchCriteria {

    private String nameOrIdentifier;

}
