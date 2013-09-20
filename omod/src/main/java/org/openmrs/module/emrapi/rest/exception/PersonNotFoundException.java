package org.openmrs.module.emrapi.rest.exception;

import org.openmrs.api.APIException;

public class PersonNotFoundException extends APIException {

    public PersonNotFoundException(String message) {
        super(message);
    }
}
