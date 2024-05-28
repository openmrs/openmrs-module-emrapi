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
package org.openmrs.module.emrapi.adt.domain;

import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;

public class AdtEvent {

    private AdtEventType eventType;
    private Encounter eventEncounter;
    private Obs dispositionObsGroup;
    private Location ward;

    public AdtEvent() {}

    public AdtEvent(AdtEventType eventType, Encounter encounter) {
        this.eventType = eventType;
        this.eventEncounter = encounter;
        this.ward = encounter.getLocation();
    }

    public AdtEvent(AdtEventType eventType, Encounter encounter, Obs dispositionObsGroup, Location ward) {
        this.eventType = eventType;
        this.eventEncounter = encounter;
        this.dispositionObsGroup = dispositionObsGroup;
        this.ward = ward;
    }

    public AdtEventType getEventType() {
        return eventType;
    }

    public void setEventType(AdtEventType eventType) {
        this.eventType = eventType;
    }

    public Encounter getEventEncounter() {
        return eventEncounter;
    }

    public void setEventEncounter(Encounter eventEncounter) {
        this.eventEncounter = eventEncounter;
    }

    public Obs getDispositionObsGroup() {
        return dispositionObsGroup;
    }

    public void setDispositionObsGroup(Obs dispositionObsGroup) {
        this.dispositionObsGroup = dispositionObsGroup;
    }

    public Location getWard() {
        return ward;
    }

    public void setWard(Location ward) {
        this.ward = ward;
    }
}
