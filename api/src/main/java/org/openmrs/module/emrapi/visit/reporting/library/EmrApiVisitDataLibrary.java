package org.openmrs.module.emrapi.visit.reporting.library;

import org.openmrs.module.emrapi.adt.reporting.definition.MostRecentAdmissionRequestVisitDataDefinition;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.springframework.stereotype.Component;

/**
 * Basic visit data columns provided by module
 */
@Component
public class EmrApiVisitDataLibrary extends BaseDefinitionLibrary<VisitDataDefinition> {


    public static final String PREFIX = "emrapi.visitDataDefinition.";

    @Override
    public Class<? super VisitDataDefinition> getDefinitionType() {
        return VisitDataDefinition.class;
    }

    @Override
    public String getKeyPrefix() {
        return PREFIX;
    }

    @DocumentedDefinition("mostRecentAdmissionRequest")
    public VisitDataDefinition getMostRecentAdmissionRequestVisitDataDefinition() {
        return new MostRecentAdmissionRequestVisitDataDefinition();
    }

}
