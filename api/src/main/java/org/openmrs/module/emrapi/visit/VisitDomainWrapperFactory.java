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
package org.openmrs.module.emrapi.visit;

import java.util.Date;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.springframework.beans.factory.annotation.Autowired;

public class VisitDomainWrapperFactory {
	
	@Autowired
	private EmrApiProperties emrApiProperties;
	
	public VisitDomainWrapper createNewVisit(Patient patient, Location location, Date visitTime) {
		Visit visit = new Visit();
		visit.setPatient(patient);
		visit.setLocation(getVisitLocation(location));
		visit.setStartDatetime(visitTime);
		
		visit.setVisitType(emrApiProperties.getAtFacilityVisitType());
		
		return new VisitDomainWrapper(visit);
	}
	
	private Location getVisitLocation(Location location) {
		if (location == null) {
			throw new IllegalArgumentException("Location does not support visits");
		}
		return location.hasTag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS) ? location : getVisitLocation(location
		        .getParentLocation());
	}
}
