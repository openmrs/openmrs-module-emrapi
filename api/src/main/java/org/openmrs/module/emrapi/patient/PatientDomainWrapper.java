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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapper;
import org.openmrs.module.emrapi.domainwrapper.DomainWrapperFactory;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.reporting.query.visit.service.VisitQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * A rich-domain-model class that wraps a Patient, and lets you perform common queries.
 */
public class PatientDomainWrapper implements DomainWrapper {

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

	@Qualifier("emrDiagnosisService")
	@Autowired
	protected DiagnosisService diagnosisService;

   @Autowired
   protected VisitQueryService visitQueryService;
    
   @Qualifier("domainWrapperFactory")
   @Autowired
   protected DomainWrapperFactory domainWrapperFactory;

	public PatientDomainWrapper() {
	}

    @Deprecated  // use the PatientDomainWrapperFactory component to instantiate a new PDW
	public PatientDomainWrapper(Patient patient, EmrApiProperties emrApiProperties, AdtService adtService,
								VisitService visitService, EncounterService encounterService, DiagnosisService diagnosisService, DomainWrapperFactory domainWrapperFactory) {
		this.patient = patient;
		this.emrApiProperties = emrApiProperties;
		this.adtService = adtService;
		this.visitService = visitService;
		this.encounterService = encounterService;
		this.diagnosisService = diagnosisService;
		this.domainWrapperFactory = domainWrapperFactory; 
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
	}

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public void setAdtService(AdtService adtService) {
        this.adtService = adtService;
    }

    public void setVisitService(VisitService visitService) {
        this.visitService = visitService;
    }

    public void setEncounterService(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    public void setDiagnosisService(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    public void setVisitQueryService(VisitQueryService visitQueryService) {
        this.visitQueryService = visitQueryService;
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


    public List<PatientIdentifier> getExtraIdentifiers() {
	    return getExtraIdentifiers(null);
    }

    /**
     * Return all extra identifiers associated with this patient, restricted by the specified location
     *
     * If an identifier type has locationBehaviour = REQUIRED, only return identifiers for which
     * the specified location fails within the hierarchy of the location associated with the identifier (ie, if "Clinic B"
     * is passed in as the location parameter, don't return identifiers that fall under the "Clinic A" location hierarchy)
     *
     * @param location
     * @return
     */
   public List<PatientIdentifier> getExtraIdentifiers(Location location) {

       List<PatientIdentifier> patientIdentifiers = null;
       List<PatientIdentifierType> types = emrApiProperties.getExtraPatientIdentifierTypes();

       if (types != null && types.size() > 0) {
           patientIdentifiers = new ArrayList<PatientIdentifier>();

           for (PatientIdentifierType type : types) {
               List<PatientIdentifier> extraPatientIdentifiers = patient.getPatientIdentifiers(type);

               if (extraPatientIdentifiers != null) {

                   for (PatientIdentifier extraPatientIdentifier: extraPatientIdentifiers) {
                        if (type.getLocationBehavior() == null || !type.getLocationBehavior().equals(PatientIdentifierType.LocationBehavior.REQUIRED)
                                || location == null || Location.isInHierarchy(location, extraPatientIdentifier.getLocation())) {
                            patientIdentifiers.add(extraPatientIdentifier);
                        }

                   }
               }
           }
       }
       return patientIdentifiers;
   }

    public Map<PatientIdentifierType, List<PatientIdentifier>> getExtraIdentifiersMappedByType(Location location) {

        Map<PatientIdentifierType, List<PatientIdentifier>> identifierMap = new HashMap<PatientIdentifierType, List<PatientIdentifier>>();

        List<PatientIdentifier> patientIdentifiers = getExtraIdentifiers(location);

        if (patientIdentifiers != null) {

            for (PatientIdentifier patientIdentifier : patientIdentifiers) {

                if (!identifierMap.containsKey(patientIdentifier.getIdentifierType())) {
                    identifierMap.put(patientIdentifier.getIdentifierType(), new ArrayList<PatientIdentifier>());
                }
                identifierMap.get(patientIdentifier.getIdentifierType()).add(patientIdentifier);
            }
        }

        return identifierMap;
    }

    public Map<PatientIdentifierType, List<PatientIdentifier>> getExtraIdentifiersMappedByType() {
        return getExtraIdentifiersMappedByType(null);
    }


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
			VisitDomainWrapper visitWrapper = domainWrapperFactory.newVisitDomainWrapper(visit);
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

	public List<Diagnosis> getDiagnosesSince(Date date) {
		List<Diagnosis> diagnoses = diagnosisService.getDiagnoses(patient, date);

		return diagnoses;
	}

	public List<Diagnosis> getUniqueDiagnosesSince(Date date) {
		List<Diagnosis> diagnoses = diagnosisService.getUniqueDiagnoses(patient, date);

		return diagnoses;
	}

}
