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
import org.openmrs.Patient;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

/**
 * Hibernate implementation of {@link ProcedureDAO}.
 * @since 3.3.0
 */
@Setter
@Slf4j
public class HibernateProcedureDAO implements ProcedureDAO {

   private SessionFactory sessionFactory;

   private EntityManager getEntityManager() {
      return sessionFactory.getCurrentSession();
   }

   @Override
   public Procedure getById(Integer id) {
      log.debug("Getting procedure by id: {}", id);
      
      return getEntityManager().find(Procedure.class, id);
   }

   @Override
   public Procedure getByUuid(String uuid) {
      log.debug("Getting procedure by uuid: {}", uuid);
      String jpql = "SELECT p FROM Procedure p WHERE p.uuid = :uuid";
      
      TypedQuery<Procedure> query = getEntityManager().createQuery(jpql, Procedure.class);
      query.setParameter("uuid", uuid);
      
      List<Procedure> results = query.setMaxResults(1).getResultList();
      return results.isEmpty() ? null : results.get(0);
   }

   @Override
   public Procedure saveOrUpdate(Procedure procedure) {
      log.debug("Saving or updating procedure: {}", procedure.getUuid());
      return getEntityManager().merge(procedure);
   }
   
   @Override
   public List<Procedure> getProceduresByPatient(Patient patient, boolean includeVoided) {
      log.debug("Getting procedures for patient: {}, includeVoided: {}", patient, includeVoided);
      
      String jpql = "SELECT p FROM Procedure p" +
              " WHERE p.patient = :patient" +
              (includeVoided ? "" : " AND p.voided = false") +
              " ORDER BY p.startDateTime DESC";
      
      TypedQuery<Procedure> query = getEntityManager().createQuery(jpql, Procedure.class);
      query.setParameter("patient", patient);
      
      return query.getResultList();
   }
   
   @Override
   public void delete(Procedure procedure) {
      log.debug("Deleting procedure: {}", procedure.getUuid());
      sessionFactory.getCurrentSession().delete(procedure);
   }

}
