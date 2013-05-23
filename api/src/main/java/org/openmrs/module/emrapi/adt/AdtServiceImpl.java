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
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.merge.PatientMergeAction;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.serialization.SerializationException;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    private AdministrationService administrationService;

    private PatientService patientService;

    private EncounterService encounterService;

    private OrderService orderService;

    private VisitService visitService;

    private ProviderService providerService;

    private LocationService locationService;

    @Autowired(required = false)
    private List<PatientMergeAction> patientMergeActions;

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

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

    public void setAdministrationService(AdministrationService administrationService) {
        this.administrationService = administrationService;
    }

    public void setProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }

    public void setPatientMergeActions(List<PatientMergeAction> patientMergeActions) {
        this.patientMergeActions = patientMergeActions;
    }

    @Override
    public boolean isActive(Visit visit) {
        if (visit.getStopDatetime() != null) {
            return false;
        }

        if (new VisitDomainWrapper(visit, emrApiProperties).isAdmitted()) {
            return true;
        }

        Date now = new Date();
        Date mustHaveSomethingAfter = DateUtils.addHours(now, -emrApiProperties.getVisitExpireHours());

        if (OpenmrsUtil.compare(visit.getStartDatetime(), mustHaveSomethingAfter) >= 0) {
            return true;
        }

        if (visit.getEncounters() != null) {
            for (Encounter candidate : visit.getEncounters()) {
                if (OpenmrsUtil.compare(candidate.getEncounterDatetime(), mustHaveSomethingAfter) >= 0) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void closeInactiveVisits() {
        List<Visit> openVisits = visitService.getVisits(null, null, null, null, null, null, null, null, null, false, false);
        for (Visit visit : openVisits) {
            if (!isActive(visit)) {
                try {
                    closeAndSaveVisit(visit);
                } catch (Exception ex) {
                    log.warn("Failed to close inactive visit " + visit, ex);
                }
            }
        }

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
            if (!isActive(candidate)) {
                if (candidate.getStopDatetime() == null) {
                    closeAndSaveVisit(candidate);
                }
                continue;
            }
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
            visitSummary = new VisitDomainWrapper(activeVisit, emrApiProperties);
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

        Encounter encounter = buildEncounter(emrApiProperties.getCheckInEncounterType(), patient, where, new Date(), obsForCheckInEncounter, ordersForCheckInEncounter);
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

    private Encounter buildEncounter(EncounterType encounterType, Patient patient, Location location, Date when, List<Obs> obsToCreate, List<Order> ordersToCreate) {
        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        encounter.setEncounterType(encounterType);
        encounter.setLocation(location);
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

    /**
     * Looks at location, and if necessary its ancestors in the location hierarchy, until it finds one tagged with
     * "Admission Location"
     *
     * @param location
     * @return location, or an ancestor
     * @throws IllegalArgumentException if neither location nor its ancestors support admissions
     */
    @Override
    public Location getLocationThatSupportsAdmissions(Location location) {
        if (location == null) {
            throw new IllegalArgumentException("Location does not support admissions");
        } else if (location.hasTag(EmrApiConstants.LOCATION_TAG_SUPPORTS_ADMISSION)) {
            return location;
        } else {
            return getLocationThatSupportsAdmissions(location.getParentLocation());
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
            if (isActive(candidate) && itBelongsToARealPatient(candidate)) {
                active.add(new VisitDomainWrapper(candidate, emrApiProperties));
            }
        }

        return active;
    }

    @Override
    public List<VisitDomainWrapper> getInpatientVisits(Location visitLocation) {

        if (visitLocation == null) {
            throw new IllegalArgumentException("Location is required");
        }
        Set<Location> locations = getChildLocationsRecursively(visitLocation, null);
        List<Visit> candidates = visitService.getVisits(null, null, locations, null, null, null, null, null, null, false,
                false);

        List<VisitDomainWrapper> inpatientVisits = new ArrayList<VisitDomainWrapper>();
        for (Visit candidate : candidates) {
            VisitDomainWrapper visitDomainWrapper = new VisitDomainWrapper(candidate, emrApiProperties);
            if (isActive(candidate) && itBelongsToARealPatient(candidate)
                    && visitDomainWrapper.isAdmitted()) {
                inpatientVisits.add(visitDomainWrapper);
            }
        }

        return inpatientVisits;
    }

    private boolean isInpatient(Visit candidate) {
        return false;
    }

    private boolean itBelongsToARealPatient(Visit candidate) {
        Patient patient = candidate.getPatient();
        PatientDomainWrapper domainWrapper = new PatientDomainWrapper(patient, emrApiProperties, null, null, null);
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
        boolean preferredWasUnknown = wrap(preferred).isUnknownPatient();
        boolean notPreferredWasUnknown = wrap(notPreferred).isUnknownPatient();
        if (preferredWasUnknown && !notPreferredWasUnknown) {
            throw new IllegalArgumentException("Cannot merge a permanent record into an unknown one");
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

        if (patientMergeActions != null) {
            for (PatientMergeAction patientMergeAction : patientMergeActions) {
                patientMergeAction.beforeMergingPatients(preferred, notPreferred);
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

        if (patientMergeActions != null) {
            for (PatientMergeAction patientMergeAction : patientMergeActions) {
                patientMergeAction.afterMergingPatients(preferred, notPreferred);
            }
        }

    }

    private PatientDomainWrapper wrap(Patient notPreferred) {
        return new PatientDomainWrapper(notPreferred, emrApiProperties, this, visitService, encounterService);
    }

    private void removeAttributeOfUnknownPatient(Patient preferred) {
        PersonAttributeType unknownPatientPersonAttributeType = emrApiProperties.getUnknownPatientPersonAttributeType();
        PersonAttribute attribute = preferred.getAttribute(unknownPatientPersonAttributeType);
        if (attribute != null) {
            preferred.removeAttribute(attribute);
            patientService.savePatient(preferred);
        }
    }

    private void mergeVisits(Visit preferred, Visit nonPreferred) {
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
        visitService.voidVisit(nonPreferred, "EMR - Merge Patients: merged into visit " + preferred.getVisitId());
        visitService.saveVisit(preferred);
    }

    @Transactional
    @Override
    public Encounter admitPatient(Admission admission) {
        if (admission.getPatient() == null || admission.getLocation() == null || !hasAny(admission.getProviders())) {
            throw new IllegalArgumentException("Must provide a patient, location, and provider");
        }

        VisitDomainWrapper activeVisit = getActiveVisit(admission.getPatient(), admission.getLocation());
        if (activeVisit == null) {
            Visit visit = ensureActiveVisit(admission.getPatient(), admission.getLocation());
            activeVisit = new VisitDomainWrapper(visit, emrApiProperties);
        }

        if (activeVisit.isAdmitted()) {
            throw new IllegalStateException("Patient is already admitted");
        }

        Date admitDatetime = admission.getAdmitDatetime();
        if (admitDatetime == null) {
            admitDatetime = new Date();
        }

        if (activeVisit.getStopDatetime() != null && OpenmrsUtil.compare(activeVisit.getStopDatetime(), admitDatetime) < 0) {
            throw new IllegalArgumentException("Cannot admit patient after the visit has stopped (stopped on " + activeVisit.getStopDatetime() + ")");
        }

        EncounterType admissionEncounterType = emrApiProperties.getAdmissionEncounterType();
        if (admissionEncounterType == null) {
            throw new IllegalStateException("Configuration required: " + EmrApiConstants.GP_ADMISSION_ENCOUNTER_TYPE);
        }

        Encounter encounter = buildEncounter(admissionEncounterType, admission.getPatient(), getLocationThatSupportsAdmissions(admission.getLocation()), admitDatetime, null, null);
        addProviders(encounter, admission.getProviders());

        activeVisit.addEncounter(encounter);
        encounterService.saveEncounter(encounter);
        return encounter;

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

    @Transactional
    @Override
    public Encounter dischargePatient(Discharge discharge) {
        if (discharge.getVisit() == null || discharge.getLocation() == null || !hasAny(discharge.getProviders())) {
            throw new IllegalArgumentException("Must provide a visit, location, and provider");
        }

        VisitDomainWrapper visit = new VisitDomainWrapper(discharge.getVisit(), emrApiProperties);
        if (!visit.isAdmitted()) {
            throw new IllegalStateException("Patient is not currently admitted");
        }

        Date dischargeDatetime = discharge.getDischargeDatetime();
        if (dischargeDatetime == null) {
            dischargeDatetime = new Date();
        }

        visit.errorIfOutsideVisit(dischargeDatetime, "Invalid dischargeDatetime");

        EncounterType dischargeEncounterType = emrApiProperties.getExitFromInpatientEncounterType();
        if (dischargeEncounterType == null) {
            throw new IllegalStateException("Configuration required: " + EmrApiConstants.GP_EXIT_FROM_INPATIENT_ENCOUNTER_TYPE);
        }

        Encounter encounter = buildEncounter(dischargeEncounterType, visit.getVisit().getPatient(), discharge.getLocation(), dischargeDatetime, null, null);
        addProviders(encounter, discharge.getProviders());

        visit.addEncounter(encounter);
        encounterService.saveEncounter(encounter);
        return encounter;
    }

    @Transactional
    @Override
    public Encounter transferPatient(Transfer transfer) {
        if (transfer.getVisit() == null || transfer.getToLocation() == null || !hasAny(transfer.getProviders())) {
            throw new IllegalArgumentException("Must provide visit, toLocation, and provider");
        }

        VisitDomainWrapper visit = new VisitDomainWrapper(transfer.getVisit(), emrApiProperties);

        Date transferDatetime = transfer.getTransferDatetime();
        if (transferDatetime == null) {
            transferDatetime = new Date();
        }

        visit.errorIfOutsideVisit(transferDatetime, "Invalid transferDatetime");

        EncounterType transferWithinHospitalEncounterType = emrApiProperties.getTransferWithinHospitalEncounterType();
        if (transferWithinHospitalEncounterType == null) {
            throw new IllegalStateException("Configuration required: " + EmrApiConstants.GP_TRANSFER_WITHIN_HOSPITAL_ENCOUNTER_TYPE);
        }

        Encounter encounter = buildEncounter(transferWithinHospitalEncounterType, visit.getVisit().getPatient(), transfer.getToLocation(), transferDatetime, null, null);
        addProviders(encounter, transfer.getProviders());

        visit.addEncounter(encounter);
        encounterService.saveEncounter(encounter);
        return encounter;
    }

    @Override
    public VisitDomainWrapper wrap(Visit visit) {
        return new VisitDomainWrapper(visit, emrApiProperties);
    }

}
