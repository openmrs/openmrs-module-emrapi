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

import org.hibernate.query.Query;
import org.openmrs.Patient;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;

import java.util.List;

public class HibernateEmrEncounterDAO implements EmrEncounterDAO {
	
	private DbSessionFactory sessionFactory;
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	public List<Encounter> getEncountersByObsValueText(Patient patient, Concept obsConcept, String valueText,
	        EncounterType encounterType, boolean includeAll) {
		
		// we want to return an encounters (but not duplicate encounters)
		StringBuilder hql = new StringBuilder("select distinct o.encounter from Obs o");
		
		if (encounterType != null) {
			// join on the encounter table
			hql.append(" join o.encounter encounter");
		}
		
		hql.append(" where o.valueText = :valueText");
		
		if (!includeAll) {
			hql.append(" and o.voided = false");
		}
		
		if (obsConcept != null) {
			hql.append(" and o.concept = :concept");
		}
		
		if (encounterType != null) {
			hql.append(" and encounter.encounterType = :encounterType");
		}
		
		if (patient != null) {
			hql.append(" and o.person = :person");
		}
		
		Query<Encounter> query = sessionFactory.getHibernateSessionFactory().getCurrentSession().createQuery(hql.toString());
		query.setParameter("valueText", valueText);
		if (obsConcept != null) {
			query.setParameter("concept", obsConcept);
		}
		if (encounterType != null) {
			query.setParameter("encounterType", encounterType);
		}
		if (patient != null) {
			query.setParameter("person", patient);
		}
		
		return query.list();
	}
	
	@Override
	public List<Encounter> getEncountersByObsValueText(Concept obsConcept, String valueText, EncounterType encounterType,
	        boolean includeAll) {
		return getEncountersByObsValueText(null, obsConcept, valueText, encounterType, includeAll);
	}
	
}
