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
import org.openmrs.ConceptMap;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class DispositionMapper {
    private final ConceptService conceptService;

    public DispositionMapper(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public EncounterTransaction.Disposition getDisposition(Obs obs) {
        EncounterTransaction.Disposition disposition = new EncounterTransaction.Disposition();
        Set<Obs> groupMembers = obs.getGroupMembers();
        List<EncounterTransaction.Observation> additionalObservations = new ArrayList<EncounterTransaction.Observation>();
        for (Obs groupMember : groupMembers) {
            if (isDisposition(groupMember)) {
                disposition.setCode(getConceptMappingCodeBySource(EmrApiConstants.EMR_CONCEPT_SOURCE_NAME, groupMember.getValueCoded().getConceptMappings()));
                disposition.setVoided(groupMember.getVoided());
                disposition.setVoidReason(groupMember.getVoidReason());
                disposition.setExistingObs(groupMember.getUuid());
            } else {
                EncounterTransaction.Observation observation = new EncounterTransaction.Observation();
                observation.setConcept(new EncounterTransaction.Concept(groupMember.getConcept().getUuid(), groupMember.getConcept().getName().getName()));
                observation.setValue(groupMember.getValueAsString(Context.getLocale()));
                observation.setVoidReason(groupMember.getVoidReason());
                observation.setVoided(groupMember.getVoided());
                observation.setComment(groupMember.getComment());
                observation.setUuid(groupMember.getUuid());
                additionalObservations.add(observation);
            }
        }
        disposition.setAdditionalObs(additionalObservations);
        disposition.setDispositionDate(obs.getObsDatetime());
        return disposition;
    }

    private String getConceptMappingCodeBySource(String  source,Collection<ConceptMap> conceptMappings){
        for (ConceptMap conceptMapping : conceptMappings) {
            if(conceptMapping.getConceptReferenceTerm().getConceptSource().getName().equals(source)){
                return  conceptMapping.getConceptReferenceTerm().getCode();
            }
        }
        return null;
    }

    public boolean isDispositionGroup(Obs obs) {
        Concept dispositionGroupConcept = getDispositionGroupConcept();
        return obs.getConcept().getUuid().equals(dispositionGroupConcept.getUuid());
    }

    boolean isDisposition(Obs obs) {
        Concept dispositionConcept = getDispositionConcept();
        return obs.getConcept().getUuid().equals(dispositionConcept.getUuid());
    }

    Concept getDispositionGroupConcept() {
        Concept concept = conceptService.getConceptByMapping(EmrApiConstants.CONCEPT_CODE_DISPOSITION_CONCEPT_SET, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
        if (concept == null) {
            throw new ConceptNotFoundException("Disposition group concept does not exist. Code : " + EmrApiConstants.CONCEPT_CODE_DISPOSITION_CONCEPT_SET);
        }
        return concept;
    }

    Concept getDispositionConcept() {
        Concept concept = conceptService.getConceptByMapping(EmrApiConstants.CONCEPT_CODE_DISPOSITION, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
        if (concept == null) {
            throw new ConceptNotFoundException("Disposition concept does not exist. Code : " + EmrApiConstants.CONCEPT_CODE_DISPOSITION);
        }
        return concept;
    }
}