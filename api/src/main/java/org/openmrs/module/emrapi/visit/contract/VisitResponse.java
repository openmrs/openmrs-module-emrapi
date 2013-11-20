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
package org.openmrs.module.emrapi.visit.contract;

import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.List;

public class VisitResponse {
    private String visitUuid;
    private List<EncounterTransaction> encounters = new ArrayList<EncounterTransaction>();

    public VisitResponse(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public List<EncounterTransaction> getEncounters() {
        return encounters;
    }

    public void addEncounter(EncounterTransaction encounter) {
        encounters.add(encounter);
    }
}
