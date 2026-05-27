/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.rest.converter;

import org.openmrs.annotation.Handler;
import org.openmrs.module.emrapi.adt.InpatientAdmission;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

@Handler(supports = InpatientAdmission.class, order = 0)
public class InpatientAdmissionConverter extends SimpleBeanConverter<InpatientAdmission> {
	
	@Override
	public DelegatingResourceDescription getResourceDescription(InpatientAdmission req, Representation representation) {
		DelegatingResourceDescription ret = super.getResourceDescription(req, representation);
		if (representation instanceof DefaultRepresentation) {
			DelegatingResourceDescription rep = new DelegatingResourceDescription();
			rep.addProperty("visit", Representation.DEFAULT);
			rep.addProperty("currentInpatientLocation", Representation.REF);
			rep.addProperty("firstAdmissionOrTransferEncounter", getEncounterRepresentation());
			rep.addProperty("latestAdmissionOrTransferEncounter", getEncounterRepresentation());
			rep.addProperty("encounterAssigningToCurrentInpatientLocation", getEncounterRepresentation());
			rep.addProperty("currentInpatientRequest", getInpatientRequestRepresentation());
			rep.addProperty("discharged");
			return rep;
		} else if (representation instanceof FullRepresentation) {
			for (String property : ret.getProperties().keySet()) {
				if (!property.equals("visit")) {
					ret.addProperty(property, Representation.DEFAULT);
				}
			}
		}
		return ret;
	}
	
	public Representation getEncounterRepresentation() {
		return new CustomRepresentation("uuid,display,encounterDatetime,location:ref,encounterType:ref");
	}
	
	public Representation getInpatientRequestRepresentation() {
		return new CustomRepresentation(
		        "dispositionType,dispositionEncounter:(uuid,display,encounterDatetime),dispositionLocation:ref");
	}
}
