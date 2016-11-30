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


import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.reverseOrder;
import static java.util.Collections.sort;
import static org.apache.commons.collections.CollectionUtils.find;
import static org.apache.commons.collections.CollectionUtils.select;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.Predicate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateMidnight;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.reporting.query.AwaitingAdmissionVisitQuery;
import org.openmrs.module.emrapi.descriptor.MissingConceptException;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.disposition.Disposition;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.disposition.DispositionType;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapper;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.context.VisitEvaluationContext;
import org.openmrs.module.reporting.query.visit.VisitIdSet;
import org.openmrs.module.reporting.query.visit.VisitQueryResult;
import org.openmrs.module.reporting.query.visit.service.VisitQueryService;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


/**
 * Wrapper around a Visit, that provides convenience methods to find particular encounters of interest.
 */
public class VisitDomainWrapper implements DomainWrapper {

    public enum SortOrder {
        EARLIEST_FIRST, MOST_RECENT_FIRST
    }

    private static final Log log = LogFactory.getLog(VisitDomainWrapper.class);

    @Qualifier("emrApiProperties")
    @Autowired
    protected EmrApiProperties emrApiProperties;

    @Qualifier("dispositionService")
    @Autowired
    protected DispositionService dispositionService;

    @Autowired
    protected VisitQueryService visitQueryService;
    
    @Qualifier("emrVisitService")
    @Autowired
    protected EmrVisitService emrVisitService; 

    private Visit visit;

    public VisitDomainWrapper(){
    }

    @Deprecated   // use new VisitDomainWrapperFactory instead to instantiate a visit domain wrapper
    public VisitDomainWrapper(Visit visit) {
        this.visit = visit;
    }

    @Deprecated    // use new VisitDomainWrapperFactory instead to instantiate a visit domain wrapper
    public VisitDomainWrapper(Visit visit, EmrApiProperties emrApiProperties) {
        this.visit = visit;
        this.emrApiProperties = emrApiProperties;
    }


    @Deprecated      // use new VisitDomainWrapperFactory instead to instantiate a visit domain wrapper
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

    public void setVisit(Visit visit) {
        this.visit = visit;
    }

    public int getVisitId() {
        return visit.getVisitId();
    }

    // setters for mocking purposes
    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public void setDispositionService(DispositionService dispositionService) {
        this.dispositionService = dispositionService;
    }

    public void setVisitQueryService(VisitQueryService visitQueryService) {
        this.visitQueryService = visitQueryService;
    }

    public Encounter getAdmissionEncounter() {
        return (Encounter) find(getSortedEncounters(SortOrder.MOST_RECENT_FIRST), new EncounterTypePredicate(emrApiProperties.getAdmissionEncounterType()));
    }

