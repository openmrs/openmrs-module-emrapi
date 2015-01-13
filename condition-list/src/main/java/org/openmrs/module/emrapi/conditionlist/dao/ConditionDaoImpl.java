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
package org.openmrs.module.emrapi.conditionlist.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Patient;
import org.openmrs.module.emrapi.conditionlist.domain.Condition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ConditionDaoImpl implements ConditionDao {

    protected static final Log log = LogFactory.getLog(ConditionDao.class);
    /**
     * Hibernate session factory
     */

    private SessionFactory sessionFactory;

    /**
     * Set session factory
     *
     * @param sessionFactory
     */
    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    @Transactional
    public Condition saveOrUpdate(Condition condition) {
        sessionFactory.getCurrentSession().saveOrUpdate(condition);
        return condition;
    }

    @Override
    @Transactional
    public Condition getConditionByUuid(String uuid) {
        Query query = sessionFactory.getCurrentSession().createQuery("from Condition where uuid=:uuid");
        query.setString("uuid", uuid);
        List<Condition> list = query.list();
        if (list.size() != 0) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<Condition> getConditions(Patient patient) {
        Query query = sessionFactory.getCurrentSession().createQuery("from Condition where patient.patientId=:patientId");
        query.setInteger("patientId", patient.getId());
        return query.list();
    }
}
