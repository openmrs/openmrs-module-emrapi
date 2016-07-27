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

import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.emrapi.adt.exception.ExistingVisitDuringTimePeriodException;
import org.openmrs.module.emrapi.merge.PatientMergeAction;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;

import java.util.Date;
import java.util.List;

/**
 * <pre>
 * API methods related to Check-In, Admission, Discharge, and Transfer
 *
 * Since patients frequently leave the facility without having any formal electronic check-out process, we ensure that
 * old stale visits are automatically closed, even if they are never intentionally stopped.
 *
 * A patient may be Admitted to inpatient care, which flags their visit as inpatient (by creating an Admission
 * encounter). Any visit flagged as inpatient will not be auto-closed. A patient may be discharged (which means generally
 * exited from inpatient, e.g. leaving against medical advice is still a "discharge"), which creates a discharge encounter
 * and frees the visit to be auto-closed.
 *
 * Visits are only allowed to happen at locations tagged with the EmrConstants.LOCATION_TAG_SUPPORTS_VISITS tag. When
 * you pass a location without that tag to a service method, we look from that location and above in the location
 * hierarchy until we find a location with this tag. (This allows you to configure the setup such that doing a check-in
 * at Outpatient Department creates a visit at its parent, with an Encounter at the location itself.)
 *
 * Admission encounters can only happen at locations tagged with the {@link org.openmrs.module.emrapi.EmrApiConstants#LOCATION_TAG_SUPPORTS_ADMISSION}
 * tag.
 * </pre>
 */
public interface AdtService extends OpenmrsService {

    /**
     * Gets the patient's <em>active</em> visit at the given location, or null, if none exists..
     *
     *
     * @param patient
     * @param department
     * @return
     */
    VisitDomainWrapper getActiveVisit(Patient patient, Location department);


    /**
     * Returns true/false as to whether the specified visit should be closed based on the built-in EMR-API logic
     *
     * @param visit
     * @return
     */
    boolean shouldBeClosed(Visit visit);

    /**
     * Close the patient's active visit at the given location
     *
     * @param visit
     * @return
     */
    void closeAndSaveVisit(Visit visit);

    /**
     * Like #getActiveVisit, but if the patient has no active visit, one is created (and persisted).
     * (This has the same side-effects as #getActiveVisit.)
     * The visit's location will be a valid visit location per our business logic.
     *
     * @param patient
     * @param department
     * @return
     */
    Visit ensureActiveVisit(Patient patient, Location department);

    /**
     * If the patient has no active visit on the day of @visitTime, one is created (and persisted).
     * The visit's location will be a valid visit location per our business logic.
     *
     * @param patient
     * @param visitTime
     * @param department
     * @return
     */
    Visit ensureVisit(Patient patient, Date visitTime, Location department);

    /**
     * Creates a "check-in" encounter for the given patient, at the location where, and adds it to the active visit.
     * (This method calls ensureActiveVisit.)
     *
     * @param patient                   required
     * @param where                     required (must either support visits, or have an ancestor location that does)
     * @param checkInClerk              optional (defaults to Provider for currently-authenticated user)
     * @param obsForCheckInEncounter    optional
     * @param ordersForCheckInEncounter optional
     * @param newVisit                  says whether create a new visit or not
     * @return the encounter created (with EncounterService.saveEncounter already called on it)
     */
    Encounter checkInPatient(Patient patient, Location where, Provider checkInClerk, List<Obs> obsForCheckInEncounter,
                             List<Order> ordersForCheckInEncounter, boolean newVisit);

    /**
     * Looks at this location, then its ancestors in the location hierarchy, to find a location tagged with
     * {@link org.openmrs.module.emrapi.EmrApiConstants#LOCATION_TAG_SUPPORTS_VISITS}
     *
     * @param location
     * @return location, or its closest ancestor that supports visits
     * @throws IllegalArgumentException if neither location nor its ancestors support visits
     */
    Location getLocationThatSupportsVisits(Location location);

    /**
     * @return all locations that are allowed to have visits assigned to them
     * @see org.openmrs.module.emrapi.EmrApiConstants#LOCATION_TAG_SUPPORTS_VISITS
     */
    List<Location> getAllLocationsThatSupportVisits();

    /**
     * @param visit
     * @param location
     * @param when
     * @return whether the given visit is suitable to store a patient interaction at the given location and date
     */
    boolean isSuitableVisit(Visit visit, Location location, Date when);

    /**
     * Gets all currently-active visits at the given location or any of its children
     *
     *
     * @param location
     * @return
     */
    List<VisitDomainWrapper> getActiveVisits(Location location);

    /**
     * If any currently-open visits are now inactive per our business logic, close them
     */
    void closeInactiveVisits();

    /**
     * @param patient
     * @return the most recent encounter for the given patient
     */
    Encounter getLastEncounter(Patient patient);

