/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.test.builder;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Encounter;
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
	
	public ObsBuilder setCreator(User creator) {
		obs.setCreator(creator);
		return this;
	}
	
	public ObsBuilder setEncounter(Encounter encounter) {
		obs.setEncounter(encounter);
		return this;
	}
	
	public ObsBuilder setFormField(String formNameSpace, String formFieldPath) {
		obs.setFormField(formNameSpace, formFieldPath);
		return this;
	}
}
