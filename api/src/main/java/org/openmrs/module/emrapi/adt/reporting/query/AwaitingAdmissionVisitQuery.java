package org.openmrs.module.emrapi.adt.reporting.query;

import org.openmrs.Location;
import org.openmrs.Visit;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;
import org.openmrs.module.reporting.query.BaseQuery;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;

/**
 * Returns all encounters that represent an active admission disposition (ie. current, unfulfilled admission request)
 */
@Caching(strategy=ConfigurationPropertyCachingStrategy.class)
public class AwaitingAdmissionVisitQuery extends BaseQuery<Visit> implements VisitQuery {


    @ConfigurationProperty
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

}
