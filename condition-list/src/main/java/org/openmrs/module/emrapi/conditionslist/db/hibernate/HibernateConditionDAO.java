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
package org.openmrs.module.emrapi.conditionslist.db.hibernate;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.openmrs.Condition;
import org.openmrs.Patient;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.emrapi.conditionslist.db.ConditionDAO;
import org.springframework.transaction.annotation.Transactional;

public class HibernateConditionDAO implements ConditionDAO {
	
	protected static final Log log = LogFactory.getLog(ConditionDAO.class);
	
	/**
	 * Hibernate session factory
	 */
	
	private DbSessionFactory sessionFactory;
	
	/**
	 * Set session factory
	 *
	 * @param sessionFactory
	 */
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@Override
	@Transactional
	public Condition saveOrUpdate(Condition condition) {
		sessionFactory.getCurrentSession().saveOrUpdate(condition);
		return condition;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Condition getConditionByUuid(String uuid) {
		return (Condition) sessionFactory.getCurrentSession().createQuery("from Condition c where c.uuid = :uuid")
				.setString("uuid", uuid).uniqueResult();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Condition> getConditionHistory(Patient patient) {
		Query query = sessionFactory.getCurrentSession().createQuery(
				"select con from Condition as con where con.patient.patientId = :patientId and con.voided = false " +
						"order by con.dateCreated desc");
		query.setInteger("patientId", patient.getId());
		return query.list();
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Condition> getActiveConditions(Patient patient) {
		Query query = sessionFactory.getCurrentSession().createQuery(
				"from Condition c where c.patient.patientId = :patientId and c.voided = false and c.endDate is null order "
						+ "by c.dateCreated desc");
		query.setInteger("patientId", patient.getId());
		return query.list();
	}
}
