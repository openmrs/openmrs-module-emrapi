package org.openmrs.module.emrapi.adt.exception;

public class EncounterDateBeforeVisitStartDateException extends Exception {

    private static final long serialVersionUID = 1L;

    public EncounterDateBeforeVisitStartDateException() {
        super();
    }

    public EncounterDateBeforeVisitStartDateException(String message) {
        super(message);
    }

    public EncounterDateBeforeVisitStartDateException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
