/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.merge;

import org.openmrs.Visit;

/**
 * {@link org.openmrs.module.emrapi.adt.AdtService#mergeVisits(Visit, Visit)} will invoke all
 * instances of this interface that are registered with it before and after voiding and saving the
 * respective visits.
 */
public interface VisitMergeAction {
	
	/**
	 * This method will be called before calling the visit service to save / void either of the given
	 * visits, but in the same transaction. Any thrown exception will cancel the merge
	 *
	 * @param preferred the Visit to keep
	 * @param notPreferred the Visit to merge into the preferred visit
	 */
	void beforeSavingVisits(Visit preferred, Visit notPreferred);
	
	/**
	 * This method will be called after calling the visit service to save / void either of the given
	 * visits, but in the same transaction. Any thrown exception will cancel the merge same transaction.
	 *
	 * @param preferred the Visit to keep
	 * @param notPreferred the Visit to merge into the preferred visit
	 */
	void afterSavingVisits(Visit preferred, Visit notPreferred);
	
}
