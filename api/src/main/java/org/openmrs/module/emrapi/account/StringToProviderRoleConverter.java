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

import org.apache.commons.lang3.StringUtils;
import org.openmrs.ProviderRole;
import org.openmrs.api.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts String to ProviderRole. Ideally this would be in core.
 */
@Component
public class StringToProviderRoleConverter implements Converter<String, ProviderRole> {
	
	@Autowired
	@Qualifier("providerService")
	public ProviderService providerService;
	
	/**
	 * Retrieves a given provider role from a string representing either the id or uuid of the
	 * ProviderRole
	 */
	@Override
	public ProviderRole convert(String id) {
		if (StringUtils.isBlank(id)) {
			return null;
		}
		ProviderRole role = providerService.getProviderRoleByUuid(id);
		if (role == null) {
			role = providerService.getProviderRole(Integer.valueOf(id));
		}
		return role;
	}
}
