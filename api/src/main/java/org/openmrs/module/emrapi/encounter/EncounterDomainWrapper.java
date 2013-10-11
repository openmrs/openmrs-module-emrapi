package org.openmrs.module.emrapi.encounter;

import org.apache.commons.collections.Predicate;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.adt.exception.EncounterDateAfterVisitStopDateException;
import org.openmrs.module.emrapi.adt.exception.EncounterDateBeforeVisitStartDateException;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class EncounterDomainWrapper {
    public static final Predicate NON_VOIDED_PREDICATE = new Predicate() {
        @Override
        public boolean evaluate(Object o) {
            return !((Encounter) o).isVoided();
        }
    };
    public static final Comparator<Encounter> DATETIME_COMPARATOR = new Comparator<Encounter>() {
        @Override
        public int compare(Encounter encounter, Encounter encounter2) {
            return DateTimeComparator.getInstance().compare(encounter.getEncounterDatetime(), encounter2.getEncounterDatetime());
        }
    };

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
     * Convenience getters for underlying encounter properties
     */

    public Visit getVisit() {
        return encounter.getVisit();
    }

    public Location getLocation() {
        return encounter.getLocation();
    }

    public Date getEncounterDatetime() {
        return encounter.getEncounterDatetime();
    }

    public int getEncounterId() {
        return encounter.getEncounterId();
    }

    public Form getForm() {
        return encounter.getForm();
    }

    public Boolean getVoided() {
        return encounter.getVoided();
    }

    public Set<EncounterProvider> getEncounterProviders() {
        return encounter.getEncounterProviders();
    }

    // TODO not sure we are ever going to get this working properly. might want to deprecate?
    public Provider getPrimaryProvider() {
        // TODO for now we just return the first non-voided provider as the primary provider; we should improve this
        for (EncounterProvider provider : encounter.getEncounterProviders()) {
            if (!provider.isVoided()) {
                return provider.getProvider();
            }
        }
        return null;
    }

    /**
     * Verify if a user is the creator or one of the providers in the encounter
     *
     * @param currentUser
     * @return
     */
    public boolean participatedInEncounter(User currentUser) {

        if (verifyIfUserIsTheCreatorOfEncounter(currentUser)) {
            return true;
        } else if (verifyIfUserIsOneOfTheProviders(currentUser)) {
            return true;
        }

        return false;
    }

    private boolean verifyIfUserIsOneOfTheProviders(User currentUser) {
        for (EncounterProvider encounterProvider : encounter.getEncounterProviders()) {
            if (encounterProvider.getProvider().getPerson().equals(currentUser.getPerson())) {
                return true;
            }
        }
        return false;
    }

    private boolean verifyIfUserIsTheCreatorOfEncounter(User currentUser) {
        return encounter.getCreator().equals(currentUser);
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

    /**
     * Associates the encounter with the specified visit
     * If the encounterDatetime has only a Date component, adds a time component (if necessary) based on our business logic:
     * if this is an open visit and encounter date = today, consider a real-tiome transaction and we stamp with the current time,
     * otherwise we add a time component (if necessary) to make sure the encounter falls within the specified visit
     *
     * @param visit
     * @throws EncounterDateBeforeVisitStartDateException
     *
     * @throws EncounterDateAfterVisitStopDateException
     *
     */
    @Transactional
    public void attachToVisit(VisitDomainWrapper visit)
            throws EncounterDateBeforeVisitStartDateException, EncounterDateAfterVisitStopDateException {

        // if time component already exists, for now we just verify that the encounter falls within the visit
        if (dateHasTimeComponent(encounter.getEncounterDatetime())) {

            if (encounter.getEncounterDatetime().before(visit.getStartDatetime())) {
                throw new EncounterDateBeforeVisitStartDateException();
            }

            if (visit.getStopDatetime() != null && encounter.getEncounterDatetime().after(visit.getStopDatetime())) {
                throw new EncounterDateAfterVisitStopDateException();
            }

        }
        // otherwise, properly set the time component
        else {

            DateMidnight encounterDate = new DateMidnight(encounter.getEncounterDatetime());
            DateMidnight currentDate = new DateMidnight();
            DateMidnight visitStartDate = new DateMidnight(visit.getStartDatetime());
            DateMidnight visitStopDate = visit.getStopDatetime() != null ? new DateMidnight(visit.getStopDatetime()) : null;

            if (encounterDate.isBefore(visitStartDate)) {
                throw new EncounterDateBeforeVisitStartDateException();
            }

            if (visitStopDate != null && encounterDate.isAfter(visitStopDate)) {
                throw new EncounterDateAfterVisitStopDateException();
            }

            // if encounter date = today and open visit, consider this a real-time transaction and timestamp with current datetime
            if (encounterDate.equals(currentDate) && visit.isOpen()) {
                encounter.setEncounterDatetime(new Date());
            }

            // otherwise, if encounterDate is before visit start date, set the encounterDatetime to the visit date time
            else if (encounterDate.isBefore(new DateTime(visit.getStartDatetime()))) {
                encounter.setEncounterDatetime(visit.getStartDatetime());
            }

            // otherwise, leave encounter date as is
        }

        // now associate with the visit
        encounter.setVisit(visit.getVisit());
    }

    /**
     * Convenience method to allow one to call attachToVisit with an "unwrapped" visit
     *
     * @param visit
     * @throws EncounterDateBeforeVisitStartDateException
     *
     * @throws EncounterDateAfterVisitStopDateException
     *
     */
    @Transactional
    public void attachToVisit(Visit visit)
            throws EncounterDateBeforeVisitStartDateException, EncounterDateAfterVisitStopDateException {
        attachToVisit(new VisitDomainWrapper(visit));
    }

    private boolean dateHasTimeComponent(Date date) {
        return !new DateTime(date).equals(new DateMidnight(date));
    }
}
