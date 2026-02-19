package org.openmrs.module.emrapi.account;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AccountSearchResult {
    Long totalCount;
    List<AccountDomainWrapper> accounts = new ArrayList<>();
}
