package org.openmrs.module.emrapi.encounter.exception;

public class ConceptNotFoundException extends RuntimeException {

    public ConceptNotFoundException(String message) {
        super(message);
    }

}