    // TODO: refactor this to use EncounterTypePredicate
    public Encounter getLatestAdtEncounter(){
        for (Encounter e : getSortedEncounters(SortOrder.MOST_RECENT_FIRST)) {
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

    /**
     * Returns the most recent  (non-voided) check-in encounter from this visit
     * @return
     */
    public Encounter getMostRecentCheckInEncounter() {
        return (Encounter) find(getSortedEncounters(SortOrder.MOST_RECENT_FIRST), new EncounterTypePredicate(emrApiProperties.getCheckInEncounterType()));
    }

    /**
     * Returns the first   (non-voided) check-in encounter from this visit
     * @return
     */
    public Encounter getEarliestCheckInEncounter() {
        return (Encounter) find(getSortedEncounters(SortOrder.EARLIEST_FIRST), new EncounterTypePredicate(emrApiProperties.getCheckInEncounterType()));
    }

    @Deprecated  // use getMostRecentCheckInEncounter, as this is a more accurate method name
    public Encounter getCheckInEncounter() {
        return getMostRecentCheckInEncounter();
    }

    public Encounter getMostRecentEncounter() {
        List<Encounter> encounters = getSortedEncounters(SortOrder.MOST_RECENT_FIRST);
        if (encounters.size() > 0)
            return encounters.get(0);
        return null;
    }

    @Deprecated //use getMostRecentEncounter because this method name doesn't make sense
    public Encounter getLastEncounter() {
        return getMostRecentEncounter();
    }

    public Encounter getEarliestEncounter() {
        List<Encounter> encounters = getSortedEncounters(SortOrder.MOST_RECENT_FIRST);
        if (encounters.size() != 0)
            return encounters.get(encounters.size() - 1);
        return null;
    }

    @Deprecated // we are standardizing on "Earliest" and "Most Recent" to the
    public Encounter getOldestEncounter() {
        return getEarliestEncounter();
    }

    /**
     * Fetches the most recent (non-voided) visit note encounter from this visit
     * @return
     */
    public Encounter getMostRecentVisitNote() {
        return (Encounter) find(getSortedEncounters(SortOrder.MOST_RECENT_FIRST), new EncounterTypePredicate(emrApiProperties.getVisitNoteEncounterType()));
    }

    /**
     * Fetches the most recent (non-voided) visit note encounter from this visit at the  specified location
     * @return
     */
    public Encounter getMostRecentVisitNoteAtLocation(Location location) {
        return (Encounter) find(getSortedEncounters(SortOrder.MOST_RECENT_FIRST), new EncounterTypeAndLocationPredicate(emrApiProperties.getVisitNoteEncounterType(), location));

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


    // default is to return most recent encounter first
    public List<Encounter> getSortedEncounters() {
        return getSortedEncounters(SortOrder.MOST_RECENT_FIRST);
    }

    /**
     * Returns all non-voided encounters in the visit
     *
     * @param order whether to return the most recent first, or the earliest first
     * @return
     */
    public List<Encounter> getSortedEncounters(SortOrder order) {

        Comparator<Encounter> datetimeComparator = EncounterDomainWrapper.DATETIME_COMPARATOR;

        if (order == SortOrder.MOST_RECENT_FIRST) {
            datetimeComparator = reverseOrder(datetimeComparator);
        }

        if (visit.getEncounters() != null) {
            List<Encounter> nonVoidedEncounters = (List<Encounter>) select(visit.getEncounters(), EncounterDomainWrapper.NON_VOIDED_PREDICATE);
            sort(nonVoidedEncounters, datetimeComparator);
            return nonVoidedEncounters;
        }
        return EMPTY_LIST;
    }

    public int getDifferenceInDaysBetweenCurrentDateAndStartDate() {
        Date today = Calendar.getInstance().getTime();

        Date startDateVisit = getStartDate();

        int millisecondsInADay = 1000 * 60 * 60 * 24;

        return (int) ((today.getTime() - startDateVisit.getTime()) / millisecondsInADay);
    }

    // note that the disposition must be on the top level for this to pick it up
    // (seemed like this made sense to do for performance reasons)
    // also, if encounter has multiple disposition (is this possible?) it just returns the first one it finds
    public Disposition getMostRecentDisposition() {

        if (dispositionService.dispositionsSupported()) {   // prevents against stace trace if dispositions are supported
            DispositionDescriptor dispositionDescriptor = dispositionService.getDispositionDescriptor();

            for (Encounter encounter : getSortedEncounters(SortOrder.MOST_RECENT_FIRST)) {
                for (Obs obs : encounter.getObsAtTopLevel(false)) {
                    if (dispositionDescriptor.isDisposition(obs)) {
                        return dispositionService.getDispositionFromObsGroup(obs);
                    }
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

        if (dispositionService.dispositionsSupported()) {   // prevents against stack trace if dispositions not configured
            DispositionDescriptor dispositionDescriptor = dispositionService.getDispositionDescriptor();

            for (Encounter encounter : getSortedEncounters(SortOrder.MOST_RECENT_FIRST)) {  // getSortedEncounters already excludes voided encounters
                for (Obs obs : encounter.getObsAtTopLevel(false)) {
                    if (dispositionDescriptor.isDisposition(obs)
                            && dispositionService.getDispositionFromObsGroup(obs).getType() == type) {
                        return getDiagnosesFromEncounter(encounter);
                    }
                }
            }
        }

        return new ArrayList<Diagnosis>();  // return empty list if no matches
    }

    public List<Diagnosis> getPrimaryDiagnoses() {
        List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
        for (Encounter encounter : getSortedEncounters(SortOrder.MOST_RECENT_FIRST)) {
            diagnoses.addAll(getDiagnosesFromEncounter(encounter, Collections.singletonList(Diagnosis.Order.PRIMARY)));
        }
        return diagnoses;
    }
    
    /**
     * @return the unique list of diagnoses recorded in this visit, where uniqueness is based only on
     * whether the CodedOrNonCoded diagnosis is the same, not on whether the order or certainty are the same
     * if the primaryOnly flag is true, return only primary diagnoses
     * if the confirmedOnly flag is true, return only confirmed diagnoses
      * will return null if diagnosis support not currently configured
     */
    public List<Diagnosis> getUniqueDiagnoses(Boolean primaryOnly, Boolean confirmedOnly) {
       
       DiagnosisMetadata diagnosisMetadata;
       try {
           diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
       }
       catch (MissingConceptException ex) {
           // this isn't a hard error, because some implementations will not be using diagnoses functionality
           log.warn("Diagnosis metadata not configured", ex);
           return Collections.emptyList();
       }
       
       List<Obs> obsList = emrVisitService.getDiagnoses(getVisit(), diagnosisMetadata, primaryOnly, confirmedOnly);
       
       Map<CodedOrFreeTextAnswer, Diagnosis> diagnoses = new LinkedHashMap<CodedOrFreeTextAnswer, Diagnosis>();
       for (Obs obs : obsList) {
          if (diagnosisMetadata.isDiagnosis(obs)) {
              try {
                  Diagnosis diagnosis = diagnosisMetadata.toDiagnosis(obs);
                  if (!diagnoses.containsKey(diagnosis.getDiagnosis())) {  // Checking uniqueness here
                     diagnoses.put(diagnosis.getDiagnosis(), diagnosis);
                  }
              } catch (Exception ex) {
                  log.warn("malformed diagnosis obs group with obsId " + obs.getObsId(), ex);
              }
          }
       }
       return new ArrayList<Diagnosis>(diagnoses.values());
    }

	/**
	 * @return the unique list of diagnoses recorded in this visit, where uniqueness is based only on
	 * whether the CodedOrNonCoded diagnosis is the same, not on whether the order or certainty are the same
	 * if the primaryOnly flag is true, return only primary diagnoses
	 * if the confirmedOnly flag is true, return only confirmed diagnoses
     * will return null if diagnosis support not currently configured
	 */
    /**
     * @deprecated  As of release 1.19, replaced by {@link #getUniqueDiagnoses(Boolean, Boolean)}
     */
    @Deprecated
    public List<Diagnosis> getUniqueDiagnosesLegacy(Boolean primaryOnly, Boolean confirmedOnly) {
		Map<CodedOrFreeTextAnswer, Diagnosis> diagnoses = new LinkedHashMap<CodedOrFreeTextAnswer, Diagnosis>();
		for (Encounter encounter : getSortedEncounters(SortOrder.MOST_RECENT_FIRST)) {

         List<Diagnosis> diagnosesFromEncounter = getDiagnosesFromEncounter(encounter);
         if (diagnosesFromEncounter == null) {
             return null;
         }

			for (Diagnosis d : diagnosesFromEncounter) {
				if (!primaryOnly || d.getOrder() == Diagnosis.Order.PRIMARY) {
					if (!confirmedOnly || d.getCertainty() == Diagnosis.Certainty.CONFIRMED) {
						if (!diagnoses.containsKey(d.getDiagnosis())) {
							diagnoses.put(d.getDiagnosis(), d);
						}
					}
				}
			}

		}
		return new ArrayList<Diagnosis>(diagnoses.values());
	}

    private List<Diagnosis> getDiagnosesFromEncounter(Encounter encounter) {
        return getDiagnosesFromEncounter(encounter, null);
    }
    
    private List<Diagnosis> getDiagnosesFromEncounter(Encounter encounter, List<Diagnosis.Order> diagnosisOrders) {

        DiagnosisMetadata diagnosisMetadata;

        try {
            diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
        }
        catch (MissingConceptException ex) {
            // this isn't a hard error, because some implementations will not be using diagnoses functionality
            log.info("Diagnosis metadata not configured", ex);
            return null;
        }

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

    public boolean hasEncounters(){
        List<Encounter> encounters = getSortedEncounters(SortOrder.MOST_RECENT_FIRST);
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

        for (Encounter encounter : getSortedEncounters(SortOrder.MOST_RECENT_FIRST)) {
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

    public boolean isAwaitingAdmission() {

        if (!isActive() || !dispositionService.dispositionsSupported()) {  // prevents a stack trace if dispositions are supported
            return false;
        }

        VisitQueryResult result = null;

        VisitEvaluationContext context = new VisitEvaluationContext();
        context.setBaseVisits(new VisitIdSet(getVisitId()));

        AwaitingAdmissionVisitQuery query = new AwaitingAdmissionVisitQuery();
        query.setLocation(visit.getLocation());

        try {
            result = visitQueryService.evaluate(query, context);
        }
        catch (EvaluationException e) {
            throw new IllegalStateException("Unable to evaluate awaiting admission query", e);
        }

        return result != null && result.getMemberIds().size() > 0;
    }

    public Location getInpatientLocation(Date onDate) {

        if (!isAdmitted(onDate)) {
            return null;
        }

        EncounterType admissionEncounterType = emrApiProperties.getAdmissionEncounterType();
        EncounterType transferEncounterType = emrApiProperties.getTransferWithinHospitalEncounterType();

        for (Encounter encounter : getSortedEncounters(SortOrder.MOST_RECENT_FIRST)) {
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

    public Date getStartDate() {
        return visit.getStartDatetime() != null ? new DateMidnight(visit.getStartDatetime()).toDate() : null;
    }

    public Date getStopDate() {
        return visit.getStopDatetime() != null ? new DateMidnight(visit.getStopDatetime()).toDate() : null;
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

    public Object getVisitAttribute(String uuidOrName) {
        for (VisitAttribute attribute : visit.getActiveAttributes()) {
            if (attribute.getAttributeType().getUuid().equals(uuidOrName) ||
                    attribute.getAttributeType().getName().equals(uuidOrName)) {
                // note the assumption of this method is that there is only one attribute
                return attribute.getValue();
            }
        }
        return null;
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
