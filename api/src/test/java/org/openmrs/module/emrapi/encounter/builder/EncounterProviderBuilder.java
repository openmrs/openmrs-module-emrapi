/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.encounter.builder;

import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;

import java.util.HashSet;

public class EncounterProviderBuilder {
	
	private EncounterProvider encounterProvider;
	
	public EncounterProviderBuilder() {
		encounterProvider = new EncounterProvider();
		Provider provider = new Provider();
		EncounterRole role = new EncounterRole();
		Person person = new Person(1234);
		HashSet<PersonName> names = new HashSet<PersonName>();
		PersonName personName = new PersonName("Yogesh", "", "Jain");
		names.add(personName);
		person.setNames(names);
		provider.setPerson(person);
		encounterProvider.setEncounterRole(role);
		encounterProvider.setProvider(provider);
	}
	
	public EncounterProvider build() {
		return encounterProvider;
	}
}
