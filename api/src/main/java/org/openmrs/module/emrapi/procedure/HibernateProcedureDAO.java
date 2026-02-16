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
import org.openmrs.Patient;
import org.openmrs.api.db.hibernate.DbSessionFactory;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Setter
public class HibernateProcedureDAO implements ProcedureDAO {

   private DbSessionFactory sessionFactory;

   private EntityManager getEntityManager() {
      return sessionFactory.getHibernateSessionFactory().getCurrentSession();
   }

   @Override
   public Procedure getById(Integer id) {
      return getEntityManager().find(Procedure.class, id);
   }

   @Override
   public Procedure getByUuid(String uuid) {
      String jpql = "SELECT p FROM Procedure p WHERE p.uuid = :uuid";
      
      TypedQuery<Procedure> query = getEntityManager().createQuery(jpql, Procedure.class);
      query.setParameter("uuid", uuid);
      
      List<Procedure> results = query.getResultList();
      return results.isEmpty() ? null : results.get(0);
   }

   @Override
   public Procedure saveOrUpdate(Procedure procedure) {
      return getEntityManager().merge(procedure);
   }
   
   @Override
   public List<Procedure> getProceduresByPatient(Patient patient, boolean includeVoided) {
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
      sessionFactory.getCurrentSession().delete(procedure);
   }
}
