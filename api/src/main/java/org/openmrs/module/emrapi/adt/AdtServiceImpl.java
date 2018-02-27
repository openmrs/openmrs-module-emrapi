/*
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

package org.openmrs.module.emrapi.adt;

import org.apache.commons.lang.time.DateUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Form;
import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.exception.ExistingVisitDuringTimePeriodException;
import org.openmrs.module.emrapi.disposition.Disposition;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.openmrs.module.emrapi.merge.PatientMergeAction;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.serialization.SerializationException;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class AdtServiceImpl extends BaseOpenmrsService implements AdtService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private EmrApiProperties emrApiProperties;

    private PatientService patientService;

    private EncounterService encounterService;

    private VisitService visitService;

    private ProviderService providerService;

    private LocationService locationService;

    private DomainWrapperFactory domainWrapperFactory;

    private List<PatientMergeAction> patientMergeActions;

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public void setEncounterService(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    public void setVisitService(VisitService visitService) {
        this.visitService = visitService;
    }

    public void setProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }

    public void setDomainWrapperFactory(DomainWrapperFactory domainWrapperFactory) {
        this.domainWrapperFactory = domainWrapperFactory;
    }

    public void setPatientMergeActions(List<PatientMergeAction> patientMergeActions) {
        this.patientMergeActions = patientMergeActions;
    }

    // for testing
    public List<PatientMergeAction> getPatientMergeActions() {
        return patientMergeActions;
    }

    @Override
    public void closeInactiveVisits() {
        Collection<Location> possibleLocations = getPossibleLocationsToCloseVisit();
        List<Visit> openVisits = visitService.getVisits(null, null, possibleLocations, null, null, null, null, null, null, false, false);
            for (Visit visit : openVisits) {
                if (shouldBeClosed(visit)) {
                    try {
                        closeAndSaveVisit(visit);
                    } catch (Exception ex) {
                        log.warn("Failed to close inactive visit " + visit, ex);
                    }
                }
        }
    }

    private Collection<Location> getPossibleLocationsToCloseVisit() {
        LocationTag visitLocationTag =  locationService.getLocationTagByName(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS);

        return locationService.getLocationsByTag(visitLocationTag);
    }

    @Override
    public boolean shouldBeClosed(Visit visit) {

        if (visit.getStopDatetime() != null) {
            return false;  // already closed
        }

        VisitDomainWrapper visitDomainWrapper = domainWrapperFactory.newVisitDomainWrapper(visit);

        if (visitDomainWrapper.isAdmitted() || visitDomainWrapper.isAwaitingAdmission()) {
            return false;  // don't close the visit if patient is admitted or waiting admission
        }

        Disposition mostRecentDisposition = visitDomainWrapper.getMostRecentDisposition();
        if (mostRecentDisposition != null && mostRecentDisposition.getKeepsVisitOpen() != null && mostRecentDisposition.getKeepsVisitOpen()) {
            return false; // don't close the visit if the most recent disposition is one that keeps visit opens
        }

        Date now = new Date();
        Date mustHaveSomethingAfter = DateUtils.addHours(now, -emrApiProperties.getVisitExpireHours());

        if (OpenmrsUtil.compare(visit.getStartDatetime(), mustHaveSomethingAfter) >= 0) {
            return false;
        }

        if (visit.getEncounters() != null) {
            for (Encounter candidate : visit.getEncounters()) {
                if (!candidate.isVoided() && OpenmrsUtil.compare(candidate.getEncounterDatetime(), mustHaveSomethingAfter) >= 0) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean visitsOverlap(Visit v1, Visit v2) {
        Location where1 = v1.getLocation();
        Location where2 = v2.getLocation();
        if ((where1 == null && where2 == null) ||
                isSameOrAncestor(where1, where2) ||
                isSameOrAncestor(where2, where1)) {
            // "same" location, so check if date ranges overlap (assuming startDatetime is never null)
            return (OpenmrsUtil.compareWithNullAsLatest(v1.getStartDatetime(), v2.getStopDatetime()) <= 0)
                    && (OpenmrsUtil.compareWithNullAsLatest(v2.getStartDatetime(), v1.getStopDatetime()) <= 0);
        }
        return false;
    }

    /**
     * Anything that calls this needs to be @Transactional
     *
     * @param patient
     * @param department
     * @return
     */
    private Visit getActiveVisitHelper(Patient patient, Location department) {
        Date now = new Date();

        List<Visit> candidates = visitService.getVisitsByPatient(patient);
        Visit ret = null;
        for (Visit candidate : candidates) {
            if (isSuitableVisit(candidate, department, now)) {
                ret = candidate;
            }
        }

        return ret;
    }

    @Override
    @Transactional
    public VisitDomainWrapper getActiveVisit(Patient patient, Location location) {
        VisitDomainWrapper visitSummary = null;
        Visit activeVisit = getActiveVisitHelper(patient, location);
        if (activeVisit != null) {
            visitSummary = wrap(activeVisit);
        }
        return visitSummary;
    }

    @Override
    @Transactional
    public void closeAndSaveVisit(Visit visit) {
        visit.setStopDatetime(guessVisitStopDatetime(visit));
        visitService.saveVisit(visit);
    }

    @Override
    @Transactional
    public Visit ensureActiveVisit(Patient patient, Location department) {
        Visit activeVisit = getActiveVisitHelper(patient, department);
        if (activeVisit == null) {
            Date now = new Date();
            activeVisit = buildVisit(patient, department, now);
            visitService.saveVisit(activeVisit);
        }
        return activeVisit;
    }

    @Transactional
    public Visit ensureVisit(Patient patient, Date visitTime, Location department){
        if (visitTime == null) {
            visitTime = new Date();
        }
        Visit visit = null;
        List<Patient> patientList = Collections.singletonList(patient);

        // visits that have not ended by the encounter date.
        List<Visit> candidates = visitService.getVisits(null, patientList, null, null, null,
                visitTime, null, null, null, true, false);
        if(candidates != null){
            for (Visit candidate : candidates) {
                if (isSuitableVisit(candidate, department, visitTime)) {
                    return candidate;
                }
            }
        }
        if(visit == null){
            visit = buildVisit(patient, department, visitTime);
            visitService.saveVisit(visit);
        }
        return visit;
    }

    private Date guessVisitStopDatetime(Visit visit) {
        if (visit.getEncounters() == null || visit.getEncounters().size() == 0) {
            return visit.getStartDatetime();
        }

        Iterator<Encounter> iterator = visit.getEncounters().iterator();
        Encounter latest = iterator.next();
        while (iterator.hasNext()) {
            Encounter candidate = iterator.next();
            if (OpenmrsUtil.compare(candidate.getEncounterDatetime(), latest.getEncounterDatetime()) > 0) {
                latest = candidate;
            }
        }
        return latest.getEncounterDatetime();
    }

    /**
     * This method is synchronized to prevent multiple check-ins in a row at the same location and during the same visit.
     * See #579.
     * 
     * @see org.openmrs.module.emrapi.adt.AdtService#checkInPatient(org.openmrs.Patient, org.openmrs.Location, org.openmrs.Provider, java.util.List, java.util.List, boolean)
     */
    @Override
    @Transactional
    public synchronized Encounter checkInPatient(Patient patient, Location where, Provider checkInClerk,
                                    List<Obs> obsForCheckInEncounter, List<Order> ordersForCheckInEncounter, boolean newVisit) {
        if (checkInClerk == null) {
            checkInClerk = getProvider(Context.getAuthenticatedUser());
        }

        Visit activeVisit = getActiveVisitHelper(patient, where);

        if (activeVisit != null && newVisit) {
            closeAndSaveVisit(activeVisit);
            activeVisit = null;
        }

        if (activeVisit == null) {
            activeVisit = ensureActiveVisit(patient, where);
        }

        Encounter lastEncounter = getLastEncounter(patient);
		if (lastEncounter != null && activeVisit.equals(lastEncounter.getVisit())
		        && emrApiProperties.getCheckInEncounterType().equals(lastEncounter.getEncounterType())
		        && where.equals(lastEncounter.getLocation())) {
			log.warn("Patient id:{} tried to check-in twice in a row at id:{} during the same visit", patient.getId(), where.getId());
			return lastEncounter;
		}

        Encounter encounter = buildEncounter(emrApiProperties.getCheckInEncounterType(), patient, where, null, new Date(), obsForCheckInEncounter, ordersForCheckInEncounter);
        encounter.addProvider(emrApiProperties.getCheckInClerkEncounterRole(), checkInClerk);
        activeVisit.addEncounter(encounter);
        encounterService.saveEncounter(encounter);
        return encounter;
    }

