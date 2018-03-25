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
package org.openmrs.module.emrapi;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptSource;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.GlobalProperty;
import org.openmrs.Location;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.VisitType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.event.Event;
import org.openmrs.event.EventListener;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.DaemonToken;
import org.openmrs.module.DaemonTokenAware;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.ModuleException;
import org.openmrs.module.emrapi.account.AccountService;
import org.openmrs.module.emrapi.adt.EmrApiVisitAssignmentHandler;
import org.openmrs.module.emrapi.event.PatientViewedEventListener;
import org.openmrs.module.metadatamapping.MetadataSet;
import org.openmrs.module.metadatamapping.MetadataSource;
import org.openmrs.module.metadatamapping.MetadataTermMapping;
import org.openmrs.module.metadatamapping.api.MetadataMappingService;
import org.openmrs.module.metadatamapping.util.GlobalPropertyToMappingConverter;
import org.openmrs.util.OpenmrsConstants;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class EmrApiActivator extends BaseModuleActivator implements DaemonTokenAware {

    protected final Log log = LogFactory.getLog(getClass());

    private EventListener eventListener;

    private DaemonToken daemonToken;

    private AdministrationService administrationService;

    private ProviderService providerService;

    private PersonService personService;

    private ConceptService conceptService;

    private MetadataMappingService metadataMappingService;

    /**
     * @see ModuleActivator#contextRefreshed()
     */
    @Override
    public void contextRefreshed() {
        super.contextRefreshed();    //To change body of overridden methods use File | Settings | File Templates.
        ensurePrivilegeLevelRoles();
    }

    /**
     * Creates role "Privilege Level: Full" if does not exist
     *
     * @return
     */
    private void ensurePrivilegeLevelRoles() {
        UserService userService = Context.getUserService();
        AccountService accountService = Context.getService(AccountService.class);
        EmrApiProperties emrProperties = Context.getRegisteredComponents(EmrApiProperties.class).iterator().next();

        Role fullPrivilegeLevel = emrProperties.getFullPrivilegeLevel();
        Role highPrivilegeLevel = emrProperties.getHighPrivilegeLevel();

        if (fullPrivilegeLevel == null) {
            fullPrivilegeLevel = new Role();
            fullPrivilegeLevel.setRole(EmrApiConstants.PRIVILEGE_LEVEL_FULL_ROLE);
            fullPrivilegeLevel.setDescription(EmrApiConstants.PRIVILEGE_LEVEL_FULL_DESCRIPTION);
            fullPrivilegeLevel.setUuid(EmrApiConstants.PRIVILEGE_LEVEL_FULL_UUID);
            userService.saveRole(fullPrivilegeLevel);
        }

        if (highPrivilegeLevel == null) {
            highPrivilegeLevel = new Role();
            highPrivilegeLevel.setRole(EmrApiConstants.PRIVILEGE_LEVEL_HIGH_ROLE);
            highPrivilegeLevel.setDescription(EmrApiConstants.PRIVILEGE_LEVEL_HIGH_DESCRIPTION);
            highPrivilegeLevel.setUuid(EmrApiConstants.PRIVILEGE_LEVEL_HIGH_UUID);
            userService.saveRole(highPrivilegeLevel);
        }

        for (Privilege candidate : accountService.getApiPrivileges()) {

            if (!fullPrivilegeLevel.hasPrivilege(candidate.getName())) {
                fullPrivilegeLevel.addPrivilege(candidate);
            }

            if (!highPrivilegeLevel.hasPrivilege(candidate.getName()) && !EmrApiConstants.UNSAFE_PRIVILEGES.contains(candidate.getName())) {
                highPrivilegeLevel.addPrivilege(candidate);
            }
        }

        userService.saveRole(fullPrivilegeLevel);
        userService.saveRole(highPrivilegeLevel);
    }

    @Override
    public void started() {
        super.started();

        administrationService = Context.getAdministrationService();
        providerService = Context.getProviderService();
        personService = Context.getPersonService();
        conceptService = Context.getConceptService();
        metadataMappingService = Context.getService(MetadataMappingService.class);

        createMissingMetadataMappings();
        createUnknownProvider();

        administrationService.setGlobalProperty(OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER, EmrApiVisitAssignmentHandler.class.getName());
        createConceptSource(conceptService);
        eventListener = new PatientViewedEventListener(daemonToken);
        Event.subscribe(EmrApiConstants.EVENT_TOPIC_NAME_PATIENT_VIEWED, eventListener);

        createPersonImageFolder();
    }

    /**
     * Creates missing mappings for metadata necessary for module functioning. MetadataTermMapping codes match global properties keys.
     * Note that if mapping already has been created, and global property is edited, these changes will not be reflected in module metadata
     * @should do nothing, if mapping is not missing
     * @should create new mapping without mapped object, if mapping is missing and there is no global property or it has value not matching any OpenmrsMetadata uuid
     * @should create new mapping with mapped object, if mapping is missing and there is global property with uuid matching existing OpenmrsMetadata
     */
    private void createMissingMetadataMappings() {
        //create main metadata source
        MetadataSource emrapiMetadataSource = saveMetadataSourceIfMissing(EmrApiConstants.EMR_METADATA_SOURCE_NAME, EmrApiConstants.EMR_METADATA_SOURCE_DESCRIPTION);

        createExtraPatientIdTypesSetIfMissing(emrapiMetadataSource);

        new GlobalPropertyToMappingConverter<Location>(emrapiMetadataSource) {
            public Location getMetadataByUuid(String s) {
                return Context.getLocationService().getLocationByUuid(s);
            }
        }.convert(EmrApiConstants.GP_UNKNOWN_LOCATION);

        new GlobalPropertyToMappingConverter<PatientIdentifierType>(emrapiMetadataSource) {
            @Override
            public PatientIdentifierType getMetadataByUuid(String uuid) {
                return Context.getPatientService().getPatientIdentifierTypeByUuid(uuid);
            }
        }.convert(EmrApiConstants.PRIMARY_IDENTIFIER_TYPE);

        new GlobalPropertyToMappingConverter<VisitType>(emrapiMetadataSource) {
            @Override
            public VisitType getMetadataByUuid(String uuid) {
                return Context.getVisitService().getVisitTypeByUuid(uuid);
            }
        }.convert(EmrApiConstants.GP_AT_FACILITY_VISIT_TYPE);

        GlobalPropertyToMappingConverter<EncounterRole> encounterRoleHelper = new GlobalPropertyToMappingConverter<EncounterRole>(emrapiMetadataSource){
            @Override
            public EncounterRole getMetadataByUuid(String uuid) {
                return Context.getEncounterService().getEncounterRoleByUuid(uuid);
            }
        };

        encounterRoleHelper.convert(EmrApiConstants.GP_ORDERING_PROVIDER_ENCOUNTER_ROLE);
        encounterRoleHelper.convert(EmrApiConstants.GP_CHECK_IN_CLERK_ENCOUNTER_ROLE);
        encounterRoleHelper.convert(EmrApiConstants.GP_CLINICIAN_ENCOUNTER_ROLE);

        GlobalPropertyToMappingConverter<EncounterType> encounterTypeHelper = new GlobalPropertyToMappingConverter<EncounterType>(emrapiMetadataSource) {
            @Override
            public EncounterType getMetadataByUuid(String uuid) {
                return Context.getEncounterService().getEncounterTypeByUuid(uuid);
            }
        };

        encounterTypeHelper.convert(EmrApiConstants.GP_CHECK_IN_ENCOUNTER_TYPE);
        encounterTypeHelper.convert(EmrApiConstants.GP_CONSULT_ENCOUNTER_TYPE);
        encounterTypeHelper.convert(EmrApiConstants.GP_VISIT_NOTE_ENCOUNTER_TYPE);
        encounterTypeHelper.convert(EmrApiConstants.GP_ADMISSION_ENCOUNTER_TYPE);
        encounterTypeHelper.convert(EmrApiConstants.GP_EXIT_FROM_INPATIENT_ENCOUNTER_TYPE);
        encounterTypeHelper.convert(EmrApiConstants.GP_TRANSFER_WITHIN_HOSPITAL_ENCOUNTER_TYPE);

        GlobalPropertyToMappingConverter<Form> formHelper = new GlobalPropertyToMappingConverter<Form>(emrapiMetadataSource) {
            @Override
            public Form getMetadataByUuid(String uuid) {
                return Context.getFormService().getFormByUuid(uuid);
            }
        };

        formHelper.convert(EmrApiConstants.GP_ADMISSION_FORM);
        formHelper.convert(EmrApiConstants.GP_EXIT_FROM_INPATIENT_FORM);
        formHelper.convert(EmrApiConstants.GP_TRANSFER_WITHIN_HOSPITAL_FORM);
    }

    private void createExtraPatientIdTypesSetIfMissing(MetadataSource emrapiMetadataSource) {
        MetadataTermMapping extraPatientIdTypesMapping = metadataMappingService.getMetadataTermMapping(emrapiMetadataSource, EmrApiConstants.GP_EXTRA_PATIENT_IDENTIFIER_TYPES);

        if(extraPatientIdTypesMapping == null){
            MetadataSet extraPatientIdTypesSet = metadataMappingService.saveMetadataSet(new MetadataSet());

            List<PatientIdentifierType> types = getPatientIdentifierTypesFromGlobalProperty(administrationService, EmrApiConstants.GP_EXTRA_PATIENT_IDENTIFIER_TYPES);

            for(PatientIdentifierType type : types){
                metadataMappingService.saveMetadataSetMember(extraPatientIdTypesSet, type);
            }

            extraPatientIdTypesMapping = new MetadataTermMapping(emrapiMetadataSource, EmrApiConstants.GP_EXTRA_PATIENT_IDENTIFIER_TYPES, extraPatientIdTypesSet);
            extraPatientIdTypesMapping.setName(EmrApiConstants.GP_EXTRA_PATIENT_IDENTIFIER_TYPES);
            metadataMappingService.saveMetadataTermMapping(extraPatientIdTypesMapping);
        }
    }
    
    private MetadataSource saveMetadataSourceIfMissing(String name, String description){
        MetadataSource source = metadataMappingService.getMetadataSourceByName(name);
        if(source == null){
            source = new MetadataSource();
            source.setName(name);
            source.setDescription(description);
            metadataMappingService.saveMetadataSource(source);
        }
        return source;
    }

    /**
     * it is protected and uses AdministrationService from argument to make testing easier
     */
    protected List<PatientIdentifierType> getPatientIdentifierTypesFromGlobalProperty(AdministrationService administrationService, String gpKey) {
        List<PatientIdentifierType> types = new ArrayList<PatientIdentifierType>();
        String globalProperty = administrationService.getGlobalProperty(gpKey);
        if (StringUtils.isNotEmpty(globalProperty)) {
            for (String type : globalProperty.split(",")) {
                PatientIdentifierType patientIdentifierType = Context.getPatientService().getPatientIdentifierTypeByUuid(type.trim());
                if (patientIdentifierType != null) {
                    types.add(patientIdentifierType);
                } else {
                    log.warn("Global property " + EmrApiConstants.GP_EXTRA_PATIENT_IDENTIFIER_TYPES + " specifies an unknown patient identifier type: " + type);
                }
            }
        }
        return types;
    }

    private void createPersonImageFolder() {
        EmrApiProperties emrProperties = Context.getRegisteredComponents(EmrApiProperties.class).get(0);
        File personImageDirectory = emrProperties.getPersonImageDirectory();
        try {
            personImageDirectory.mkdirs();
        } catch (Exception e) {
            log.error("Could not create person images folder : " + personImageDirectory.getAbsolutePath(), e);
            throw new ModuleException("Could not create person images folder : " + personImageDirectory.getAbsolutePath());
        }
    }

    private void createUnknownProvider() {

        Provider unknownProvider = null;
        // see if the provider exists

        MetadataSource emrSource = metadataMappingService.getMetadataSourceByName(EmrApiConstants.EMR_METADATA_SOURCE_NAME);
        MetadataTermMapping mapping = metadataMappingService.getMetadataTermMapping(emrSource, EmrApiConstants.GP_UNKNOWN_PROVIDER);
        if(mapping != null){
            unknownProvider = providerService.getProviderByUuid(mapping.getMetadataUuid());
        }

        if(unknownProvider == null){
            // both of these global properties should at least exist, because they are defined in the config.xml of this module
            GlobalProperty emrApiUnknownProviderUuid = administrationService.getGlobalPropertyObject(EmrApiConstants.GP_UNKNOWN_PROVIDER);
            GlobalProperty coreApiUnknownProviderUuid = administrationService.getGlobalPropertyObject("provider.unknownProviderUuid"); // there's an OpenMRS constant for this, but it isn't available until 1.10

            // first try to fetch based on the GP provided by the EMR API module
            if (emrApiUnknownProviderUuid != null && StringUtils.isNotBlank(emrApiUnknownProviderUuid.getPropertyValue())) {
                unknownProvider = providerService.getProviderByUuid(emrApiUnknownProviderUuid.getPropertyValue());
            }

            // next try to fetch based on the GP provided by core
            if (unknownProvider == null) {
                if (coreApiUnknownProviderUuid!= null && StringUtils.isNotBlank(coreApiUnknownProviderUuid.getPropertyValue())) {
                    unknownProvider = providerService.getProviderByUuid(coreApiUnknownProviderUuid.getPropertyValue());
                }
            }
        }

        // if we haven't found an unknown provider. create it
        if (unknownProvider == null) {

            Person unknownPerson = new Person();
            unknownPerson.setGender("F");
            PersonName unknownPersonName = new PersonName();
            unknownPersonName.setGivenName("Unknown");
            unknownPersonName.setFamilyName("Provider");
            unknownPerson.addName(unknownPersonName);

            personService.savePerson(unknownPerson);

            unknownProvider = new Provider();
            unknownProvider.setPerson(unknownPerson);
            unknownProvider.setIdentifier("UNKNOWN");
            unknownProvider.setUuid("f9badd80-ab76-11e2-9e96-0800200c9a66");

            providerService.saveProvider(unknownProvider);
        }

        if(mapping == null){
            mapping = new MetadataTermMapping(emrSource, EmrApiConstants.GP_UNKNOWN_PROVIDER, unknownProvider);
            metadataMappingService.saveMetadataTermMapping(mapping);
        } else if (mapping.getMetadataUuid() == null){
            mapping.setMappedObject(unknownProvider);
            metadataMappingService.saveMetadataTermMapping(mapping);
        }
    }

    /**
     * (public so that it can be used in tests, but you shouldn't use this in production code)
     * Creates a single ConceptSource which we will use to tag concepts relevant to this module
     *
     */
    public ConceptSource createConceptSource(ConceptService conceptService) {
        ConceptSource conceptSource = conceptService.getConceptSourceByName(EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
        if (conceptSource == null) {
            conceptSource = new ConceptSource();
            conceptSource.setName(EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
            conceptSource.setDescription(EmrApiConstants.EMR_CONCEPT_SOURCE_DESCRIPTION);
            conceptSource.setUuid(EmrApiConstants.EMR_CONCEPT_SOURCE_UUID);
            conceptService.saveConceptSource(conceptSource);
        }
        return conceptSource;
    }

    @Override
    public void stopped() {
        if (eventListener != null){
            Event.unsubscribe(EmrApiConstants.EVENT_TOPIC_NAME_PATIENT_VIEWED, eventListener);
        }
    }

    @Override
    public void setDaemonToken(DaemonToken token) {
        daemonToken = token;
    }
}
