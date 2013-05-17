package org.openmrs.module.emrapi.encounter;

import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;

import java.util.Date;
import java.util.Map;
import java.util.Set;

public class EncounterDomainWrapper {

    private Encounter encounter;

    public EncounterDomainWrapper(Encounter encounter) {
        this.encounter = encounter;
    }

    public Encounter getEncounter() {
        return encounter;
    }

    public void setEncounter(Encounter encounter) {
        this.encounter = encounter;
    }

    /**
     * Verify if a user is the creator or one of the providers in the encounter
     * @param currentUser
     * @return
     */
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

    public Visit getVisit() {
        return encounter.getVisit();
    }

    public Location getLocation() {
        return encounter.getLocation();
    }

    public void closeVisit() {
        Visit visit = encounter.getVisit();
        if (visit == null) {
            throw new IllegalArgumentException("This encounter does not belong to a visit");
        }
        if (visit.getStopDatetime() == null) {
            visit.setStopDatetime(new Date());
        }
        // TODO save the visit via service
    }

    public Map<EncounterRole, Set<Provider>> getProviders() {
        return encounter.getProvidersByRoles();
    }

}
