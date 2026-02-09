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
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.db.hibernate.DbSessionFactory;

import java.util.List;

/**
 * Hibernate implementation of the ProcedureDAO interface.
 */
@Setter
public class HibernateProcedureDAO implements ProcedureDAO {
   
   private DbSessionFactory sessionFactory;
   
   @Override
   public Procedure getById(Integer id) {
      return (Procedure) sessionFactory.getCurrentSession().get(Procedure.class, id);
   }
   
   @Override
   public Procedure getByUuid(String uuid) {
      Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Procedure.class);
      criteria.add(Restrictions.eq("uuid", uuid));
      return (Procedure) criteria.uniqueResult();
   }
   
   @Override
   public Procedure saveOrUpdate(Procedure procedure) {
      sessionFactory.getCurrentSession().saveOrUpdate(procedure);
      return procedure;
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public List<Procedure> getProceduresByPatient(Patient patient, boolean includeVoided) {
      Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Procedure.class);
      criteria.add(Restrictions.eq("patient", patient));
      if (!includeVoided) {
         criteria.add(Restrictions.eq("voided", false));
      }
      criteria.addOrder(Order.desc("startDateTime"));
      return criteria.list();
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public List<Procedure> getProceduresByEncounter(Encounter encounter) {
      Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Procedure.class);
      criteria.add(Restrictions.eq("encounter", encounter));
      criteria.add(Restrictions.eq("voided", false));
      criteria.addOrder(Order.desc("startDateTime"));
      return criteria.list();
   }
   
   @Override
   public void delete(Procedure procedure) {
      sessionFactory.getCurrentSession().delete(procedure);
   }
}
