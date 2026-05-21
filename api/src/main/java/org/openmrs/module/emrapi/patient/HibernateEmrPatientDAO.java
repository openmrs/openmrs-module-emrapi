/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.patient;

import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.openmrs.Visit;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.emrapi.EmrApiProperties;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Setter
public class HibernateEmrPatientDAO implements EmrPatientDAO {
	
	private DbSessionFactory sessionFactory;
	
	private EmrApiProperties emrApiProperties;
	
	@Override
	public List<Patient> findPatients(String query, Location checkedInAt, Integer start, Integer maxResults) {
		EntityManager em = sessionFactory.getHibernateSessionFactory().unwrap(EntityManager.class);
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Patient> cq = cb.createQuery(Patient.class);
		Root<Patient> patient = cq.from(Patient.class);
		
		List<Predicate> preds = new ArrayList<>();
		
		if (checkedInAt != null) {
			Join<Patient, Visit> visit = patient.join("visits");
			preds.add(cb.equal(visit.get("location"), checkedInAt));
			preds.add(cb.isNull(visit.get("stopDatetime")));
		}
		
		if (StringUtils.isNotBlank(query)) {
			if (query.matches(".*\\d.*")) {
				Join<Patient, PatientIdentifier> ids = patient.join("identifiers", JoinType.LEFT);
				preds.add(cb.like(cb.lower(ids.get("identifier")), "%" + query.toLowerCase() + "%"));
			} else {
				Join<Patient, PersonName> names = patient.join("names");
				String like = "%" + query.toLowerCase() + "%";
				preds.add(cb.or(cb.like(cb.lower(names.get("givenName")), like),
				    cb.like(cb.lower(names.get("familyName")), like)));
			}
		}
		
		cq.select(patient).where(cb.and(preds.toArray(new Predicate[0]))).distinct(true);
		
		TypedQuery<Patient> typed = em.createQuery(cq);
		if (start != null) {
			typed.setFirstResult(start);
		}
		if (maxResults != null) {
			typed.setMaxResults(maxResults);
		}
		
		return typed.getResultList();
		
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
	public List<Obs> getVisitNoteObservations(Collection<Visit> visits) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Obs.class);
		criteria.createAlias("encounter", "encounter");
		criteria.add(Restrictions.in("encounter.visit", visits));
		criteria.add(Restrictions.eq("encounter.encounterType", emrApiProperties.getVisitNoteEncounterType()));
		criteria.add(Restrictions.eq("voided", false));
		return criteria.list();
	}
}
