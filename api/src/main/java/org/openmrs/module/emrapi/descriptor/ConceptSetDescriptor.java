package org.openmrs.module.emrapi.descriptor;

import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;

public abstract class ConceptSetDescriptor {

    /**
     * @param conceptService
     * @param conceptSourceName
     * @param fieldsAndConceptCodes must have an even number of elements. The first of each pair is the field name to set (on a subclass) and the second is the code in conceptSourceName.
     */
    protected void setup(ConceptService conceptService, String conceptSourceName, String... fieldsAndConceptCodes) {
        try {
            String primaryConceptCode = fieldsAndConceptCodes[1];
            Concept primaryConcept = conceptService.getConceptByMapping(primaryConceptCode, conceptSourceName);
            if (primaryConcept == null) {
                throw new IllegalStateException("Couldn't find primary concept for " + getClass().getSimpleName() + " which should be mapped as " + conceptSourceName + ":" + primaryConceptCode);
            }
            PropertyUtils.setProperty(this, fieldsAndConceptCodes[0], primaryConcept);
            for (int i = 2; i < fieldsAndConceptCodes.length; i += 2) {
                String propertyName = fieldsAndConceptCodes[i];
                String mappingCode = fieldsAndConceptCodes[i + 1];
                Concept childConcept = conceptService.getConceptByMapping(mappingCode, conceptSourceName);
                if (childConcept == null) {
                    throw new IllegalStateException("Couldn't find " + propertyName + " concept for " + getClass().getSimpleName() + " which should be mapped as " + conceptSourceName + ":" + mappingCode);
                }
                if (!primaryConcept.getSetMembers().contains(childConcept)) {
                    throw new IllegalStateException("Concept mapped as " + conceptSourceName + ":" + mappingCode + " needs to be a set member of concept " + primaryConcept.getConceptId() + " which is mapped as " + conceptSourceName + ":" + primaryConceptCode);
                }
                PropertyUtils.setProperty(this, propertyName, childConcept);
            }
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new IllegalStateException(ex);
            }
        }
    }

    protected Obs findMember(Obs obsGroup, Concept concept) {
        for (Obs candidate : obsGroup.getGroupMembers(false)) {
            if (candidate.getConcept().equals(concept)) {
                return candidate;
            }
        }
        return null;
    }
}
