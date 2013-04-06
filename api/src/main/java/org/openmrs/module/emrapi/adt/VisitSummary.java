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
import org.openmrs.Visit;
import org.openmrs.module.emrapi.EmrApiProperties;

/**
 * Wrapper around a Visit, that provides convenience methods to find particular encounters of interest.
 */
public class VisitSummary {

    private Visit visit;

    private EmrApiProperties props;

    public VisitSummary(Visit visit, EmrApiProperties props) {
        this.visit = visit;
        this.props = props;
    }

    /**
     * @return the check-in encounter for this visit, or null if none exists
     */
    public Encounter getCheckInEncounter() {
        for (Encounter e : visit.getEncounters()) {
            if (props.getCheckInEncounterType().equals(e.getEncounterType()))
                return e;
        }
        return null;
    }

    /**
     * @return the most recent encounter in the visit
     */
    public Encounter getLastEncounter() {
        if (visit.getEncounters().size() > 0)
            return visit.getEncounters().iterator().next();
        return null;
    }

    /**
     * @return the visit
     */
    public Visit getVisit() {
        return visit;
    }

}
