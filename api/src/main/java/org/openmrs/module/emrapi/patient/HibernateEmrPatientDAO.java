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
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.Query;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PersonName;
import org.openmrs.Visit;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.emrapi.EmrApiProperties;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Setter
public class HibernateEmrPatientDAO implements EmrPatientDAO {
	
	private DbSessionFactory sessionFactory;
	
	private EmrApiProperties emrApiProperties;
	
	@Override
	public List<Patient> findPatients(String query, Location checkedInAt, Integer start, Integer maxResults) {
		EntityManager em = sessionFactory.getHibernateSessionFactory().getCurrentSession();
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
		Query<Visit> query = sessionFactory.getHibernateSessionFactory().getCurrentSession()
		        .createQuery("from Visit v where v.patient = :patient and v.voided = false order by v.startDatetime desc");
		query.setParameter("patient", patient);
		if (startIndex != null) {
			query.setFirstResult(startIndex);
		}
		if (limit != null) {
			query.setMaxResults(limit);
		}
		return query.list();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public List<Obs> getVisitNoteObservations(Collection<Visit> visits) {
		if (visits == null || visits.isEmpty()) {
			return new ArrayList<>();
		}
		Query<Obs> query = sessionFactory.getHibernateSessionFactory().getCurrentSession()
		        .createQuery("select o from Obs o join o.encounter encounter where encounter.visit in (:visits)"
		                + " and encounter.encounterType = :encounterType and o.voided = false");
		query.setParameterList("visits", visits);
		query.setParameter("encounterType", emrApiProperties.getVisitNoteEncounterType());
		return query.list();
	}
}
