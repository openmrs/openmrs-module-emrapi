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

        EncounterTransaction.Concept encounterTransactionConcept = new EncounterTransaction.Concept(
                concept.getUuid(),
                concept.getName().getName(),
                concept.isSet(),
                concept.getDatatype().getName(),
                null,
                conceptClassName,
                getShortName(concept),
                concept.getConceptMappings());

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
        String systemDefaultLocale = Context.getAdministrationService().getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE);
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
