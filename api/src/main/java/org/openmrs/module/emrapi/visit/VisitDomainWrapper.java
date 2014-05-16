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
package org.openmrs.module.emrapi.visit;


import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.disposition.Disposition;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.disposition.DispositionType;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.util.OpenmrsUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.reverseOrder;
import static java.util.Collections.sort;
import static org.apache.commons.collections.CollectionUtils.find;
import static org.apache.commons.collections.CollectionUtils.select;

/**
 * Wrapper around a Visit, that provides convenience methods to find particular encounters of interest.
 */
public class VisitDomainWrapper {
    private static final Log log = LogFactory.getLog(VisitDomainWrapper.class);

    private EmrApiProperties emrApiProperties;

    private DispositionService dispositionService;

    private Visit visit;

    @Deprecated
    public VisitDomainWrapper(Visit visit) {
        this.visit = visit;
    }

    @Deprecated
    public VisitDomainWrapper(Visit visit, EmrApiProperties emrApiProperties) {
        this.visit = visit;
        this.emrApiProperties = emrApiProperties;
    }

    public VisitDomainWrapper(Visit visit, EmrApiProperties emrApiProperties, DispositionService dispositionService) {
        this.visit = visit;
        this.emrApiProperties = emrApiProperties;
        this.dispositionService = dispositionService;
    }


    /**
     * @return the visit
     */
    public Visit getVisit() {
        return visit;
    }

