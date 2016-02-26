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


import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Provider;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EncounterParameters {

    private Location location;
    private EncounterType encounterType;
    private Set<Provider> providers;
    private Date encounterDateTime;
    private Patient patient;
    private String encounterUuid;
    private Map<String, Object> context = new HashMap<String, Object>();

    private EncounterParameters(){

    }

    public static EncounterParameters instance() {
        return new EncounterParameters();
    }

    public EncounterParameters setLocation(Location location) {
        this.location = location;
        return this;
    }

    public EncounterParameters setEncounterType(EncounterType encounterType) {
        this.encounterType = encounterType;
        return this;
    }

    public EncounterParameters setProviders(Set<Provider> providers) {
        this.providers = providers;
        return this;
    }

    public EncounterParameters setEncounterDateTime(Date encounterDateTime) {
        this.encounterDateTime = encounterDateTime;
        return this;
    }

    public EncounterParameters setPatient(Patient patient) {
        this.patient = patient;
        return this;
    }

    public EncounterParameters setEncounterUuid(String encounterUuid) {
        this.encounterUuid = encounterUuid;
        return this;
    }

    public EncounterParameters setContext(Map<String, Object> context) {
        this.context = context;
        return this;
    }

    public Location getLocation() {
        return location;
    }

    public EncounterType getEncounterType() {
        return encounterType;
    }

    public Set<Provider> getProviders() {
        return providers;
    }

    public Date getEncounterDateTime() {
        return encounterDateTime;
    }

    public Patient getPatient() {
        return patient;
    }

    public String getEncounterUuid() {
        return encounterUuid;
    }

    public Map<String, Object> getContext() {
        return context;
    }
}
