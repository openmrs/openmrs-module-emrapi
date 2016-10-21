package org.openmrs.module.emrapi.db;

import org.hibernate.FlushMode;

public interface DbSessionDAO {
    FlushMode getCurrentFlushMode();
    void setManualFlushMode();
    void setFlushMode(FlushMode flushMode);
}
