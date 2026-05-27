/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
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
	
	private EncounterParameters() {
		
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
