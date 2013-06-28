package org.openmrs.module.emrapi.adt.exception;

public class EncounterDateAfterVisitStopDateException extends Exception {

    private static final long serialVersionUID = 1L;

    public EncounterDateAfterVisitStopDateException() {
        super();
    }

    public EncounterDateAfterVisitStopDateException(String message) {
        super(message);
    }

    public EncounterDateAfterVisitStopDateException(String message, Throwable throwable) {
        super(message, throwable);
    }


}
