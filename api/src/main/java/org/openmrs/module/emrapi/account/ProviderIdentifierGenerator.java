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

import org.openmrs.Provider;

/**
 * Implementers can provide implementations of this interface if they wish to automatically create
 * provider identifiers; custom implementations should be injected into the AccountService;
 * <p/>
 * When saving an Account, if there is an associated Provider and that Provider does not have an
 * identifier, the generateIdentifier method will be called and the result set as the
 * provider.identifier
 * <p/>
 * See MirebalaisProviderIdentifierGenerator for an example implementation; check out the
 * MirebalaisHospitalActivator to see how this generator is set on the AccountService
 */
public interface ProviderIdentifierGenerator {
	
	String generateIdentifier(Provider provider);
	
}
