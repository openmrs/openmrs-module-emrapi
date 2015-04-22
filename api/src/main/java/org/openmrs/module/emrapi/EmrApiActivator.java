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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ConceptSource;
import org.openmrs.GlobalProperty;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Privilege;
import org.openmrs.Provider;
import org.openmrs.Role;
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
import org.openmrs.util.OpenmrsConstants;

import java.io.File;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class EmrApiActivator extends BaseModuleActivator implements DaemonTokenAware {

    protected final Log log = LogFactory.getLog(getClass());

    private EventListener eventListener;

    private DaemonToken daemonToken;

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
        if (fullPrivilegeLevel == null) {
            fullPrivilegeLevel = new Role();
            fullPrivilegeLevel.setRole(EmrApiConstants.PRIVILEGE_LEVEL_FULL_ROLE);
            fullPrivilegeLevel.setDescription(EmrApiConstants.PRIVILEGE_LEVEL_FULL_DESCRIPTION);
            fullPrivilegeLevel.setUuid(EmrApiConstants.PRIVILEGE_LEVEL_FULL_UUID);
            userService.saveRole(fullPrivilegeLevel);
        }

        for (Privilege candidate : accountService.getApiPrivileges()) {
            if (!fullPrivilegeLevel.hasPrivilege(candidate.getName())) {
                fullPrivilegeLevel.addPrivilege(candidate);
            }
        }
        userService.saveRole(fullPrivilegeLevel);
    }

    @Override
    public void started() {
        super.started();

        AdministrationService administrationService = Context.getAdministrationService();
        ProviderService providerService = Context.getProviderService();
        PersonService personService = Context.getPersonService();
        ConceptService conceptService = Context.getConceptService();

        createGlobalProperties(administrationService);
        createUnknownProvider(administrationService, providerService, personService);
        createConceptSource(conceptService);
        eventListener = new PatientViewedEventListener(daemonToken);
        Event.subscribe(EmrApiConstants.EVENT_TOPIC_NAME_PATIENT_VIEWED, eventListener);

        createPersonImageFolder();
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

    private void createGlobalProperties(AdministrationService administrationService) {
        // When https://tickets.openmrs.org/browse/TRUNK-3773 is resolved, refactor this to refer to a bean by id
        GlobalProperty gp = administrationService.getGlobalPropertyObject(OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER);
        if (gp == null) {
            gp = new GlobalProperty();
            gp.setProperty(OpenmrsConstants.GP_VISIT_ASSIGNMENT_HANDLER);
        }
        gp.setPropertyValue(EmrApiVisitAssignmentHandler.class.getName());
        administrationService.saveGlobalProperty(gp);
    }

    private void createUnknownProvider(AdministrationService adminService, ProviderService providerService, PersonService personService) {

        // see if the provider exists
        Provider provider = null;
        String providerUuid = adminService.getGlobalProperty(EmrApiConstants.GP_UNKNOWN_PROVIDER);

        if (StringUtils.isNotBlank(providerUuid)) {
            provider = providerService.getProviderByUuid(providerUuid);
        }

        // create the unknown provider if necessary
        if (provider == null) {
            Person unknownPerson = new Person();
            unknownPerson.setGender("F");
            PersonName unknownPersonName = new PersonName();
            unknownPersonName.setGivenName("Unknown");
            unknownPersonName.setFamilyName("Provider");
            unknownPerson.addName(unknownPersonName);

            personService.savePerson(unknownPerson);

            Provider unknownProvider = new Provider();
            unknownProvider.setPerson(unknownPerson);
            unknownProvider.setIdentifier("UNKNOWN");
            unknownProvider.setUuid("f9badd80-ab76-11e2-9e96-0800200c9a66");

            providerService.saveProvider(unknownProvider);

            // also set global property
            GlobalProperty unknownProviderUuid = adminService.getGlobalPropertyObject(EmrApiConstants.GP_UNKNOWN_PROVIDER);
            if (unknownProviderUuid == null) {
                unknownProviderUuid = new GlobalProperty(EmrApiConstants.GP_UNKNOWN_PROVIDER);
            }
            unknownProviderUuid.setPropertyValue("f9badd80-ab76-11e2-9e96-0800200c9a66");
            adminService.saveGlobalProperty(unknownProviderUuid);

        }

        // also update to set name field of provider to Unknown Provider, added Apr 22 2015
        if (provider.getName() == null) {
            provider.setName("Unknown Provider");
            providerService.saveProvider(provider);
        }

    }

    /**
     * (public so that it can be used in tests, but you shouldn't use this in production code)
     * Creates a single ConceptSource which we will use to tag concepts relevant to this module
     *
     * @param conceptService
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
