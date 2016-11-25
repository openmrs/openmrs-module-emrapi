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

package org.openmrs.module.emrapi;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.UserService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class EmrApiActivatorComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    @Qualifier("adminService")
    private AdministrationService adminService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Before
    public void setUp() throws Exception {
        executeDataSet("baseTestDataset.xml");
    }

    @Test
    public void testPrivilegeLevelsCreated() throws Exception {
        EmrApiActivator activator = new EmrApiActivator();
        activator.willRefreshContext();
        activator.contextRefreshed();

        // ensure Privilege Level: Full role
        Role fullPrivsRole = userService.getRole(EmrApiConstants.PRIVILEGE_LEVEL_FULL_ROLE);
        assertThat(fullPrivsRole, is(notNullValue()));
        assertThat(fullPrivsRole.getUuid(), is(EmrApiConstants.PRIVILEGE_LEVEL_FULL_UUID));
    }

    @Test
    public void confirmThatUnknownProviderCreated() {
        EmrApiActivator activator = new EmrApiActivator();
        activator.willRefreshContext();
        activator.contextRefreshed();
        activator.started();

        Provider unknownProvider = emrApiProperties.getUnknownProvider();

        assertNotNull(unknownProvider);
        assertNotNull(unknownProvider.getPerson());
        assertThat(unknownProvider.getIdentifier(), is("UNKNOWN"));
        assertThat(unknownProvider.getPerson().getGivenName(), is("Unknown"));
        assertThat(unknownProvider.getPerson().getFamilyName(), is("Provider"));

        // check that the global properties have been properly set
        assertThat(adminService.getGlobalProperty(EmrApiConstants.GP_UNKNOWN_PROVIDER),
                is("f9badd80-ab76-11e2-9e96-0800200c9a66"));
        assertThat(adminService.getGlobalProperty("provider.unknownProviderUuid"),
                is("f9badd80-ab76-11e2-9e96-0800200c9a66"));
    }

    @Test
    public void shouldSetCoreUnknownProviderGPIfProviderAlreadyExistsForEmrApiGlobalProperty() {

        // setup, by setting the emr api global property
        GlobalProperty emrApiUnknownProviderUuid = adminService.getGlobalPropertyObject(EmrApiConstants.GP_UNKNOWN_PROVIDER);
        emrApiUnknownProviderUuid.setPropertyValue("c2299800-cca9-11e0-9572-0800200c9a66");  // this uuid is provider #1 in the standard test dataset
        adminService.saveGlobalProperty(emrApiUnknownProviderUuid);

        EmrApiActivator activator = new EmrApiActivator();
        activator.willRefreshContext();
        activator.contextRefreshed();
        activator.started();

        Provider unknownProvider = emrApiProperties.getUnknownProvider();
        assertNotNull(unknownProvider);
        assertThat(unknownProvider.getIdentifier(), is("Test"));   // provider #1 from the standard test dataset

        // check that the global properties have been properly set
        assertThat(adminService.getGlobalProperty(EmrApiConstants.GP_UNKNOWN_PROVIDER),
                is("c2299800-cca9-11e0-9572-0800200c9a66"));
        assertThat(adminService.getGlobalProperty("provider.unknownProviderUuid"),
                is("c2299800-cca9-11e0-9572-0800200c9a66"));

    }

    @Test
    public void shouldSetEmrApiUnknownProviderGPIfProviderAlreadyExistsForCoreGlobalProperty() {

        // setup, by setting the emr api global property
        GlobalProperty coreUnknownProviderUuid = adminService.getGlobalPropertyObject(EmrApiConstants.GP_UNKNOWN_PROVIDER);
        coreUnknownProviderUuid.setPropertyValue("c2299800-cca9-11e0-9572-0800200c9a66");  // this uuid is provider #1 in the standard test dataset
        adminService.saveGlobalProperty(coreUnknownProviderUuid);

        EmrApiActivator activator = new EmrApiActivator();
        activator.willRefreshContext();
        activator.contextRefreshed();
        activator.started();

        Provider unknownProvider = emrApiProperties.getUnknownProvider();
        assertNotNull(unknownProvider);
        assertThat(unknownProvider.getIdentifier(), is("Test"));   // provider #1 from the standard test dataset

        // check that the global properties have been properly set
        assertThat(adminService.getGlobalProperty(EmrApiConstants.GP_UNKNOWN_PROVIDER),
                is("c2299800-cca9-11e0-9572-0800200c9a66"));
        assertThat(adminService.getGlobalProperty("provider.unknownProviderUuid"),
                is("c2299800-cca9-11e0-9572-0800200c9a66"));

    }

}
