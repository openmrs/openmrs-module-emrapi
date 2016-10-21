package org.openmrs.module.emrapi.db;

import org.hibernate.FlushMode;
import org.openmrs.api.db.hibernate.DbSessionFactory;

public class DbSessionDAOImpl implements DbSessionDAO {

    private DbSessionFactory sessionFactory;


    @Override
    public FlushMode getCurrentFlushMode() {
        return sessionFactory.getCurrentSession().getFlushMode();
    }

    @Override
    public void setManualFlushMode() {
        sessionFactory.getCurrentSession().setFlushMode(FlushMode.MANUAL);
    }

    @Override
    public void setFlushMode(FlushMode flushMode) {
        sessionFactory.getCurrentSession().setFlushMode(flushMode);
    }

    public void setSessionFactory(DbSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
