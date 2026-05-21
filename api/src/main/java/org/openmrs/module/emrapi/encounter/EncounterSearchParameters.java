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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.module.emrapi.utils.CustomJsonDateDeserializer;
import org.openmrs.module.emrapi.utils.CustomJsonDateSerializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EncounterSearchParameters {
	
	private List<String> visitUuids;
	
	private String patientUuid;
	
	private List<String> visitTypeUuids;
	
	private Date encounterDateTimeFrom;
	
	private Date encounterDateTimeTo;
	
	private List<String> providerUuids;
	
	private List<String> encounterTypeUuids;
	
	private String locationUuid;
	
	private Boolean includeAll = false;
	
	public EncounterSearchParameters() {
	}
	
	public EncounterSearchParameters(List<String> visitUuids, String patientUuid, List<String> visitTypeUuids,
	    Date encounterDateTimeFrom, Date encounterDateTimeTo, List<String> providerUuids, List<String> encounterTypeUuids,
	    String locationUuid, Boolean includeAll) {
		this.visitUuids = visitUuids;
		this.patientUuid = patientUuid;
		this.visitTypeUuids = visitTypeUuids;
		this.encounterDateTimeFrom = encounterDateTimeFrom;
		this.encounterDateTimeTo = encounterDateTimeTo;
		this.providerUuids = providerUuids;
		this.encounterTypeUuids = encounterTypeUuids;
		this.locationUuid = locationUuid;
		this.includeAll = includeAll;
	}
	
	public List<String> getVisitUuids() {
		return visitUuids == null ? new ArrayList<String>() : visitUuids;
	}
	
	public void setVisitUuids(List<String> visitUuids) {
		this.visitUuids = visitUuids;
	}
	
	public String getPatientUuid() {
		return patientUuid;
	}
	
	public void setPatientUuid(String patientUuid) {
		this.patientUuid = patientUuid;
	}
	
	public List<String> getVisitTypeUuids() {
		return visitTypeUuids == null ? new ArrayList<String>() : visitTypeUuids;
	}
	
	public void setVisitTypeUuids(List<String> visitTypeUuids) {
		this.visitTypeUuids = visitTypeUuids;
	}
	
	@JsonSerialize(using = CustomJsonDateSerializer.class)
	public Date getEncounterDateTimeFrom() {
		return encounterDateTimeFrom;
	}
	
	@JsonDeserialize(using = CustomJsonDateDeserializer.class)
	public void setEncounterDateTimeFrom(Date encounterDateTimeFrom) {
		this.encounterDateTimeFrom = encounterDateTimeFrom;
	}
	
	@JsonSerialize(using = CustomJsonDateSerializer.class)
	public Date getEncounterDateTimeTo() {
		return encounterDateTimeTo;
	}
	
	@JsonDeserialize(using = CustomJsonDateDeserializer.class)
	public void setEncounterDateTimeTo(Date endDate) {
		this.encounterDateTimeTo = endDate;
	}
	
	public List<String> getProviderUuids() {
		return providerUuids == null ? new ArrayList<String>() : providerUuids;
	}
	
	public void setProviderUuids(List<String> providerUuids) {
		this.providerUuids = providerUuids;
	}
	
	public List<String> getEncounterTypeUuids() {
		return encounterTypeUuids == null ? new ArrayList<String>() : encounterTypeUuids;
	}
	
	public void setEncounterTypeUuids(List<String> encounterTypeUuids) {
		this.encounterTypeUuids = encounterTypeUuids;
	}
	
	public String getLocationUuid() {
		return locationUuid;
	}
	
	public void setLocationUuid(String locationUuid) {
		this.locationUuid = locationUuid;
	}
	
	public Boolean getIncludeAll() {
		return includeAll;
	}
	
	public void setIncludeAll(Boolean includeAll) {
		this.includeAll = includeAll;
	}
	
}
