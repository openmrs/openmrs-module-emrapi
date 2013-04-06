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
import org.openmrs.LocationAttributeType;
import org.openmrs.Role;
import org.openmrs.api.LocationService;
import org.openmrs.api.UserService;
import org.openmrs.module.emrapi.printer.Printer;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class EmrApiActivatorComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private UserService userService;

    @Autowired
    private LocationService locationService;

    @Before
    public void setUp() throws Exception {
        EmrApiActivator activator = new EmrApiActivator();
        activator.willRefreshContext();
        activator.contextRefreshed();
        activator.willStart();
        activator.started();
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
    public void confirmThatLocationAttributeTypesHaveBeenCreated() {
        EmrApiActivator activator = new EmrApiActivator();
        activator.willStart();
        activator.started();

        LocationAttributeType defaultIdCardPrinter = locationService.getLocationAttributeTypeByUuid(EmrApiConstants.LOCATION_ATTRIBUTE_TYPE_DEFAULT_PRINTER.get(Printer.Type.ID_CARD.name()));
        LocationAttributeType defaultLabelPrinter = locationService.getLocationAttributeTypeByUuid(EmrApiConstants.LOCATION_ATTRIBUTE_TYPE_DEFAULT_PRINTER.get(Printer.Type.LABEL.name()));
        LocationAttributeType nameToPrintOnIdCard = locationService.getLocationAttributeTypeByUuid(EmrApiConstants.LOCATION_ATTRIBUTE_TYPE_NAME_TO_PRINT_ON_ID_CARD);

        assertThat(defaultIdCardPrinter, is(notNullValue()));
        assertThat(defaultLabelPrinter, is(notNullValue()));
        assertThat(nameToPrintOnIdCard, is(notNullValue()));
    }

}
