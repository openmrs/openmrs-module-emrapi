/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.procedure;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Hibernate implementation of {@link ProcedureTypeDAO}.
 * @since 3.3.0
 */
@Setter
@Slf4j
public class HibernateProcedureTypeDAO implements ProcedureTypeDAO {

	private SessionFactory sessionFactory;

	private EntityManager getEntityManager() {
		return sessionFactory.getCurrentSession();
	}

	@Override
	public ProcedureType getByUuid(String uuid) {
		log.debug("Getting procedure type by uuid: {}", uuid);
  
		TypedQuery<ProcedureType> query = getEntityManager()
				.createQuery("SELECT pt FROM ProcedureType pt WHERE pt.uuid = :uuid", ProcedureType.class);
		query.setParameter("uuid", uuid);
  
		List<ProcedureType> results = query.setMaxResults(1).getResultList();
		return results.isEmpty() ? null : results.get(0);
	}

	@Override
	public List<ProcedureType> getAll(boolean includeRetired) {
		log.debug("Getting all procedure types, includeRetired: {}", includeRetired);
  
		String jpql = "SELECT pt FROM ProcedureType pt"
				+ (includeRetired ? "" : " WHERE pt.retired = false")
				+ " ORDER BY pt.name ASC";
    
		return getEntityManager().createQuery(jpql, ProcedureType.class).getResultList();
	}

	@Override
	public ProcedureType saveOrUpdate(ProcedureType procedureType) {
		log.debug("Saving or updating procedure type: {}", procedureType.getName());
		return getEntityManager().merge(procedureType);
	}

	@Override
	public void delete(ProcedureType procedureType) {
		log.debug("Deleting procedure type: {}", procedureType.getName());
		sessionFactory.getCurrentSession().delete(procedureType);
	}
}
