/*
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

package org.openmrs.module.emrapi.patient;

import org.junit.jupiter.api.Test;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 *
 */
public class PatientDomainWrapperComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private DomainWrapperFactory factory;

    @Test
    public void testThatBeanCanHavePropertiesAutowired() throws Exception {
        PatientDomainWrapper patientDomainWrapper = factory.newPatientDomainWrapper();
        assertThat(patientDomainWrapper.emrApiProperties, notNullValue());
    }

}

