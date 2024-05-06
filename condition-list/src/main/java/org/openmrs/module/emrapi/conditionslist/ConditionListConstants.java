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
package org.openmrs.module.emrapi.conditionslist;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Constants used in ConditionList. Contents built from build properties (version, version_short, and
 * expected_database). Some are set at runtime (database, database version). This file should
 * contain all privilege names and global property names. Those strings added to the static CORE_*
 * methods will be written to the database at startup if they don't exist yet.
 */

public final class ConditionListConstants {
	
	private static Log log = LogFactory.getLog(ConditionListConstants.class);
	
	public static final String GP_END_REASON_CONCEPT_SET_UUID = "conditionList.endReasonConceptSetUuid";
	
	public static final String GLOBAL_PROPERTY_NON_CODED_UUID = "conditionList.nonCodedUuid";
	
}