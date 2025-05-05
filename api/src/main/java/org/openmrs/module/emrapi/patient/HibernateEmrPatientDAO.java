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
package org.openmrs.module.emrapi.patient;

import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Visit;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.api.db.hibernate.PatientSearchCriteria;
import org.openmrs.module.emrapi.EmrApiProperties;

import java.util.ArrayList;
import java.util.List;

@Setter
public class HibernateEmrPatientDAO implements EmrPatientDAO {
	
	private DbSessionFactory sessionFactory;
	
	private EmrApiProperties emrApiProperties;
	
	@Override
	public List<Patient> findPatients(String query, Location checkedInAt, Integer start, Integer maxResults) {
		
		Criteria criteria;
		if (checkedInAt != null) {
			criteria = sessionFactory.getCurrentSession().createCriteria(Visit.class);
			criteria.setProjection(Property.forName("patient"));
			criteria.add(Restrictions.isNull("stopDatetime"));
			criteria.add(Restrictions.eq("location", checkedInAt));
			Criteria patientCriteria = criteria.createCriteria("patient");
			if (StringUtils.isNotBlank(query)) {
				patientCriteria = buildCriteria(query, patientCriteria);
			}
			criteria = patientCriteria;
		} else {
			criteria = sessionFactory.getCurrentSession().createCriteria(Patient.class);
			criteria = buildCriteria(query, criteria);
		}
		
		if (start != null) {
			criteria.setFirstResult(start);
		}
		
		if (maxResults != null) {
			criteria.setMaxResults(maxResults);
		}
		
		return (List<Patient>) criteria.list();
	}
	
	private Criteria buildCriteria(String query, Criteria criteria) {
		if (query.matches(".*\\d.*")) {
			// has at least one digit, so treat as an identifier
			return new PatientSearchCriteria(sessionFactory.getHibernateSessionFactory(), criteria).prepareCriteria(null, query,
			    emrApiProperties.getIdentifierTypesToSearch(), true, true, true);
		} else {
			// no digits, so treat as a name
			return new PatientSearchCriteria(sessionFactory.getHibernateSessionFactory(), criteria).prepareCriteria(query, null,
			    new ArrayList<PatientIdentifierType>(), true, true, true);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Visit> getVisitsForPatient(Patient patient, Integer startIndex, Integer limit) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Visit.class);
		criteria.add(Restrictions.eq("patient", patient));
		criteria.add(Restrictions.eq("voided", false));
		criteria.addOrder(Order.desc("startDatetime"));
		if (startIndex != null) {
			criteria.setFirstResult(startIndex);
		}
		if (limit != null) {
			criteria.setMaxResults(limit);
		}
		return criteria.list();
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Obs> getVisitNoteObservations(List<Visit> visits) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		criteria.createAlias("encounter", "encounter");
		criteria.add(Restrictions.in("encounter.visit", visits));
		criteria.add(Restrictions.eq("encounter.encounterType", emrApiProperties.getVisitNoteEncounterType()));
		criteria.add(Restrictions.eq("voided", false));
		return criteria.list();
	}
}
