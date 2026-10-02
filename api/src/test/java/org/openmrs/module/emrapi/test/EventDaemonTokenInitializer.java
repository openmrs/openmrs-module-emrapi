/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.test;

import java.lang.reflect.Method;

import org.openmrs.event.EventActivator;
import org.openmrs.module.Module;
import org.openmrs.module.ModuleFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * The event module's transaction listeners run in a daemon thread and need the daemon token that
 * ModuleFactory passes to the event module's activator when the module starts. Tests do not start
 * the event module, so this passes the token the same way, as the event module's own tests do. On
 * Platform 3.0 (Hibernate 7) an exception thrown from a transaction synchronization is no longer
 * swallowed, so without the token every committed test transaction fails.
 */
public class EventDaemonTokenInitializer implements InitializingBean {
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Module module = new Module("event");
		module.setModuleId("event");
		module.setModuleActivator(new EventActivator());
		
		Method passDaemonTokenMethod = ModuleFactory.class.getDeclaredMethod("passDaemonToken", Module.class);
		passDaemonTokenMethod.setAccessible(true);
		passDaemonTokenMethod.invoke(null, module);
	}
}
