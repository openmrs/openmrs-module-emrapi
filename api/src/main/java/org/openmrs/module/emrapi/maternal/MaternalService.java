/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.maternal;

import java.util.List;

import org.openmrs.api.OpenmrsService;

public interface MaternalService extends OpenmrsService {
	
	/**
	 * Fetches patients in a "Mother-to-Child" relationship, based on the given search criteria.
	 *
	 * @param criteria search criteria (see class for details)
	 * @return a list of mothers and children that match the search criteria
	 */
	List<MotherAndChild> getMothersAndChildren(MothersAndChildrenSearchCriteria criteria);
	
}
