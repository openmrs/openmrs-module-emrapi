/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.encounter;

import org.junit.jupiter.api.Test;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

public class EncounterDomainWrapperComponentTest extends BaseModuleContextSensitiveTest {
	
	@Autowired
	private DomainWrapperFactory factory;
	
	@Test
	public void testThatBeanCanHavePropertiesAutowired() throws Exception {
		EncounterDomainWrapper encounterDomainWrapper = factory.newEncounterDomainWrapper();
		// currently no beans are actually wired in--adding this so we remember to test it later
	}
	
}
