package org.openmrs.module.emrapi.encounter.exception;

public class EncounterMatcherNotFoundException extends RuntimeException {

    public EncounterMatcherNotFoundException() {
        super("Encounter matcher not found.");
    }
}
