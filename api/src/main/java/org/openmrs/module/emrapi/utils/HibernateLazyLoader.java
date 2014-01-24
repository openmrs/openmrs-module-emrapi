package org.openmrs.module.emrapi.utils;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

//should be removed once this class is moved to web service api from omod
public class HibernateLazyLoader {

    public <T> T load(T entity) {
        if (entity == null) {
            return null;
        }
        if (entity instanceof HibernateProxy) {
            Hibernate.initialize(entity);
            entity = (T) ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation();
        }
        return entity;
    }
}
