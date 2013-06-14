package org.openmrs.module.emrapi.adt.exception;

public class ExistingVisitDuringTimePeriodException extends Exception {

    private static final long serialVersionUID = 1L;

    public ExistingVisitDuringTimePeriodException() {
        super();
    }

    public ExistingVisitDuringTimePeriodException(String message) {
        super(message);
    }

    public ExistingVisitDuringTimePeriodException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
