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
