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
     * @param fieldsAndConceptCodes must have an even number of elements. The first of each pair is the field name to set (on a subclass) and the second is the code in conceptSourceName.
     */
    protected void setup(ConceptService conceptService, String conceptSourceName, String... fieldsAndConceptCodes) {
        try {
            String primaryConceptCode = fieldsAndConceptCodes[1];
            Concept primaryConcept = conceptService.getConceptByMapping(primaryConceptCode, conceptSourceName);
            if (primaryConcept == null) {
                throw new IllegalStateException("Couldn't find primary concept for " + getClass().getSimpleName() + " which should be mapped as " + conceptSourceName + ":" + primaryConceptCode);
            }
            PropertyUtils.setProperty(this, fieldsAndConceptCodes[0], primaryConcept);
            for (int i = 2; i < fieldsAndConceptCodes.length; i += 2) {
                String propertyName = fieldsAndConceptCodes[i];
                String mappingCode = fieldsAndConceptCodes[i + 1];
                Concept childConcept = conceptService.getConceptByMapping(mappingCode, conceptSourceName);
                if (childConcept == null) {
                    throw new IllegalStateException("Couldn't find " + propertyName + " concept for " + getClass().getSimpleName() + " which should be mapped as " + conceptSourceName + ":" + mappingCode);
                }
                if (!primaryConcept.getSetMembers().contains(childConcept)) {
                    throw new IllegalStateException("Concept mapped as " + conceptSourceName + ":" + mappingCode + " needs to be a set member of concept " + primaryConcept.getConceptId() + " which is mapped as " + conceptSourceName + ":" + primaryConceptCode);
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
        for (ConceptAnswer conceptAnswer : concept.getAnswers()) {
            Concept answerConcept = conceptAnswer.getAnswerConcept();
            if (answerConcept != null) {
                if (hasConceptMapping(answerConcept, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME, codeForAnswer)) {
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
