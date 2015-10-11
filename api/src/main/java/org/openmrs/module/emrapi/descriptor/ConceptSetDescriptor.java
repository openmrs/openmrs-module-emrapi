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

package org.openmrs.module.emrapi.descriptor;

import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.util.OpenmrsUtil;

public abstract class ConceptSetDescriptor {

    /**
     * @param conceptService
     * @param conceptSourceName
     * @param primaryConceptField Field for primary concept. This concept is mandatory
     * @param memberConceptFields Fields for member concepts of primary concept. These concepts can be mandatory or optional.
     */
    protected void setup(ConceptService conceptService, String conceptSourceName, ConceptSetDescriptorField primaryConceptField, ConceptSetDescriptorField... memberConceptFields) {
        try {
            String primaryConceptCode = primaryConceptField.getConceptCode();
            Concept primaryConcept = conceptService.getConceptByMapping(primaryConceptCode, conceptSourceName);
            if (primaryConcept == null) {
                throw new MissingConceptException("Couldn't find primary concept for " + getClass().getSimpleName() + " which should be mapped as " + conceptSourceName + ":" + primaryConceptCode);
            }
            PropertyUtils.setProperty(this, primaryConceptField.getName(), primaryConcept);
            for (ConceptSetDescriptorField conceptSetDescriptorField : memberConceptFields) {
                String propertyName = conceptSetDescriptorField.getName();
                String mappingCode = conceptSetDescriptorField.getConceptCode();
                Concept childConcept = conceptService.getConceptByMapping(mappingCode, conceptSourceName);
                if(conceptSetDescriptorField.isRequired()) {
                    if (childConcept == null) {
                        throw new MissingConceptException("Couldn't find " + propertyName + " concept for " + getClass().getSimpleName() + " which should be mapped as " + conceptSourceName + ":" + mappingCode);
                    }
                    if (!primaryConcept.getSetMembers().contains(childConcept)) {
                        throw new IllegalStateException("Concept mapped as " + conceptSourceName + ":" + mappingCode + " needs to be a set member of concept " + primaryConcept.getConceptId() + " which is mapped as " + conceptSourceName + ":" + primaryConceptCode);
                    }
                }
                PropertyUtils.setProperty(this, propertyName, childConcept);
            }
        } catch (Exception ex) {
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else {
                throw new IllegalStateException(ex);
            }
        }
    }

    protected Obs findMember(Obs obsGroup, Concept concept) {
        for (Obs candidate : obsGroup.getGroupMembers(false)) {
            if (candidate.getConcept().equals(concept)) {
                return candidate;
            }
        }
        return null;
    }

    protected void setCodedOrFreeTextMember(Obs obsGroup, CodedOrFreeTextAnswer answer, Concept questionIfCoded, Concept questionIfNonCoded) {
        if (answer.getNonCodedAnswer() != null) {
            setFreeTextMember(obsGroup, questionIfNonCoded, answer.getNonCodedAnswer());
            setCodedMember(obsGroup, questionIfCoded, null, null);
        }
        else {
            setFreeTextMember(obsGroup, questionIfNonCoded, null);
            setCodedMember(obsGroup, questionIfCoded, answer.getCodedAnswer(), answer.getSpecificCodedAnswer());
        }
    }

    private void setFreeTextMember(Obs obsGroup, Concept memberConcept, String memberAnswer) {
        Obs member = findMember(obsGroup, memberConcept);
        boolean needToVoid = member != null && !OpenmrsUtil.nullSafeEquals(memberAnswer, member.getValueText());
        boolean needToCreate = memberAnswer != null && (member == null || needToVoid);
        if (needToVoid) {
            member.setVoided(true);
            member.setVoidReason(getDefaultVoidReason());
        }
        if (needToCreate) {
            addToObsGroup(obsGroup, buildObsFor(memberConcept, memberAnswer));
        }
    }

    /**
     * @return text to put in the voidReason field of an obs that gets voided by an implementation of this class.
     */
    protected String getDefaultVoidReason() {
        return getClass().getSimpleName() + " modifying obs group";
    }

    protected void setCodedMember(Obs obsGroup, Concept memberConcept, Concept memberAnswer, ConceptName specificAnswer) {
        Obs member = findMember(obsGroup, memberConcept);
        boolean needToVoid = member != null &&
                (!memberAnswer.equals(member.getValueCoded())
                        || !OpenmrsUtil.nullSafeEquals(specificAnswer, member.getValueCodedName()));
        boolean needToCreate = memberAnswer != null && (member == null || needToVoid);
        if (needToVoid) {
            member.setVoided(true);
            member.setVoidReason(getDefaultVoidReason());
        }
        if (needToCreate) {
            addToObsGroup(obsGroup, buildObsFor(memberConcept, memberAnswer, specificAnswer));
        }
    }

    private void addToObsGroup(Obs obsGroup, Obs member) {
        member.setPerson(obsGroup.getPerson());
        member.setObsDatetime(obsGroup.getObsDatetime());
        member.setLocation(obsGroup.getLocation());
        member.setEncounter(obsGroup.getEncounter());
        obsGroup.addGroupMember(member);
    }

    protected Obs buildObsFor(Concept question, String answer) {
        Obs obs = new Obs();
        obs.setConcept(question);
        obs.setValueText(answer);
        return obs;
    }

    protected Obs buildObsFor(Concept question, Concept answer, ConceptName answerName) {
        Obs obs = new Obs();
        obs.setConcept(question);
        obs.setValueCoded(answer);
        obs.setValueCodedName(answerName);
        return obs;
    }

    protected Obs buildObsFor(CodedOrFreeTextAnswer codedOrFreeTextAnswer, Concept questionIfCoded, Concept questionIfNonCoded) {
        Obs obs = new Obs();
        if (codedOrFreeTextAnswer.getNonCodedAnswer() != null) {
            obs.setConcept(questionIfNonCoded);
            obs.setValueText(codedOrFreeTextAnswer.getNonCodedAnswer());
        } else {
            obs.setConcept(questionIfCoded);
            obs.setValueCoded(codedOrFreeTextAnswer.getCodedAnswer());
            obs.setValueCodedName(codedOrFreeTextAnswer.getSpecificCodedAnswer());
        }
        return obs;
    }

    protected Concept findAnswer(Concept concept, String codeForAnswer) {
        return findAnswer(concept,EmrApiConstants.EMR_CONCEPT_SOURCE_NAME,codeForAnswer);
    }


    protected Concept findAnswer(Concept concept, String conceptSource, String codeForAnswer) {
        for (ConceptAnswer conceptAnswer : concept.getAnswers()) {
            Concept answerConcept = conceptAnswer.getAnswerConcept();
            if (answerConcept != null) {
                if (hasConceptMapping(answerConcept, conceptSource, codeForAnswer)) {
                    return answerConcept;
                }
            }
        }
        throw new IllegalStateException("Cannot find answer mapped with " + EmrApiConstants.EMR_CONCEPT_SOURCE_NAME + ":" + codeForAnswer + " in the concept " + concept.getName());
    }

    private boolean hasConceptMapping(Concept concept, String sourceName, String codeToLookFor) {
        for (ConceptMap conceptMap : concept.getConceptMappings()) {
            ConceptReferenceTerm conceptReferenceTerm = conceptMap.getConceptReferenceTerm();
            if (sourceName.equals(conceptReferenceTerm.getConceptSource().getName()) && codeToLookFor.equals(conceptReferenceTerm.getCode())) {
                return true;
            }
        }
        return false;
    }
}
