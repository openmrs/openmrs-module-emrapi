/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.diagnosis;

import org.hibernate.query.Query;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Visit;
import org.openmrs.api.db.hibernate.DbSessionFactory;

import java.util.List;

/**
 * Hibernate implementation of the EmrDiagnosisDAO
 */
public class EmrDiagnosisDAOImpl implements EmrDiagnosisDAO {
	
	// TODO: Fetching diagnosis should be delegated to core Diagnosis service.
	// https://issues.openmrs.org/browse/TRUNK-5999
	
	private static final Integer PRIMARY_RANK = 1;
	
	private static final String CONFIRMED_CERTAINTY = "CONFIRMED";
	
	private DbSessionFactory sessionFactory;
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * Gets the diagnosis for a given visit
	 *
	 * @param visit visit to get the diagnoses from
	 * @param primaryOnly whether to fetch primary diagnosis only or all diagnosis regardless of rank
	 * @param confirmedOnly whether to fetch only confirmed diagnosis or both confirmed and provisional
	 * @return list of diagnoses for a visit
	 */
	public List<org.openmrs.Diagnosis> getDiagnoses(Visit visit, boolean primaryOnly, boolean confirmedOnly) {
		String queryString = "from Diagnosis d where d.encounter.visit.visitId = :visitId and d.voided = false";
		if (primaryOnly) {
			queryString += " and d.rank = :rankId";
		}
		if (confirmedOnly) {
			queryString += " and d.certainty = :certainty";
		}
		queryString += " order by d.dateCreated desc";
		
		Query query = sessionFactory.getHibernateSessionFactory().getCurrentSession().createQuery(queryString);
		query.setParameter("visitId", visit.getId());
		if (primaryOnly) {
			query.setParameter("rankId", PRIMARY_RANK);
		}
		if (confirmedOnly) {
			query.setParameter("certainty", ConditionVerificationStatus.valueOf(CONFIRMED_CERTAINTY));
		}
		
		return (List<org.openmrs.Diagnosis>) query.list();
	}
}