    public int getVisitId() {
        return visit.getVisitId();
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public Encounter getAdmissionEncounter() {
        return (Encounter) find(getSortedEncounters(), new EncounterTypePredicate(emrApiProperties.getAdmissionEncounterType()));
    }

    // TODO: refactor this to use EncounterTypePredicate
    public Encounter getLatestAdtEncounter(){
        for (Encounter e : getSortedEncounters()) {
            if (emrApiProperties.getAdmissionEncounterType().equals(e.getEncounterType()) ||
                    emrApiProperties.getTransferWithinHospitalEncounterType().equals(e.getEncounterType()) )
                return e;
        }
        return null;
    }

    public boolean isActive() {
        return visit.getStopDatetime() == null;
    }

    @Deprecated  // renamed to is Active
    public boolean isOpen() {
        return isActive();
    }

    public Encounter getCheckInEncounter() {
        return (Encounter) find(getSortedEncounters(), new EncounterTypePredicate(emrApiProperties.getCheckInEncounterType()));
    }

    public Encounter getMostRecentEncounter() {
        List<Encounter> encounters = getSortedEncounters();
        if (encounters.size() > 0)
            return encounters.get(0);
        return null;
    }

    @Deprecated //use getMostRecentEncounter because this method name doesn't make sense
    public Encounter getLastEncounter() {
        return getMostRecentEncounter();
    }

    public Encounter getOldestEncounter() {
        List<Encounter> encounters = getSortedEncounters();
        if (encounters.size() != 0)
            return encounters.get(encounters.size() - 1);
        return null;
    }

    /**
     * Fetches the most recent (non-voided) visit note encounter from this visit
     * @return
     */
    public Encounter getMostRecentVisitNote() {
        return (Encounter) find(getSortedEncounters(), new EncounterTypePredicate(emrApiProperties.getVisitNoteEncounterType()));
    }

    /**
     * Fetches the most recent (non-voided) visit note encounter from this visit at the  specified location
     * @return
     */
    public Encounter getMostRecentVisitNoteAtLocation(Location location) {
        return (Encounter) find(getSortedEncounters(), new EncounterTypeAndLocationPredicate(emrApiProperties.getVisitNoteEncounterType(), location));

    }

    /**
     * True/false whether there is a non-voided visit encounter associated with this visit
     * @return
     */
    public boolean hasVisitNote() {
        return getMostRecentVisitNote() != null;
    }

    /**
     * True/false whether there is a non-voided visit encounter associated with this visit at the specified location
     * @return
     *
     */
    public boolean hasVisitNoteAtLocation(Location location) {
        return getMostRecentVisitNoteAtLocation(location) != null;
    }


    // note that this returns the most recent encounter first
    public List<Encounter> getSortedEncounters() {
        if (visit.getEncounters() != null) {
            List<Encounter> nonVoidedEncounters = (List<Encounter>) select(visit.getEncounters(), EncounterDomainWrapper.NON_VOIDED_PREDICATE);
            sort(nonVoidedEncounters, reverseOrder(EncounterDomainWrapper.DATETIME_COMPARATOR));
            return nonVoidedEncounters;
        }
        return EMPTY_LIST;
    }

    public int getDifferenceInDaysBetweenCurrentDateAndStartDate() {
        Date today = Calendar.getInstance().getTime();

        Calendar startDateVisit = getStartDateVisit();

        int millisecondsInADay = 1000 * 60 * 60 * 24;

        return (int) ((today.getTime() - startDateVisit.getTimeInMillis()) / millisecondsInADay);
    }

    // note that the disposition must be on the top level for this to pick it up
    // (seemed like this made sense to do for performance reasons)
    // also, if encounter has multiple disposition (is this possible?) it just returns the first one it finds
    public Disposition getMostRecentDisposition() {

        DispositionDescriptor dispositionDescriptor = dispositionService.getDispositionDescriptor();

        for (Encounter encounter : getSortedEncounters()) {
            for (Obs obs : encounter.getObsAtTopLevel(false)) {
                if (dispositionDescriptor.isDisposition(obs)) {
                    return dispositionService.getDispositionFromObsGroup(obs);
                }
            }
        }

        return null;
    }

    /**
     * Finds the most recent encounter in the visit with a disposition of a specific type, and retrieves
     * all diagnoses stored on that encounter. Can be used to prepopulate an admission, transfer, or discharge note;
     * example workflow: a doctor writes a visit note and on that note sets a disposition of admission; later on,
     * another doctor writes the actual admission note; that doctor may want a way to prepopulate this
     * note with the dispositions from the visit note recommending admission; see the EncounterDisposition
     * HFE tag in CoreApps for an example of how this method is used
     */
    public List<Diagnosis> getDiagnosesFromMostRecentDispositionByType(DispositionType type) {

        DispositionDescriptor dispositionDescriptor = dispositionService.getDispositionDescriptor();

        for (Encounter encounter : getSortedEncounters()) {  // getSortedEncounters already excludes voided encounters
            for (Obs obs : encounter.getObsAtTopLevel(false)) {
                if (dispositionDescriptor.isDisposition(obs)
                        && dispositionService.getDispositionFromObsGroup(obs).getType() == type) {
                    return getDiagnosesFromEncounter(encounter);
                }
            }
        }

        return new ArrayList<Diagnosis>();  // return empty list if no matches
    }

    public List<Diagnosis> getPrimaryDiagnoses() {
        List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
        for (Encounter encounter : getSortedEncounters()) {
            diagnoses.addAll(getDiagnosesFromEncounter(encounter, Collections.singletonList(Diagnosis.Order.PRIMARY)));
        }
        return diagnoses;
    }

    private List<Diagnosis> getDiagnosesFromEncounter(Encounter encounter) {
        return getDiagnosesFromEncounter(encounter, null);
    }

    private List<Diagnosis> getDiagnosesFromEncounter(Encounter encounter, List<Diagnosis.Order> diagnosisOrders) {
        DiagnosisMetadata diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
        List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();

        for (Obs obs : encounter.getObsAtTopLevel(false)) {
            if (diagnosisMetadata.isDiagnosis(obs)) {
                try {
                    Diagnosis diagnosis = diagnosisMetadata.toDiagnosis(obs);
                    if (diagnosisOrders == null || diagnosisOrders.contains(diagnosis.getOrder())) {
                        diagnoses.add(diagnosis);
                    }
                } catch (Exception ex) {
                    log.warn("malformed diagnosis obs group with obsId " + obs.getObsId(), ex);
                }
            }
        }
        return diagnoses;
    }

    private Calendar getStartDateVisit() {
        Date startDatetime = visit.getStartDatetime();
        Calendar startDateCalendar = Calendar.getInstance();
        startDateCalendar.setTime(startDatetime);
        startDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startDateCalendar.set(Calendar.MINUTE, 0);
        startDateCalendar.set(Calendar.SECOND, 0);
        return startDateCalendar;
    }

    public boolean hasEncounters(){
        List<Encounter> encounters = getSortedEncounters();
        if (encounters != null && encounters.size() > 0){
            return true;
        }
        return false;
    }

    public boolean hasEncounterWithoutSubsequentEncounter(EncounterType lookForEncounterType, EncounterType withoutSubsequentEncounterType) {
        return hasEncounterWithoutSubsequentEncounter(lookForEncounterType, withoutSubsequentEncounterType, null);
    }

    private boolean hasEncounterWithoutSubsequentEncounter(EncounterType lookForEncounterType, EncounterType withoutSubsequentEncounterType, Date onDate) {

        if (visit.getEncounters() == null) {
            return false;
        }

        for (Encounter encounter : getSortedEncounters()) {
            if (onDate == null || encounter.getEncounterDatetime().before(onDate) || encounter.getEncounterDatetime().equals(onDate)) {
                if (encounter.getEncounterType().equals(lookForEncounterType)) {
                    return true;
                }
                else if (encounter.getEncounterType().equals(withoutSubsequentEncounterType)) {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * @return true if the visit includes an admission encounter with no discharge encounter after it
     */
    public boolean isAdmitted() {
        EncounterType admissionEncounterType = emrApiProperties.getAdmissionEncounterType();
        EncounterType dischargeEncounterType = emrApiProperties.getExitFromInpatientEncounterType();
        if (admissionEncounterType == null) {
            return false;
        }
        return hasEncounterWithoutSubsequentEncounter(admissionEncounterType, dischargeEncounterType);
    }

    public boolean isAdmitted(Date onDate) {

        if (visit.getStartDatetime().after(onDate) || (visit.getStopDatetime() != null && visit.getStopDatetime().before(onDate))) {
            throw new IllegalArgumentException("date does not fall within visit");
        }

        EncounterType admissionEncounterType = emrApiProperties.getAdmissionEncounterType();
        EncounterType dischargeEncounterType = emrApiProperties.getExitFromInpatientEncounterType();
        if (admissionEncounterType == null) {
            return false;
        }

        return hasEncounterWithoutSubsequentEncounter(admissionEncounterType, dischargeEncounterType, onDate);
    }

    public Location getInpatientLocation(Date onDate) {

        if (!isAdmitted(onDate)) {
            return null;
        }

        EncounterType admissionEncounterType = emrApiProperties.getAdmissionEncounterType();
        EncounterType transferEncounterType = emrApiProperties.getTransferWithinHospitalEncounterType();

        for (Encounter encounter : getSortedEncounters()) {
            if (onDate == null || encounter.getEncounterDatetime().before(onDate) || encounter.getEncounterDatetime().equals(onDate)) {
                if (encounter.getEncounterType().equals(admissionEncounterType) ||
                        encounter.getEncounterType().equals(transferEncounterType)) {
                    return encounter.getLocation();
                }
            }
        }

        // should never get here if isAdmitted == true
        return null;
    }

    public Date getStartDatetime() {
        return visit.getStartDatetime();
    }

    public Date getStopDatetime() {
        return visit.getStopDatetime();
    }

    /**
     * @param encounter
     * @return this, for call chaining
     */
    public VisitDomainWrapper addEncounter(Encounter encounter) {
        visit.addEncounter(encounter);
        return this;
    }

    public void closeOnLastEncounterDatetime() {

        Encounter mostRecentEncounter = getMostRecentEncounter();

        if (mostRecentEncounter == null) {
            throw new IllegalStateException("Visit has no encounters");
        }

        visit.setStopDatetime(mostRecentEncounter.getEncounterDatetime());
    }

    /**
     * Throws an {@link IllegalArgumentException} if checkDatetime is not within the start/stop date bounds of this visit
     * @param checkDatetime
     * @param errorMessage base of the error message to throw (some details may be added)
     */
    public void errorIfOutsideVisit(Date checkDatetime, String errorMessage) throws IllegalArgumentException {
        if (visit.getStartDatetime() != null && OpenmrsUtil.compare(checkDatetime, visit.getStartDatetime()) < 0) {
            throw new IllegalArgumentException(errorMessage + ": visit started at " + visit.getStartDatetime() + " but testing an earlier date");
        }
        if (visit.getStopDatetime() != null && OpenmrsUtil.compare(visit.getStopDatetime(), checkDatetime) < 0) {
            throw new IllegalArgumentException(errorMessage + ": visit stopped at " + visit.getStopDatetime() + " but testing a later date");
        }
    }

    public Date getEncounterStopDateRange() {
        return getStopDatetime() == null ? new Date() : getStopDatetime();
    }

    public boolean verifyIfUserIsTheCreatorOfVisit(User currentUser) {
        return visit.getCreator().equals(currentUser);
    }

    private class EncounterTypePredicate implements Predicate {

        private EncounterType type;

        public EncounterTypePredicate(EncounterType type) {
            this.type = type;
        }

        @Override
        public boolean evaluate(Object o) {
            return type.equals(((Encounter) o).getEncounterType());
        }
    }

    private class EncounterTypeAndLocationPredicate implements Predicate {

        private EncounterType type;

        private Location location;

        public EncounterTypeAndLocationPredicate(EncounterType type, Location location) {
            this.type = type;
            this.location = location;
        }

        @Override
        public boolean evaluate(Object o) {
            return type.equals(((Encounter) o).getEncounterType())
                    && location.equals(((Encounter) o).getLocation());
        }
    }
}
