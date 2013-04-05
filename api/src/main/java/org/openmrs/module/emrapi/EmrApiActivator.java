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

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.Privilege;
import org.openmrs.Role;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.emrapi.account.AccountService;
import org.openmrs.module.emrapi.metadata.MetadataManager;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class EmrApiActivator extends BaseModuleActivator {
	
	protected static final Log log = LogFactory.getLog(EmrApiActivator.class);
	
	private static final String PACKAGES_FILENAME = "packages.xml";
	
	/**
	 * @see ModuleActivator#contextRefreshed()
	 */
	@Override
	public void contextRefreshed() {
		super.contextRefreshed(); //To change body of overridden methods use File | Settings | File Templates.
		
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
	
	/**
	 * @see ModuleActivator#started()
	 * @should install initial data only once
	 */
	public void started() {
		setupGlobalProperties();
		
		log.info("Setup core global properties");
		
		boolean metadataUpdated = setupStandardMetadata();
		
		log.info("Setup core metadata packages (" + (metadataUpdated ? "imported packages" : "already up-to-date") + ")");
	}
	
	/**
	 * Setup the standard metadata packages
	 * 
	 * @return
	 */
	protected boolean setupStandardMetadata() {
		try {
			InputStream stream = getClass().getClassLoader().getResourceAsStream(PACKAGES_FILENAME);
			return new MetadataManager().loadPackagesFromXML(stream, null);
		}
		catch (Exception ex) {
			throw new RuntimeException("Cannot find " + PACKAGES_FILENAME + ". Make sure it's in api/src/main/resources");
		}
	}
	
	/**
	 * Setup required global properties (Public for testing)
	 */
	public void setupGlobalProperties() {
		
	}
	
	/**
	 * Creates an empty global property if it doesn't exist
	 * 
	 * @param property the property name
	 * @param description the property description
	 * @param dataType the property value data type
	 */
	protected void ensureGlobalPropertyExists(String property, String description, Class dataType) {
		GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(property);
		if (gp == null) {
			gp = new GlobalProperty();
			gp.setProperty(property);
			gp.setDescription(description);
			gp.setDatatypeClassname(dataType.getName());
			Context.getAdministrationService().saveGlobalProperty(gp);
		}
	}
}
