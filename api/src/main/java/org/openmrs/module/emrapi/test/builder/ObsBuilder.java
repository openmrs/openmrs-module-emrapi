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

package org.openmrs.module.emrapi.test.builder;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.api.context.Context;

import java.util.Date;

/**
 * Helper for building Obs in unit tests
 */
public class ObsBuilder {

    Obs obs = new Obs();

    public ObsBuilder setUuid(String uuid) {
        obs.setUuid(uuid);
        return this;
    }

    public ObsBuilder setPerson(Person person) {
        obs.setPerson(person);
        return this;
    }

    public ObsBuilder setConcept(Concept question) {
        obs.setConcept(question);
        return this;
    }

    public ObsBuilder setValue(Concept valueCoded) {
        obs.setValueCoded(valueCoded);
        return this;
    }

    public ObsBuilder setValue(Drug drug) {
        obs.setValueDrug(drug);
        return this;
    }

    public ObsBuilder setValue(Boolean valueBoolean) {
        obs.setValueBoolean(valueBoolean);
        return this;
    }

    public ObsBuilder setValue(String valueText) {
        obs.setValueText(valueText);
        return this;
    }

    public ObsBuilder setValue(Double value) {
        obs.setValueNumeric(value);
        return this;
    }

    public ObsBuilder setObsDatetime(Date obsDatetime) {
        obs.setObsDatetime(obsDatetime);
        return this;
    }

    public ObsBuilder setComment(String comment) {
        obs.setComment(comment);
        return this;
    }

    public ObsBuilder addMember(Concept question, Concept valueCoded) {
        Obs child = new Obs();
        child.setPerson(obs.getPerson());
        child.setObsDatetime(obs.getObsDatetime());
        child.setConcept(question);
        child.setValueCoded(valueCoded);
        obs.addGroupMember(child);
        return this;
    }

    public ObsBuilder addMember(Concept question, String valueText) {
        Obs child = new Obs();
        child.setPerson(obs.getPerson());
        child.setObsDatetime(obs.getObsDatetime());
        child.setConcept(question);
        child.setValueText(valueText);
        obs.addGroupMember(child);
        return this;
    }

    public ObsBuilder save() {
        Context.getObsService().saveObs(obs, null);
        return this;
    }

    public Obs get() {
        return obs;
    }

    public ObsBuilder setVoided(boolean voided) {
        obs.setVoided(voided);
        return this;
    }

    public ObsBuilder setVoidedReason(String reason) {
        obs.setVoidReason(reason);
        return this;
    }

    public ObsBuilder setCreator(User creator){
        obs.setCreator(creator);
        return this;
    }
}
