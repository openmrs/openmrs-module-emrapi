/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.db;

import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public abstract class HibernateSingleClassDAO<T> implements SingleClassDAO<T> {
	
	protected DbSessionFactory sessionFactory;
	
	protected Class<T> mappedClass;
	
	/**
	 * Marked private because you *must* provide the class at runtime when instantiating one of these,
	 * using the next constructor
	 */
	@SuppressWarnings("unused")
	private HibernateSingleClassDAO() {
	}
	
	/**
	 * You must call this before using any of the data access methods, since it's not actually possible
	 * to write them all with compile-time class information.
	 *
	 * @param mappedClass
	 */
	protected HibernateSingleClassDAO(Class<T> mappedClass) {
		this.mappedClass = mappedClass;
	}
	
	public void setSessionFactory(DbSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public T getById(Integer id) {
		return (T) sessionFactory.getCurrentSession().get(mappedClass, id);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<T> getAll() {
		return (List<T>) sessionFactory.getCurrentSession().createCriteria(mappedClass).list();
	}
	
	@Override
	@Transactional
	public T saveOrUpdate(T object) {
		sessionFactory.getCurrentSession().saveOrUpdate(object);
		return object;
	}
	
	@Override
	@Transactional
	public T update(T object) {
		sessionFactory.getCurrentSession().update(object);
		return object;
	}
	
	@Override
	@Transactional
	public void delete(T object) {
		sessionFactory.getCurrentSession().delete(object);
	}
	
}
