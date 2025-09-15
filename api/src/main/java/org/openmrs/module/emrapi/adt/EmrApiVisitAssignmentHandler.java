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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.api.handler.BaseEncounterVisitHandler;
import org.openmrs.api.handler.EncounterVisitHandler;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.util.OpenmrsUtil;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Ensures that encounters are assigned to visits based on the EMR module's business logic.
 * <p/>
 * For now, we require that a compatible visit exist before you're allowed to create an encounter. However if the EmrApiConstants#GP_VISIT_ASSIGNMENT_HANDLER_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAP
 * property that provides a mapping between encounter types and visit types is set then a new visit is created using the visit type from the saved mapping
 */
public class EmrApiVisitAssignmentHandler extends BaseEncounterVisitHandler implements EncounterVisitHandler {

    private VisitService visitService;

    private AdtService adtService;
    
    private AdministrationService administrationService;

    private EmrApiProperties emrApiProperties;

    private EncounterTypetoVisitTypeMapper encounterTypetoVisitTypeMapper;

    /**
     * Since the OpenMRS core doesn't load this bean via Spring, do some hacky setup here.
     *
     * @see https://tickets.openmrs.org/browse/TRUNK-3772
     */
    public EmrApiVisitAssignmentHandler() {
        try {
            // in production, set the fields this way
            visitService = Context.getVisitService();
            adtService = Context.getService(AdtService.class);
            administrationService = Context.getAdministrationService();
            emrApiProperties = Context.getRegisteredComponents(EmrApiProperties.class).get(0);
            encounterTypetoVisitTypeMapper = Context.getRegisteredComponents(EncounterTypetoVisitTypeMapper.class).get(0);
        } catch (Exception ex) {
            // unit tests will set the fields manually
        }
    }

    @Override
    public String getDisplayName(Locale locale) {
        return "Default EMR Visit Assignment Handler";
    }

    @Override
    public void beforeCreateEncounter(Encounter encounter) {

        // do nothing if disabled
        if ("true".equalsIgnoreCase(administrationService.getGlobalProperty(EmrApiConstants.GP_DISABLE_VISIT_ASSIGMENT_HANDLER))) {
            return;
        }

        //Do nothing if the encounter already belongs to a visit.
        if (encounter.getVisit() != null) {
            return;
        }

        // Eventually we should explicitly allow some encounters to be visit-free, probably via a GP defining a list of EncounterTypes.
        // If we do that, we'd return early from here, and re-enable the IllegalStateException below.


        Date when = encounter.getEncounterDatetime();
        if (when == null) {
            when = new Date();
        }

        // location-less encounters shouldn't belong to a visit
        if (encounter.getLocation() == null) {
            return;
        }

        List<Patient> patient = Collections.singletonList(encounter.getPatient());

        // visits that have started by end of day on the encounter date
        List<Visit> candidates = visitService.getVisits(null, patient, null, null, null,
                new DateTime(when).withTime(23, 59, 59, 999).toDate(), null, null, null, true, false);

        if (candidates != null) {
            for (Visit candidate : candidates) {

                if (emrApiProperties.getVisitAssignmentHandlerAdjustEncounterTimeOfDayIfNecessary()) {
                    if (adtService.isSuitableVisitIgnoringTime(candidate, encounter.getLocation(), when)) {
                        if (when.before(candidate.getStartDatetime())) {
                            updateDateActivatedOfOrdersIfNecessary(encounter.getOrders(), when, candidate.getStartDatetime());
                            encounter.setEncounterDatetime(candidate.getStartDatetime());
                        }
                        else if (candidate.getStopDatetime() != null && when.after(candidate.getStopDatetime())) {
                            encounter.setEncounterDatetime(candidate.getStopDatetime());
                        }

                        candidate.addEncounter(encounter);
                        return;
                    }
                }
                else {
                    if (adtService.isSuitableVisit(candidate, encounter.getLocation(), when)) {
                        candidate.addEncounter(encounter);
                        return;
                    }
                }
            }
        }
        // there is no suitable visit so create one if there is a mapping encounter type to the visit type via the Global property
        if (StringUtils.isNotBlank(administrationService.getGlobalProperty(EmrApiConstants.GP_VISIT_ASSIGNMENT_HANDLER_ENCOUNTER_TYPE_TO_VISIT_TYPE_MAP))) {
            VisitType visitType = getEncounterTypetoVisitTypeMapper().getVisitTypeForEncounter(encounter);
            // only process a visit if there is a matching visitType
            if (visitType != null) {
                Visit visit = new Visit();
                visit.setStartDatetime(encounter.getEncounterDatetime());
                visit.setLocation(adtService.getLocationThatSupportsVisits(encounter.getLocation()));
                visit.setPatient(encounter.getPatient());
                visit.setVisitType(visitType);
                //set stop date time to last millisecond of the encounter day for a past visit
                if (!DateUtils.isSameDay(encounter.getEncounterDatetime(), new Date())) {
                    visit.setStopDatetime(OpenmrsUtil.getLastMomentOfDay(encounter.getEncounterDatetime()));
                }
                visit.addEncounter(encounter);
            }
        }

        // TEMP HACK: allow visit-free encounters while we continue to discuss this
        // throw new IllegalStateException("Cannot create an encounter outside of a visit");
    }

    /**
     * If an encounter contains orders, and those orders have `dateActivated` that equals the encounter's datetime,
     * then if the encounter's datetime is changed, the order dateActivated values should be changed accordingly
     * @param orders - the orders to check
     * @param existingDatetime the existing dateActivated to adjust from
     * @param newDatetime - the new dateActivated to adjust to
     */
    protected void updateDateActivatedOfOrdersIfNecessary(Set<Order> orders, Date existingDatetime, Date newDatetime) {
        if (orders != null) {
            for (Order order : orders) {
                if (order.getDateActivated() != null & order.getDateActivated().equals(existingDatetime)) {
                    order.setDateActivated(newDatetime);
                }
            }
        }
    }

    public void setVisitService(VisitService visitService) {
        this.visitService = visitService;
    }

    public void setAdtService(AdtService adtService) {
        this.adtService = adtService;
    }
    
    public void setAdministrationService(AdministrationService administrationService) {this.administrationService = administrationService; }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public EncounterTypetoVisitTypeMapper getEncounterTypetoVisitTypeMapper() {
        return encounterTypetoVisitTypeMapper;
    }
    
    public void setEncounterTypetoVisitTypeMapper(
            EncounterTypetoVisitTypeMapper encounterTypetoVisitTypeMapper) {
        this.encounterTypetoVisitTypeMapper = encounterTypetoVisitTypeMapper;
    }
}
