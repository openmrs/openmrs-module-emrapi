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
                                     Date encounterDateTimeFrom, Date encounterDateTimeTo, List<String> providerUuids,
                                     List<String> encounterTypeUuids, String locationUuid, Boolean includeAll) {
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