    /**
     * @param patient
     * @return the number of non-voided encounters this patient has had
     */
    int getCountOfEncounters(Patient patient);

    /**
     * @param patient
     * @return the number of non-voided visits this patient has had
     */
    int getCountOfVisits(Patient patient);

    /**
     * @param v1
     * @param v2
     * @return true if both visits are in overlapping locations, and have overlapping datetime ranges
     */
    boolean visitsOverlap(Visit v1, Visit v2);

    /**
     * Merges patients using the underlying OpenMRS core mechanism, but applying extra business logic:
     * <ul>
     * <li>Any two visits that overlap will be joined together into one</li>
     * <li>Merging an "unknown" patient into a known one does _not_ copy the unknown flag to the target patient</li>
     * <li>Any additional {@link org.openmrs.module.emrapi.merge.PatientMergeAction}s</li>
     * </ul>
     *
     * @param preferred
     * @param notPreferred
     * @see #visitsOverlap(org.openmrs.Visit, org.openmrs.Visit)
     * @see org.openmrs.api.PatientService#mergePatients(org.openmrs.Patient, org.openmrs.Patient)
     */
    void mergePatients(Patient preferred, Patient notPreferred);

    /**
     * Allows another module to add a patient merge action to the list of actions to be performed
     * when a patient is merged; (ideally, this would just happen via @Autowired, but because @Autowired
     * in service impls greatly slow down startup, instead we have other modules inject the beans explicitly
     * via this method). (See the FixPaperRecordRequestsForMerge class and the PaperRecordModuleActivator
     * in the Paper Record module for an example)
     *
     * @param patientMergeAction
     */
    void addPatientMergeAction(PatientMergeAction patientMergeAction);

    /**
     * Allows another module to remove a patient merge action to the list of actions to be performed
     * when a patient is merged; (see addPatientMergeAction above)
     *
     * @param patientMergeAction
     */
    void removePatientMergeAction(PatientMergeAction patientMergeAction);

    /**
     * Merge a set of consecutive patient visits
     * @param visits
     * @param patient
     * @return a visit representing the newly merged visit
     */
    Visit mergeConsecutiveVisits(List<Integer> visits, Patient patient);

    /**
     * Merges patient visits
     * @param preferred
     * @param nonPreferred
     * @return a visit representing the newly merged visit
     */
    Visit mergeVisits(Visit preferred, Visit nonPreferred);

    /**
     * Checks if the list of visits passed in are consecutive
     * @param visits
     * @param patient
     * @return a boolean indicating whether or not the visits are consecutives
     */
    boolean areConsecutiveVisits(List<Integer> visits, Patient patient);

    /**
     * Creates an encounter for specific adt action.
     * Throws an exception if not valid with visit.
     * @param action
     * @return the encounter representing this discharge
     */
    Encounter createAdtEncounterFor(AdtAction action);

    /**
     * Helper method to get a {@link VisitDomainWrapper} given a {@link Visit}
     * @param visit
     * @return
     */
    @Deprecated  // use new VisitDomainWrapperFactory instead (this service method has been delegated to use the new factory)
    VisitDomainWrapper wrap(Visit visit);

    /**
     * Gets all currently-active inpatient visits (patients who have been admitted) at the given location or any of its children
     * @param visitLocation
     * @return
     */
    List<VisitDomainWrapper> getInpatientVisits(Location visitLocation, Location ward);

    /**
     * Creates a retrospective visit for the specified patient with the specified start and stop dates
     *
     * @param patient
     * @param location
     * @param startDatetime
     * @param stopDatetime
     * @should throw IllegalArgumentException if startDatetime in future
     * @should throw IllegalArgumentException it stopDatetime in future
     * @should throw IllegalArgumentException if stopDatetime before startDatetime
     * @should throw ExistingVisitDuringTimePeriodException if existing visit during date range
     * @return the created visit
     */
    VisitDomainWrapper createRetrospectiveVisit(Patient patient, Location location, Date startDatetime, Date stopDatetime)
        throws ExistingVisitDuringTimePeriodException;

    /**
     * Gets all visits for the patient at the visit location associated with the specified location
     * during the specified datetime range
     *
     * @param patient
     * @param location
     * @param startDatetime
     * @param stopDatetime
     * @return
     */
    List<VisitDomainWrapper> getVisits(Patient patient, Location location, Date startDatetime, Date stopDatetime);

    /**
     * Returns true/false whether or not the patient has any visits at the visit location associated with
     * the specified location during the specified datetime range
     *
     * @param patient
     * @param location
     * @param startDatetime
     * @param stopDatetime
     * @return
     */
    boolean hasVisitDuring(Patient patient, Location location, Date startDatetime, Date stopDatetime);


    /**
     * @return all locations that are tagged to support admissions
     * @see {@link org.openmrs.module.emrapi.EmrApiConstants#LOCATION_TAG_SUPPORTS_ADMISSION}
     */
    List<Location> getInpatientLocations();

}
