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

public class ActiveEncounterParameters {
	
	String patientUuid;
	
	String encounterTypeUuid;
	
	String visitTypeUuid;
	
	String providerUuid;
	
	Boolean includeAll;
	
	String locationUuid;
	
	public String getLocationUuid() {
		return locationUuid;
	}
	
	public void setLocationUuid(String locationUuid) {
		this.locationUuid = locationUuid;
	}
	
	public String getProviderUuid() {
		return providerUuid;
	}
	
	public void setProviderUuid(String providerUuid) {
		this.providerUuid = providerUuid;
	}
	
	public String getPatientUuid() {
		return patientUuid;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public String getEncounterTypeUuid() {
		return encounterTypeUuid;
	}
	
	public void setEncounterTypeUuid(String encounterTypeUuid) {
		this.encounterTypeUuid = encounterTypeUuid;
	}
	
	public String getVisitTypeUuid() {
		return visitTypeUuid;
	}
	
	public void setVisitTypeUuid(String visitTypeUuid) {
		this.visitTypeUuid = visitTypeUuid;
	}
	
	public Boolean getIncludeAll() {
		return includeAll;
	}
	
	public void setIncludeAll(Boolean includeAll) {
		this.includeAll = includeAll;
	}
	
}
