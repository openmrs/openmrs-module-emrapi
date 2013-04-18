package org.openmrs.module.emrapi.encounter;

import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.User;

import java.util.Set;

public class EncounterDomainWrapper {

    private Encounter encounter;

    public EncounterDomainWrapper(Encounter encounter) {
        this.encounter = encounter;
    }

    public boolean participatedInEncounter(User currentUser) {

        if (verifyIfUserIsTheCreatorOfEncounter(currentUser)){
            return true;
        } else if (verifyIfUserIsOneOfTheProviders(currentUser)){
            return true;
        }

        return false;
    }

    private boolean verifyIfUserIsOneOfTheProviders(User currentUser) {
        for (EncounterProvider encounterProvider : encounter.getEncounterProviders()) {
            if (encounterProvider.getProvider().getPerson().equals(currentUser.getPerson())){
                return true;
            }
        }
        return false;
    }

    private boolean verifyIfUserIsTheCreatorOfEncounter(User currentUser) {
        return encounter.getCreator().equals(currentUser);
    }
}
