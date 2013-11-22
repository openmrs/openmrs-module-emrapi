package org.openmrs.module.emrapi.encounter.builder;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;

import java.util.UUID;

public class EncounterBuilder {
    private final Encounter encounter;

    public EncounterBuilder() {
        encounter = new Encounter();
        Visit visit = new Visit();
        VisitType visitType = new VisitType();
        visitType.setUuid(UUID.randomUUID().toString());
        visit.setVisitType(visitType);
        visit.setUuid(UUID.randomUUID().toString());
        encounter.setVisit(visit);
        encounter.setUuid(UUID.randomUUID().toString());
        Patient patient = new Patient();
        patient.setUuid(UUID.randomUUID().toString());
        encounter.setPatient(patient);
        EncounterType encounterType = new EncounterType();
        encounterType.setUuid(UUID.randomUUID().toString());
        encounter.setEncounterType(encounterType);
        Location location = new Location();
        location.setUuid(UUID.randomUUID().toString());
        encounter.setLocation(location);
    }

    public Encounter build() {
        return encounter;
    }
}