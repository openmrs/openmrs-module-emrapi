/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.account;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts String to AccountDomainWrapper, interpreting it as a person id
 */
@Component
public class StringToAccountDomainWrapperConverter implements Converter<String, AccountDomainWrapper> {
	
	@Autowired
	private AccountService accountService;
	
	/**
	 * @see org.springframework.core.convert.converter.Converter#convert(Object)
	 */
	@Override
	public AccountDomainWrapper convert(String personId) {
		if (StringUtils.isNotBlank(personId))
			return accountService.getAccount(Integer.valueOf(personId));
		return null;
	}
}
