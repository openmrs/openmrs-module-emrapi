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
import org.openmrs.ConceptNumeric;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.utils.HibernateLazyLoader;

public class ConceptMapper {
    public EncounterTransaction.Concept map(Concept concept) {
        if (concept == null){
            return null;
        }
        concept = new HibernateLazyLoader().load(concept);
        ConceptClass conceptClass = concept.getConceptClass();
        String conceptClassName = (conceptClass != null) ? conceptClass.getName() : null;

        String shortName = concept.getShortNames() != null && concept.getShortNames().size() > 0 ? concept.getShortNames().iterator().next().getName() : null;

        EncounterTransaction.Concept encounterTransactionConcept = new EncounterTransaction.Concept(concept.getUuid(),
                concept.getName().getName(), concept.isSet(), concept.getDatatype().getName(), null,
                conceptClassName, shortName);
        if(concept.isNumeric() && ((ConceptNumeric) concept).getUnits() != null) {
            encounterTransactionConcept.setUnits(((ConceptNumeric) concept).getUnits());
        }
        return encounterTransactionConcept;
    }
}
