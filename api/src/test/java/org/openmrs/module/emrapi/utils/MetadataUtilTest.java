/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.emrapi.utils;

import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MetadataUtilTest extends BaseModuleContextSensitiveTest {
	
	@Test
	public void testSetupStandardMetadata() throws Exception {
        assertThat(Context.getVisitService().getVisitTypeByUuid("86b3d7bc-d91f-4ce2-991c-f71bba0b31e4"), nullValue());

        boolean anyChanges = MetadataUtil.setupStandardMetadata(getClass().getClassLoader());

        assertTrue(anyChanges);
        assertThat(Context.getVisitService().getVisitTypeByUuid("86b3d7bc-d91f-4ce2-991c-f71bba0b31e4").getName(), is("Clinic or Hospital Visit"));
	}
}
