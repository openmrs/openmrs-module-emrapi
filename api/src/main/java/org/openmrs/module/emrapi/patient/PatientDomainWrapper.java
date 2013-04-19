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

package org.openmrs.module.emrapi.patient;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * A rich-domain-model class that wraps a Patient, and lets you perform common queries.
 */
public class PatientDomainWrapper {

    private Patient patient;

    @Qualifier("emrApiProperties")
    @Autowired
    protected EmrApiProperties emrApiProperties;

    @Qualifier("adtService")
    @Autowired
    protected AdtService adtService;

    @Qualifier("visitService")
    @Autowired
    protected VisitService visitService;

    @Qualifier("encounterService")
    @Autowired
    protected EncounterService encounterService;

    public PatientDomainWrapper() {
    }

    public PatientDomainWrapper(Patient patient, EmrApiProperties emrApiProperties, AdtService adtService,
                                VisitService visitService, EncounterService encounterService) {
        this.patient = patient;
        this.emrApiProperties = emrApiProperties;
        this.adtService = adtService;
        this.visitService = visitService;
        this.encounterService = encounterService;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Patient getPatient() {
        return patient;
    }

    public Integer getId() {
        return patient.getPatientId();
    }

    public String getGender() {
        return patient.getGender();
    }

    public Integer getAge() {
        return patient.getAge();
    }

    public Integer getAgeInMonths() {

        if (patient.getBirthdate() == null) {
            return null;
        }

        Date endDate = patient.isDead() ? patient.getDeathDate() : new Date();
        return Months.monthsBetween(new DateTime(patient.getBirthdate()), new DateTime(endDate)).getMonths();
    }

    public Integer getAgeInDays() {

        if (patient.getBirthdate() == null) {
            return null;
        }

        Date endDate = patient.isDead() ? patient.getDeathDate() : new Date();
        return Days.daysBetween(new DateTime(patient.getBirthdate()), new DateTime(endDate)).getDays();
    }

    public Boolean getBirthdateEstimated() {
        return patient.getBirthdateEstimated();
    }

    public Date getBirthdate() {
        return patient.getBirthdate();
    }

    public String getTelephoneNumber() {
        String telephoneNumber = null;
        PersonAttributeType type = emrApiProperties.getTelephoneAttributeType();
        if (type != null) {
            PersonAttribute attr = patient.getAttribute(type);
            if (attr != null && attr.getValue() != null) {
                telephoneNumber = attr.getValue();
            }
        }
        return telephoneNumber;
    }

    public PersonAddress getPersonAddress() {
        return patient.getPersonAddress();
    }

    public PatientIdentifier getPrimaryIdentifier() {
        List<PatientIdentifier> primaryIdentifiers = getPrimaryIdentifiers();
        if (primaryIdentifiers.size() == 0) {
            return null;
        } else {
            return primaryIdentifiers.get(0);
        }
    }

    public List<PatientIdentifier> getPrimaryIdentifiers() {
        return patient.getPatientIdentifiers(emrApiProperties.getPrimaryIdentifierType());
    }

// This can no longer be on PatientDomainWrapper since we pulled out the paperrecord module
//    public PatientIdentifier getPaperRecordIdentifier() {
//        List<PatientIdentifier> paperRecordIdentifiers = getPaperRecordIdentifiers();
//        if (paperRecordIdentifiers.size() == 0) {
//            return null;
//        } else {
//            return paperRecordIdentifiers.get(0);
//        }
//    }

    public List<PatientIdentifier> getExtraIdentifiers() {
        List<PatientIdentifier> patientIdentifiers = null;
        List<PatientIdentifierType> types = emrApiProperties.getExtraPatientIdentifierTypes();
        if (types != null && types.size() > 0) {
            patientIdentifiers = new ArrayList<PatientIdentifier>();
            for (PatientIdentifierType type : types) {
                PatientIdentifier patientIdentifier = patient.getPatientIdentifier(type);
                if (patientIdentifier != null) {
                    patientIdentifiers.add(patientIdentifier);
                }
            }
        }
        return patientIdentifiers;
    }

// This can no longer be on PatientDomainWrapper since we pulled out the paperrecord module
//    public List<PatientIdentifier> getPaperRecordIdentifiers() {
//        PatientIdentifierType paperRecordIdentifierType = emrApiProperties.getPaperRecordIdentifierType();
//        if (paperRecordIdentifierType == null) {
//            return new ArrayList<PatientIdentifier>();
//        }
//        return patient.getPatientIdentifiers(paperRecordIdentifierType);
//    }

    public Encounter getLastEncounter() {
        return adtService.getLastEncounter(patient);
    }

    public VisitDomainWrapper getActiveVisit(Location location) {
        return adtService.getActiveVisit(patient, location);
    }

    public int getCountOfEncounters() {
        return adtService.getCountOfEncounters(patient);
    }

    public int getCountOfVisits() {
        return adtService.getCountOfVisits(patient);
    }

    public List<Encounter> getAllEncounters() {
        return encounterService.getEncountersByPatient(patient);
    }

    public List<Visit> getAllVisits() {
        return visitService.getVisitsByPatient(patient, true, false);
    }

    public boolean hasOverlappingVisitsWith(Patient otherPatient) {
        List<Visit> otherVisits = visitService.getVisitsByPatient(otherPatient, true, false);
        List<Visit> myVisits = getAllVisits();

        for (Visit v : myVisits) {
            for (Visit o : otherVisits) {
                if (adtService.visitsOverlap(v, o)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isUnknownPatient() {
        boolean unknownPatient = false;
        PersonAttributeType unknownPatientAttributeType = emrApiProperties.getUnknownPatientPersonAttributeType();
        if (patient != null) {
            PersonAttribute att = patient.getAttribute(unknownPatientAttributeType);
            if (att != null && "true".equals(att.getValue())) {
                unknownPatient = true;
            }
        }
        return unknownPatient;
    }

    public List<VisitDomainWrapper> getAllVisitsUsingWrappers() {
        List<VisitDomainWrapper> visitDomainWrappers = new ArrayList<VisitDomainWrapper>();

        for (Visit visit : getAllVisits()) {
            VisitDomainWrapper visitWrapper = new VisitDomainWrapper(visit);
            visitWrapper.setEmrApiProperties(emrApiProperties);
            visitDomainWrappers.add(visitWrapper);
        }

        return visitDomainWrappers;
    }

    public String getFormattedName() {
        return getPersonName().getFamilyName() + ", " + getPersonName().getGivenName();
    }

    public PersonName getPersonName() {
        Set<PersonName> names = patient.getNames();
        if (names != null && names.size() > 0) {
            for (PersonName name : names) {
                if (name.isPreferred())
                    return name;
            }
            for (PersonName name : names) {
                return name;
            }

        }
        return null;
    }

    public boolean isTestPatient() {
        boolean testPatient = false;
        PersonAttributeType testPatientPersonAttributeType = emrApiProperties.getTestPatientPersonAttributeType();
        if (patient != null) {
            PersonAttribute att = patient.getAttribute(testPatientPersonAttributeType);
            if (att != null && "true".equals(att.getValue())) {
                testPatient = true;
            }
        }
        return testPatient;
    }
}
