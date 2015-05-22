package org.openmrs.module.emrapi.descriptor;

public class MissingConceptException extends IllegalStateException {

    public MissingConceptException(String message) {
        super(message);
    }
}