// Commenting this out since the feature isn't in use yet, and it refers to payment, which isn't supposed to be in this module
//    @Override
//    @Transactional
//    public Encounter createCheckinInRetrospective(Patient patient, Location location, Provider clerk, Obs paymentReason, Obs paymentAmount, Obs paymentReceipt, Date checkinDate) {
//        Visit encounterVisit = buildVisit(patient, location, checkinDate);
//
//        List<Obs> paymentObservations = new ArrayList<Obs>();
//        Obs paymentGroup = new Obs();
//        paymentGroup.setConcept(emrApiProperties.getPaymentConstructConcept());
//        paymentGroup.addGroupMember(paymentReason);
//        paymentGroup.addGroupMember(paymentAmount);
//        paymentGroup.addGroupMember(paymentReceipt);
//        paymentObservations.add(paymentGroup);
//        Encounter checkinEncounter = buildEncounter(emrApiProperties.getCheckInEncounterType(), patient, location, checkinDate, paymentObservations, null);
//        checkinEncounter.addProvider(emrApiProperties.getCheckInClerkEncounterRole(), clerk);
//        encounterVisit.addEncounter(checkinEncounter);
//
//        checkinEncounter = encounterService.saveEncounter(checkinEncounter);
//
//        return checkinEncounter;
//    }


    private Provider getProvider(User accountBelongingToUser) {
        Collection<Provider> candidates = providerService.getProvidersByPerson(accountBelongingToUser.getPerson(), false);
        if (candidates.size() == 0) {
            throw new IllegalStateException("User " + accountBelongingToUser.getUsername() + " does not have a Provider account");
        } else if (candidates.size() > 1) {
            throw new IllegalStateException("User " + accountBelongingToUser.getUsername() + " has more than one Provider account");
        } else {
            return candidates.iterator().next();
        }
    }

    private Encounter buildEncounter(EncounterType encounterType, Patient patient, Location location, Form form, Date when, List<Obs> obsToCreate, List<Order> ordersToCreate) {
        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setEncounterType(encounterType);
        encounter.setLocation(location);
        encounter.setForm(form);
        encounter.setEncounterDatetime(when);
        if (obsToCreate != null) {
            for (Obs obs : obsToCreate) {
                obs.setObsDatetime(new Date());
                encounter.addObs(obs);
            }
        }
        if (ordersToCreate != null) {
            for (Order order : ordersToCreate) {
                encounter.addOrder(order);
            }
        }
        return encounter;
    }

    private Visit buildVisit(Patient patient, Location location, Date when) {
        Visit visit = new Visit();
        visit.setPatient(patient);
        visit.setLocation(getLocationThatSupportsVisits(location));
        visit.setStartDatetime(when);
        visit.setVisitType(emrApiProperties.getAtFacilityVisitType());
        return visit;
    }

    /**
     * Looks at location, and if necessary its ancestors in the location hierarchy, until it finds one tagged with
     * "Visit Location"
     *
     * @param location
     * @return location, or an ancestor
     * @throws IllegalArgumentException if neither location nor its ancestors support visits
     */
    @Override
    public Location getLocationThatSupportsVisits(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location does not support visits");
        } else if (location.hasTag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS)) {
            return location;
        } else {
            return getLocationThatSupportsVisits(location.getParentLocation());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Location> getAllLocationsThatSupportVisits() {
        return locationService.getLocationsByTag(emrApiProperties.getSupportsVisitsLocationTag());
    }

    /**
     * @param visit
     * @param location
     * @param when
     * @return true if when falls in the visits timespan AND location is within visit.location
     */
    @Override
    public boolean isSuitableVisit(Visit visit, Location location, Date when) {
        if (OpenmrsUtil.compare(when, visit.getStartDatetime()) < 0) {
            return false;
        }
        if (OpenmrsUtil.compareWithNullAsLatest(when, visit.getStopDatetime()) > 0) {
            return false;
        }
        return isSameOrAncestor(visit.getLocation(), location);
    }

    /**
     * @param a
     * @param b
     * @return true if a.equals(b) or a is an ancestor of b.
     */
    private boolean isSameOrAncestor(Location a, Location b) {
        if (a == null || b == null) {
            return a == null && b == null;
        }
        return a.equals(b) || isSameOrAncestor(a, b.getParentLocation());
    }

    /**
     * @see org.openmrs.module.emrapi.adt.AdtService#getActiveVisits(org.openmrs.Location)
     */
    @Override
    public List<VisitDomainWrapper> getActiveVisits(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location is required");
        }
        Set<Location> locations = getChildLocationsRecursively(location, null);
        List<Visit> candidates = visitService.getVisits(null, null, locations, null, null, null, null, null, null, false,
                false);

        List<VisitDomainWrapper> active = new ArrayList<VisitDomainWrapper>();
        for (Visit candidate : candidates) {
            if (itBelongsToARealPatient(candidate)) {
                active.add(wrap(candidate));
            }
        }

        return active;
    }

    @Override
    public List<VisitDomainWrapper> getInpatientVisits(Location visitLocation, Location ward) {

        if (visitLocation == null) {
            throw new IllegalArgumentException("Location is required");
        }
        Set<Location> locations = getChildLocationsRecursively(visitLocation, null);
        List<Visit> candidates = visitService.getVisits(null, null, locations, null, null, null, null, null, null, false,
                false);

        List<VisitDomainWrapper> inpatientVisits = new ArrayList<VisitDomainWrapper>();
        for (Visit candidate : candidates) {
            VisitDomainWrapper visitDomainWrapper = wrap(candidate);
            if (itBelongsToARealPatient(candidate)
                    && visitDomainWrapper.isAdmitted()) {
                if(ward!=null){
                    Encounter latestAdtEncounter = visitDomainWrapper.getLatestAdtEncounter();
                    if(latestAdtEncounter!=null &&
                            ( latestAdtEncounter.getLocation().getId().compareTo(ward.getId())==0 )){
                        inpatientVisits.add(visitDomainWrapper);
                    }
                }else{
                    inpatientVisits.add(visitDomainWrapper);
                }
            }
        }

        return inpatientVisits;
    }

    private boolean itBelongsToARealPatient(Visit candidate) {
        Patient patient = candidate.getPatient();
        PatientDomainWrapper domainWrapper = new PatientDomainWrapper(patient, emrApiProperties, null, null, null, null, null);
        return !domainWrapper.isTestPatient();
    }

    @Override
    public Encounter getLastEncounter(Patient patient) {
        // speed this up by implementing it directly in a DAO
        List<Encounter> byPatient = encounterService.getEncountersByPatient(patient);
        if (byPatient.size() == 0) {
            return null;
        } else {
            return byPatient.get(byPatient.size() - 1);
        }
    }

    @Override
    public int getCountOfEncounters(Patient patient) {
        // speed this up by implementing it directly in a DAO
        return encounterService.getEncountersByPatient(patient).size();
    }

    @Override
    public int getCountOfVisits(Patient patient) {
        // speed this up by implementing it directly in a DAO
        return visitService.getVisitsByPatient(patient, true, false).size();
    }

    /**
     * Utility method that returns all child locations and children of its child locations
     * recursively
     *
     * @param location
     * @param foundLocations
     * @return
     */
    private Set<Location> getChildLocationsRecursively(Location location, Set<Location> foundLocations) {
        if (foundLocations == null)
            foundLocations = new LinkedHashSet<Location>();

        foundLocations.add(location);

        if (location.getChildLocations() != null) {
            for (Location l : location.getChildLocations()) {
                foundLocations.add(l);
                getChildLocationsRecursively(l, foundLocations);
            }
        }

        return foundLocations;
    }

    @Transactional
    @Override
    public void mergePatients(Patient preferred, Patient notPreferred) {
        boolean preferredWasUnknown = domainWrapperFactory.newPatientDomainWrapper(preferred).isUnknownPatient();
        boolean notPreferredWasUnknown = domainWrapperFactory.newPatientDomainWrapper(notPreferred).isUnknownPatient();
        if (preferredWasUnknown && !notPreferredWasUnknown) {
            throw new IllegalArgumentException("Cannot merge a permanent record into an unknown one");
        }

        // do any "before-merge actions" that have been registered
        if (patientMergeActions != null) {
            for (PatientMergeAction patientMergeAction : patientMergeActions) {
                patientMergeAction.beforeMergingPatients(preferred, notPreferred);
            }
        }

        List<Visit> preferredVisits = visitService.getVisitsByPatient(preferred, true, false);
        List<Visit> notPreferredVisits = visitService.getVisitsByPatient(notPreferred, true, false);

        // if the non-preferred patient has any visits that overlap with visits of the preferred patient, we need to merge them together
        for (Visit losing : notPreferredVisits) {
            if (!losing.isVoided()) {
                for (Visit winning : preferredVisits) {
                    if (!winning.isVoided() && visitsOverlap(losing, winning)) {
                        mergeVisits(winning, losing);
                        break;
                    }
                }
            }
        }

        // merging in visits from the non-preferred patient (and extending visit durations) may have caused preferred-patient visits to overlap
        Collections.sort(preferredVisits, new Comparator<Visit>() {
            @Override
            public int compare(Visit left, Visit right) {
                return OpenmrsUtil.compareWithNullAsEarliest(left.getStartDatetime(), right.getStartDatetime());
            }
        });
        for (int i = 0; i < preferredVisits.size(); ++i) {
            Visit visit = preferredVisits.get(i);
            if (!visit.isVoided()) {
                for (int j = i + 1; j < preferredVisits.size(); ++j) {
                    Visit candidate = preferredVisits.get(j);
                    if (!candidate.isVoided() && visitsOverlap(visit, candidate)) {
                        mergeVisits(visit, candidate);
                    }
                }
            }
        }

        try {
            patientService.mergePatients(preferred, notPreferred);
            // if we merged an unknown record into a permanent one, remove the unknown flag; if we merged two unknown records, keep it
            if (!preferredWasUnknown) {
                removeAttributeOfUnknownPatient(preferred);
            }
        } catch (SerializationException e) {
            throw new APIException("Unable to merge patients due to serialization error", e);
        }

        // do any "after-merge actions" that have been registered
        if (patientMergeActions != null) {
            for (PatientMergeAction patientMergeAction : patientMergeActions) {
                patientMergeAction.afterMergingPatients(preferred, notPreferred);
            }
        }

    }

    private void removeAttributeOfUnknownPatient(Patient preferred) {
        PersonAttributeType unknownPatientPersonAttributeType = emrApiProperties.getUnknownPatientPersonAttributeType();
        PersonAttribute attribute = preferred.getAttribute(unknownPatientPersonAttributeType);
        if (attribute != null) {
            preferred.removeAttribute(attribute);
            patientService.savePatient(preferred);
        }
    }

    public  boolean areConsecutiveVisits(List<Integer> visits, Patient patient){
        if (patient != null && visits != null && (visits.size() > 0) ){
            List<Visit> patientVisits = visitService.getVisitsByPatient(patient, true, false);
            if ( (patientVisits != null) && (patientVisits.size() > 0) ){
                ArrayList<Integer> allVisits = new ArrayList<Integer>();
                int j = 0;
                for (Visit visit : patientVisits){
                    allVisits.add(j++, visit.getId());
                }
                if (allVisits.containsAll(visits) ){
                    //find the index of the first candidate for a consecutive visit
                    int i = allVisits.indexOf(visits.get(0));
                    //make sure there are still more elements in the list than the the number of candidate consecutives
                    if ((allVisits.size() - i) >= visits.size()){
                        for (Integer candidateVisit : visits){
                            if (allVisits.get(i).compareTo(candidateVisit) == 0 ){
                                i++;
                            }else{
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Visit mergeConsecutiveVisits(List<Integer> visits, Patient patient){
        if (areConsecutiveVisits(visits, patient) ) {
            Visit mergedVisit = visitService.getVisit(visits.get(0));
            if (visits.size() > 1){
                for (int i =1; i < visits.size(); i++){
                    mergedVisit = mergeVisits(mergedVisit, visitService.getVisit(visits.get(i)));
                }
            }
            return mergedVisit;
        }
        return null;
    }
    public Visit mergeVisits(Visit preferred, Visit nonPreferred) {
        // extend date range of winning
        if (OpenmrsUtil.compareWithNullAsEarliest(nonPreferred.getStartDatetime(), preferred.getStartDatetime()) < 0) {
            preferred.setStartDatetime(nonPreferred.getStartDatetime());
        }
        if (preferred.getStopDatetime() != null && OpenmrsUtil.compareWithNullAsLatest(preferred.getStopDatetime(), nonPreferred.getStopDatetime()) < 0) {
            preferred.setStopDatetime(nonPreferred.getStopDatetime());
        }

        // move encounters from losing into winning
        if (nonPreferred.getEncounters() != null) {
            for (Encounter e : nonPreferred.getEncounters()) {
                e.setPatient(preferred.getPatient());
                preferred.addEncounter(e);
                encounterService.saveEncounter(e);
            }
        }
        nonPreferred.setEncounters(null); // we need to manually the encounters from the non-preferred visit before voiding or all the encounters we just moved will also get voided!

        visitService.voidVisit(nonPreferred, "EMR - Merge Patients: merged into visit " + preferred.getVisitId());
        visitService.saveVisit(preferred);
        return preferred;
    }

    private void addProviders(Encounter encounter, Map<EncounterRole, ? extends Collection<Provider>> providers) {
        for (Map.Entry<EncounterRole, ? extends Collection<Provider>> entry : providers.entrySet()) {
            EncounterRole encounterRole = entry.getKey();
            for (Provider provider : entry.getValue()) {
                encounter.addProvider(encounterRole, provider);
            }
        }
    }

    private boolean hasAny(Map<?, ? extends Collection<?>> providers) {
        if (providers == null) {
            return false;
        }
        for (Collection<?> byType : providers.values()) {
            if (byType != null && byType.size() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addPatientMergeAction(PatientMergeAction patientMergeAction) {
        if (this.patientMergeActions == null) {
            this.patientMergeActions = new ArrayList<PatientMergeAction>();
        }
        this.patientMergeActions.add(patientMergeAction);
    }

    @Override
    public void removePatientMergeAction(PatientMergeAction patientMergeAction) {
        if (this.patientMergeActions == null) {
            this.patientMergeActions = new ArrayList<PatientMergeAction>();
        }
        this.patientMergeActions.remove(patientMergeAction);
    }

    @Transactional
    @Override
    public Encounter createAdtEncounterFor(AdtAction action) {
        if (action.getVisit() == null || action.getLocation() == null || !hasAny(action.getProviders())) {
            throw new IllegalArgumentException("Must provide a visit, location, and provider");
        }

        VisitDomainWrapper visit = wrap(action.getVisit()) ;

        action.getType().checkVisitValid(visit);

        Date adtDatetime = action.getActionDatetime();
        if (adtDatetime == null) {
            adtDatetime = new Date();
        }

        visit.errorIfOutsideVisit(adtDatetime, "ADT Datetime outside of visit bounds");

        EncounterType adtEncounterType = action.getType().getEncounterType(emrApiProperties);
        Form adtForm = action.getType().getForm(emrApiProperties);

        Encounter encounter = buildEncounter(adtEncounterType, visit.getVisit().getPatient(), action.getLocation(), adtForm, adtDatetime, null, null);
        addProviders(encounter, action.getProviders());

        visit.addEncounter(encounter);
        encounterService.saveEncounter(encounter);
        return encounter;
    }

    @Override
    @Deprecated  // use new VisitDomainWrapperFactory instead (this service method has been delegated to use the new factory)
    public VisitDomainWrapper wrap(Visit visit) {
        return domainWrapperFactory.newVisitDomainWrapper(visit);
    }

    @Override
    @Transactional
    public VisitDomainWrapper createRetrospectiveVisit(Patient patient, Location location, Date startDatetime, Date stopDatetime)
        throws ExistingVisitDuringTimePeriodException {

        if (startDatetime.after(new Date())) {
            throw new IllegalArgumentException("emrapi.retrospectiveVisit.startDateCannotBeInFuture");
        }

        if (stopDatetime.after(new Date())) {
            throw new IllegalArgumentException("emrapi.retrospectiveVisit.stopDateCannotBeInFuture");
        }

        if (startDatetime.after(stopDatetime)) {
            throw new IllegalArgumentException("emrapi.retrospectiveVisit.endDateBeforeStartDateMessage");
        }

        if (hasVisitDuring(patient, location, startDatetime, stopDatetime)) {
            throw new ExistingVisitDuringTimePeriodException("emrapi.retrospectiveVisit.patientAlreadyHasVisit");
        }

        Visit visit = buildVisit(patient, location, startDatetime);
        visit.setStopDatetime(stopDatetime);

        return wrap(visitService.saveVisit(visit));
    }

    @Override
    @Transactional(readOnly = true)
    public List<VisitDomainWrapper> getVisits(Patient patient, Location location, Date startDatetime, Date endDatetime) {

        List<VisitDomainWrapper> visitDomainWrappers = new ArrayList<VisitDomainWrapper>();

        for (Visit visit : visitService.getVisits(Collections.singletonList(emrApiProperties.getAtFacilityVisitType()),
                Collections.singletonList(patient), Collections.singletonList(getLocationThatSupportsVisits(location)), null,
                null, endDatetime, startDatetime, null, null, true, false)) {
            visitDomainWrappers.add(wrap(visit));
        }

        return visitDomainWrappers;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasVisitDuring(Patient patient, Location location, Date startDatetime, Date stopDatetime) {
        List<VisitDomainWrapper> visits = getVisits(patient, location, startDatetime, stopDatetime);
        return visits == null || visits.size() == 0 ? false : true;
    }

    @Override
    public List<Location> getInpatientLocations() {
        return locationService.getLocationsByTag(emrApiProperties.getSupportsAdmissionLocationTag());
    }

}
