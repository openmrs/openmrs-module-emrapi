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
import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.UserService;
import org.openmrs.module.metadatamapping.MetadataSource;
import org.openmrs.module.metadatamapping.MetadataTermMapping;
import org.openmrs.module.metadatamapping.api.MetadataMappingService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
    private MetadataMappingService metadataMappingService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Before
    public void setUp() throws Exception {
        executeDataSet("activatorTestDataset.xml");
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

        // ensure Privilege Level: High role
        Role highPrivsRole = userService.getRole(EmrApiConstants.PRIVILEGE_LEVEL_HIGH_ROLE);
        assertThat(highPrivsRole, is(notNullValue()));
        assertThat(highPrivsRole.getUuid(), is(EmrApiConstants.PRIVILEGE_LEVEL_HIGH_UUID));
    }

    @Test
    public void testMetadataMappingsCreated() throws Exception {
        EmrApiActivator activator = new EmrApiActivator();
        activator.willRefreshContext();
        activator.contextRefreshed();
        activator.started();

        /**
         * check in encounter type and appropriate global property are created in activator test dataset,
         * activator should find it out and map metadata
         */
        EncounterType checkInEncounterType = emrApiProperties.getCheckInEncounterType();

        EncounterType checkInEncounterTypeByMapping = metadataMappingService.getMetadataItem(
                EncounterType.class,
                EmrApiConstants.EMR_METADATA_SOURCE_NAME,
                EmrApiConstants.GP_CHECK_IN_ENCOUNTER_TYPE);

        assertNotNull(checkInEncounterTypeByMapping);
        assertNotNull(checkInEncounterType);
        assertEquals(checkInEncounterType, checkInEncounterTypeByMapping);

        /**
         * when there is no mapping and no global property with specified value,
         * activator should create mapping without mapped object
         */
        MetadataSource metadataSource = metadataMappingService.getMetadataSourceByName(EmrApiConstants.EMR_METADATA_SOURCE_NAME);
        MetadataTermMapping admissionMapping = metadataMappingService.getMetadataTermMapping(metadataSource, EmrApiConstants.GP_ADMISSION_ENCOUNTER_TYPE);
        assertNotNull(admissionMapping);
        assertNull(admissionMapping.getMetadataUuid());
        assertEquals(admissionMapping.getMetadataClass(), "org.openmrs.EncounterType");

        /**
         * when there is already mapping and global property with specified value,
         * activator should do nothing, we assume that after mapping replaces GP, changes in GP are ignored
         */
        MetadataTermMapping exitFromInpatientMapping = metadataMappingService.getMetadataTermMapping(metadataSource, EmrApiConstants.GP_EXIT_FROM_INPATIENT_ENCOUNTER_TYPE);
        assertNotNull(exitFromInpatientMapping);
        assertNull(exitFromInpatientMapping.getMetadataUuid());
    }
    @Test
    public void testExtraPatientIdentifierTypesMappedToSet(){
        EmrApiActivator activator = new EmrApiActivator();
        List<PatientIdentifierType> typesFromGP = activator.getPatientIdentifierTypesFromGlobalProperty(adminService, EmrApiConstants.GP_EXTRA_PATIENT_IDENTIFIER_TYPES);

        activator.willRefreshContext();
        activator.contextRefreshed();
        activator.started();

        List<PatientIdentifierType> typesFromMappingSet = emrApiProperties.getExtraPatientIdentifierTypes();
        assertThat(typesFromGP, is(typesFromMappingSet));
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

        //direct check of mapping
        MetadataSource source = metadataMappingService.getMetadataSourceByName(EmrApiConstants.EMR_METADATA_SOURCE_NAME);
        MetadataTermMapping mapping = metadataMappingService.getMetadataTermMapping(source, EmrApiConstants.GP_UNKNOWN_PROVIDER);
        assertNotNull(mapping);
        assertThat(mapping.getCode(), is(EmrApiConstants.GP_UNKNOWN_PROVIDER));
    }


    @Test
    public void confirmThatMetadataSourceIsCreated() {
        EmrApiActivator activator = new EmrApiActivator();
        activator.willRefreshContext();
        activator.contextRefreshed();
        activator.started();

        MetadataSource emrapiSource = metadataMappingService.getMetadataSourceByName(EmrApiConstants.EMR_METADATA_SOURCE_NAME);
        assertNotNull(emrapiSource);
    }

    @Test
    public void shouldCreateMappingIfProviderAlreadyExistsForEmrApiGlobalProperty() {

        // setup, by setting the emr api global property
        GlobalProperty emrApiUnknownProviderUuid = adminService.getGlobalPropertyObject(EmrApiConstants.GP_UNKNOWN_PROVIDER);
        emrApiUnknownProviderUuid.setPropertyValue("c2299800-cca9-11e0-9572-0800200c9a66");  // this uuid is provider #1 in the standard test dataset
        adminService.saveGlobalProperty(emrApiUnknownProviderUuid);

        checkIfMappingFromGPCreated();
    }

    @Test
    public void shouldCreateMappingIfProviderAlreadyExistsForCoreGlobalProperty() {

        // setup, by setting the emr api global property
        GlobalProperty coreUnknownProviderUuid = adminService.getGlobalPropertyObject(EmrApiConstants.GP_UNKNOWN_PROVIDER);
        coreUnknownProviderUuid.setPropertyValue("c2299800-cca9-11e0-9572-0800200c9a66");  // this uuid is provider #1 in the standard test dataset
        adminService.saveGlobalProperty(coreUnknownProviderUuid);

        checkIfMappingFromGPCreated();
    }

    private void checkIfMappingFromGPCreated() {
        EmrApiActivator activator = new EmrApiActivator();
        activator.willRefreshContext();
        activator.contextRefreshed();
        activator.started();

        Provider unknownProvider = emrApiProperties.getUnknownProvider();

        assertNotNull(unknownProvider);
        assertThat(unknownProvider.getUuid(), is("c2299800-cca9-11e0-9572-0800200c9a66"));
    }
}
