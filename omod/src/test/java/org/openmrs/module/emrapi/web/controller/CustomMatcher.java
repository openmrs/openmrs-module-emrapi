package org.openmrs.module.emrapi.web.controller;

import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.encounter.matcher.BaseEncounterMatcher;
import org.openmrs.module.emrapi.encounter.EncounterParameters;
import org.springframework.stereotype.Component;

@Component
public class CustomMatcher implements BaseEncounterMatcher {

    @Override
    public Encounter findEncounter(Visit visit, EncounterParameters encounterParameters) {
        for (Encounter encounter : visit.getEncounters()) {
            if (encounter.getUuid().equals("f13d6fae-baa9-4553-955d-920098bec08g")) {
                return encounter;
            }
        }
        return null;
    }
}
