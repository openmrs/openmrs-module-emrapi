package org.openmrs.module.emrapi.adt.reporting.definition;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

import java.util.Map;

@Caching(strategy=ConfigurationPropertyCachingStrategy.class)
public class MostRecentAdmissionRequestVisitDataDefinition extends BaseDataDefinition implements VisitDataDefinition {


    public static final long serialVersionUID = 1L;

    /**
     * Default Constructor
     */
    public MostRecentAdmissionRequestVisitDataDefinition() {
        super();
    }

    /**
     * Constructor to populate name only
     */
    public MostRecentAdmissionRequestVisitDataDefinition(String name) {
        super(name);
    }

    @Override
    public Class<?> getDataType() {
        return Map.class;
    }

}
