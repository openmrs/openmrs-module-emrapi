/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.db;

import org.hibernate.FlushMode;

public class DbSessionUtil {
	
	public static DbSessionDAO dbSessionDAO;
	
	public static DbSessionDAO getDbSessionDAO() {
		return dbSessionDAO;
	}
	
	public void setDbSessionDAO(DbSessionDAO dbSessionDAO) {
		DbSessionUtil.dbSessionDAO = dbSessionDAO;
	}
	
	public static void setDAO(DbSessionDAO dbSessionDAO) {
		DbSessionUtil.dbSessionDAO = dbSessionDAO;
	}
	
	public static FlushMode getCurrentFlushMode() {
		return dbSessionDAO.getCurrentFlushMode();
	}
	
	public static void setManualFlushMode() {
		dbSessionDAO.setManualFlushMode();
	}
	
	public static void setFlushMode(FlushMode flushMode) {
		dbSessionDAO.setFlushMode(flushMode);
	}
}
