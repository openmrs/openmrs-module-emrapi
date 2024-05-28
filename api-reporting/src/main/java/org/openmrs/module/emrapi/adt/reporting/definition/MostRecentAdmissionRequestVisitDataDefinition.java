package org.openmrs.module.emrapi.adt.reporting.definition;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

import java.util.Map;

/**
 * Given a visit, finds the most recent encounter associated with that visit (if any) which contains a disposition obs
 * that as a value as reference to any disposition of type "ADMIT".
 *
 * Returns a map containing:
 *  "fromLocation"  - the location where the admission request was made (encounter location of the encounter defined above)
 *  "datetime"      - the time the request was made (encounterDatetime of the encounter)
 *  "provider"      - the person that requested the admission (provider of the encounter--if there are multiple providers,
 *                      for now this just returns the first non-voided provider)
 *  "toLocation"    - the location that the patient is being recommended for admission to (pulled from the encounter
 *                      via the DispositionDescriptor and, in particular, the getAdmissionLocation method)
 *  "Diagnoses"     - List of primary diagnoses associated with this request (pulled from the encounter
 *                      via getPrimaryDiagnoses(Encounter) method of the DiagnosisService)
 */
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
