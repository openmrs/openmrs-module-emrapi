/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
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
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.adt.exception.EncounterDateAfterVisitStopDateException;
import org.openmrs.module.emrapi.adt.exception.EncounterDateBeforeVisitStartDateException;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EncounterDomainWrapper implements DomainWrapper {

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

    public EncounterDomainWrapper() {
    }

    @Deprecated  // use DomainWrapperFactory
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
                setEncounterDatetimeAndPropagateToObs(encounter, new Date());
            }

            // otherwise, consider a retrospective encounter, and so we want an encounter date to have no time component, EXCEPT
            // if this encounter is on the first day of the visit, the encounter datetime cannot be before the visit start time
            else if (encounterDate.isBefore(new DateTime(visit.getStartDatetime()))) {
                setEncounterDatetimeAndPropagateToObs(encounter, visit.getStartDatetime());
            }

            // otherwise, leave encounter date as is
        }

        // now associate with the visit
        encounter.setVisit(visit.getVisit());
    }

    /**
     * Sets encounter.encounterDatetime, and if any obs in encounter.allObs had an obsDatetime equal to the original
     * encounterDatetime, those obsDatetimes are also set. (I.e. this will change the obsDatetime of obs who
     * "inherit" datetime from the encounter, but not touch obs who have an different obsDatetime.
     * @param encounter
     * @param datetime
     */
    private void setEncounterDatetimeAndPropagateToObs(Encounter encounter, Date datetime) {
        Date original = encounter.getEncounterDatetime();
        encounter.setEncounterDatetime(datetime);

        for (Obs candidate : allObs(encounter)) {
            if (OpenmrsUtil.nullSafeEquals(original, candidate.getObsDatetime())) {
                candidate.setObsDatetime(datetime);
            }
        }
    }

    /**
     * If an encounter has not been persisted yet, we cannot be sure that Encounter.getAllObs() will actually return all
     * obs (because some obs group members may not yet have been attached to the encounter by the service), so we use
     * this helper method to ensure we recursively get all obs in an encounter
     * @param encounter
     * @return
     */
    private Set<Obs> allObs(Encounter encounter) {
        Set<Obs> allObs = new HashSet<Obs>();
        for (Obs o : encounter.getObsAtTopLevel(false)) {
            allObsRecursion(allObs, o);
        }
        return allObs;
    }

    private void allObsRecursion(Set<Obs> allObs, Obs obs) {
        if (!obs.isVoided()) {
            allObs.add(obs);
            if (obs.hasGroupMembers()) {
                for (Obs child : obs.getGroupMembers()) {
                    allObsRecursion(allObs, child);
                }
            }
        }
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
