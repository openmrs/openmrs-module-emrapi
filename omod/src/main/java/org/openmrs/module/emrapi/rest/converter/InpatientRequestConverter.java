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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.emrapi.adt.InpatientRequest;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

@Handler(supports = InpatientRequest.class, order = 0)
public class InpatientRequestConverter extends SimpleBeanConverter<InpatientRequest> {
	
	private final Log log = LogFactory.getLog(getClass());
	
	@Override
	public DelegatingResourceDescription getResourceDescription(InpatientRequest req, Representation representation) {
		DelegatingResourceDescription ret = super.getResourceDescription(req, representation);
		if (representation instanceof DefaultRepresentation) {
			for (String property : ret.getProperties().keySet()) {
				if (!property.equals("visit")) {
					ret.addProperty(property, Representation.REF);
				}
			}
		} else if (representation instanceof FullRepresentation) {
			for (String property : ret.getProperties().keySet()) {
				if (!property.equals("visit")) {
					ret.addProperty(property, Representation.DEFAULT);
				}
			}
		}
		return ret;
	}
}
