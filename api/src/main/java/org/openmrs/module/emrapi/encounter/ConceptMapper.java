/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.encounter;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.utils.HibernateLazyLoader;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;

import java.util.Locale;

public class ConceptMapper {
	
	public EncounterTransaction.Concept map(Concept concept) {
		if (concept == null) {
			return null;
		}
		concept = new HibernateLazyLoader().load(concept);
		ConceptClass conceptClass = concept.getConceptClass();
		String conceptClassName = (conceptClass != null) ? conceptClass.getName() : null;
		
		EncounterTransaction.Concept encounterTransactionConcept = new EncounterTransaction.Concept(concept.getUuid(),
		        concept.getName().getName(), concept.isSet(), concept.getDatatype().getName(), null, conceptClassName,
		        getShortName(concept), concept.getConceptMappings());
		
		if (concept.isNumeric()) {
			encounterTransactionConcept.setUnits(((ConceptNumeric) concept).getUnits());
			encounterTransactionConcept.setHiNormal(((ConceptNumeric) concept).getHiNormal());
			encounterTransactionConcept.setLowNormal(((ConceptNumeric) concept).getLowNormal());
		}
		return encounterTransactionConcept;
	}
	
	private String getShortName(Concept concept) {
		User authenticatedUser = Context.getAuthenticatedUser();
		String shortName = null;
		if (authenticatedUser != null) {
			Locale userDefaultLocale = getUserDefaultLocale(authenticatedUser);
			shortName = getAnAvailableName(concept, userDefaultLocale);
			if (shortName == null) {
				Locale systemDefaultLocale = getSystemDefaultLocale();
				shortName = getAnAvailableName(concept, systemDefaultLocale);
			}
		}
		if (shortName == null) {
			shortName = concept.getName().getName();
		}
		return shortName;
	}
	
	private String getAnAvailableName(Concept concept, Locale locale) {
		ConceptName shortNameInLocale = concept.getShortNameInLocale(locale);
		if (shortNameInLocale != null) {
			return shortNameInLocale.getName();
		} else {
			ConceptName fullySpecifiedName = concept.getFullySpecifiedName(locale);
			if (fullySpecifiedName != null) {
				return fullySpecifiedName.getName();
			}
		}
		return null;
	}
	
	private Locale getSystemDefaultLocale() {
		String systemDefaultLocale = Context.getAdministrationService()
		        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE);
		if (systemDefaultLocale != null) {
			return LocaleUtility.fromSpecification(systemDefaultLocale);
		}
		return null;
	}
	
	private Locale getUserDefaultLocale(User authenticatedUser) {
		String userDefaultLocale = authenticatedUser.getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE);
		return LocaleUtility.fromSpecification(userDefaultLocale);
	}
}
