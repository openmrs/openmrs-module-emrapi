/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.domainwrapper;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.account.AccountDomainWrapper;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component("domainWrapperFactory")
public class DomainWrapperFactory implements ApplicationContextAware {
	
	private ApplicationContext applicationContext;
	
	public PatientDomainWrapper newPatientDomainWrapper() {
		PatientDomainWrapper patientDomainWrapper = new PatientDomainWrapper();
		return (PatientDomainWrapper) autowire(patientDomainWrapper);
	}
	
	public PatientDomainWrapper newPatientDomainWrapper(Patient patient) {
		PatientDomainWrapper patientDomainWrapper = newPatientDomainWrapper();
		patientDomainWrapper.setPatient(patient);
		return patientDomainWrapper;
	}
	
	public VisitDomainWrapper newVisitDomainWrapper() {
		VisitDomainWrapper visitDomainWrapper = new VisitDomainWrapper();
		return (VisitDomainWrapper) autowire(visitDomainWrapper);
	}
	
	public VisitDomainWrapper newVisitDomainWrapper(Visit visit) {
		VisitDomainWrapper visitDomainWrapper = newVisitDomainWrapper();
		visitDomainWrapper.setVisit(visit);
		return visitDomainWrapper;
	}
	
	public AccountDomainWrapper newAccountDomainWrapper() {
		AccountDomainWrapper accountDomainWrapper = new AccountDomainWrapper();
		return (AccountDomainWrapper) autowire(accountDomainWrapper);
	}
	
	public AccountDomainWrapper newAccountDomainWrapper(Person person) {
		AccountDomainWrapper accountDomainWrapper = newAccountDomainWrapper();
		accountDomainWrapper.initializeWithPerson(person);
		return accountDomainWrapper;
	}
	
	public EncounterDomainWrapper newEncounterDomainWrapper() {
		EncounterDomainWrapper encounterDomainWrapper = new EncounterDomainWrapper();
		return (EncounterDomainWrapper) autowire(encounterDomainWrapper);
	}
	
	public EncounterDomainWrapper newEncounterDomainWrapper(Encounter encounter) {
		EncounterDomainWrapper encounterDomainWrapper = newEncounterDomainWrapper();
		encounterDomainWrapper.setEncounter(encounter);
		return encounterDomainWrapper;
	}
	
	private DomainWrapper autowire(DomainWrapper domainWrapper) {
		applicationContext.getAutowireCapableBeanFactory().autowireBean(domainWrapper);
		return domainWrapper;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
}
