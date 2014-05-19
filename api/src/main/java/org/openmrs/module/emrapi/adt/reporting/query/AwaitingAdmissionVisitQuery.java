package org.openmrs.module.emrapi.adt.reporting.query;

import org.openmrs.Location;
import org.openmrs.Visit;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;
import org.openmrs.module.reporting.query.BaseQuery;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;

/**
 * Returns all visits that match the following rules:
 *  1) Are active (stopDatetime = null) AND
 *  2) Have at least one (non-voided) disposition of type "ADMIT" AND
 *  3) Does not have a (non-voided) encounter of type "Admission" AND
 *  4) Does not have an "Admission Decision" obs with value "Deny Admission" that is dated *after* the most recent "ADMIT" disposition
 *
 * If a location is specified, restricts the query to only visits that have the chosen location as a visit location
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
