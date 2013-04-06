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

package org.openmrs.module.emrapi.diagnosis;

import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiConstants;

/**
 * Metadata describing how a diagnosis is represented as an Obs group.
 * TODO refactor to pull more of this functionality into a base class, e.g. ConceptSetDescriptor
 */
public class DiagnosisMetadata {

    private Concept diagnosisSetConcept;
    private Concept codedDiagnosisConcept;
    private Concept nonCodedDiagnosisConcept;
    private Concept diagnosisOrderConcept;

    private ConceptSource emrConceptSource;

    public DiagnosisMetadata(ConceptService conceptService, ConceptSource emrConceptSource) {
        setup(conceptService, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME, "diagnosisSetConcept", EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CONCEPT_SET,
                "codedDiagnosisConcept", EmrApiConstants.CONCEPT_CODE_CODED_DIAGNOSIS,
                "nonCodedDiagnosisConcept", EmrApiConstants.CONCEPT_CODE_NON_CODED_DIAGNOSIS,
                "diagnosisOrderConcept", EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER);
        this.emrConceptSource = emrConceptSource;
    }

    /**
     * Used for testing -- in production you'll use the constructor that takes ConceptService
     */
    public DiagnosisMetadata() {
    }

    private void setup(ConceptService conceptService, String conceptSourceName, String... fieldsAndConceptCodes) {
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

    public Concept getDiagnosisSetConcept() {
        return diagnosisSetConcept;
    }

    public Concept getCodedDiagnosisConcept() {
        return codedDiagnosisConcept;
    }

    public Concept getNonCodedDiagnosisConcept() {
        return nonCodedDiagnosisConcept;
    }

    public Concept getDiagnosisOrderConcept() {
        return diagnosisOrderConcept;
    }

    public void setDiagnosisSetConcept(Concept diagnosisSetConcept) {
        this.diagnosisSetConcept = diagnosisSetConcept;
    }

    public void setCodedDiagnosisConcept(Concept codedDiagnosisConcept) {
        this.codedDiagnosisConcept = codedDiagnosisConcept;
    }

    public void setNonCodedDiagnosisConcept(Concept nonCodedDiagnosisConcept) {
        this.nonCodedDiagnosisConcept = nonCodedDiagnosisConcept;
    }

    public void setDiagnosisOrderConcept(Concept diagnosisOrderConcept) {
        this.diagnosisOrderConcept = diagnosisOrderConcept;
    }

    public ConceptSource getEmrConceptSource() {
        return emrConceptSource;
    }

    public void setEmrConceptSource(ConceptSource emrConceptSource) {
        this.emrConceptSource = emrConceptSource;
    }

    public Obs buildDiagnosisObsGroup(Diagnosis diagnosis) {
        Obs order = new Obs();
        order.setConcept(diagnosisOrderConcept);
        order.setValueCoded(findAnswer(diagnosisOrderConcept, diagnosis.getOrder().getCodeInEmrConceptSource()));

        Obs diagnosisObs = buildObsFor(diagnosis.getDiagnosis(), codedDiagnosisConcept, nonCodedDiagnosisConcept);

        Obs obs = new Obs();
        obs.setConcept(diagnosisSetConcept);
        obs.addGroupMember(order);
        obs.addGroupMember(diagnosisObs);
        return obs;
    }

    private Obs buildObsFor(CodedOrFreeTextAnswer codedOrFreeTextAnswer, Concept questionIfCoded, Concept questionIfNonCoded) {
        Obs obs = new Obs();
        if (codedOrFreeTextAnswer.getNonCodedAnswer() != null) {
            obs.setConcept(nonCodedDiagnosisConcept);
            obs.setValueText(codedOrFreeTextAnswer.getNonCodedAnswer());
        } else {
            obs.setConcept(codedDiagnosisConcept);
            obs.setValueCoded(codedOrFreeTextAnswer.getCodedAnswer());
            obs.setValueCodedName(codedOrFreeTextAnswer.getSpecificCodedAnswer());
        }
        return obs;
    }

    private Concept findAnswer(Concept concept, String codeForAnswer) {
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

    public boolean isDiagnosis(Obs obsGroup) {
        return obsGroup.getConcept().equals(diagnosisSetConcept);
    }

    public boolean isPrimaryDiagnosis(Obs obsGroup) {
        return isDiagnosis(obsGroup) && hasDiagnosisOrder(obsGroup, EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER_PRIMARY);
    }

    private boolean hasDiagnosisOrder(Obs obsGroup, String codeForDiagnosisOrderToCheckFor) {
        Obs orderObs = findMember(obsGroup, diagnosisOrderConcept);
        //return orderObs.getValueCoded()
        throw new RuntimeException("Not Yet Implemented");
    }

    private Obs findMember(Obs obsGroup, Concept concept) {
        for (Obs candidate : obsGroup.getGroupMembers(false)) {
            if (candidate.getConcept().equals(concept)) {
                return candidate;
            }
        }
        return null;
    }

    public Diagnosis toDiagnosis(Obs obsGroup) {
        if (!isDiagnosis(obsGroup)) {
            throw new IllegalArgumentException("Not an obs group for a diagnosis: " + obsGroup);
        }

        Obs orderObs = findMember(obsGroup, diagnosisOrderConcept);
        Obs codedObs = findMember(obsGroup, codedDiagnosisConcept);
        Obs nonCodedObs = null;
        if (codedObs == null) {
            nonCodedObs = findMember(obsGroup, nonCodedDiagnosisConcept);
        }
        if (codedObs == null && nonCodedObs == null) {
            throw new IllegalArgumentException("Obs group doesn't contain a coded or non-coded diagnosis: " + obsGroup);
        }
        CodedOrFreeTextAnswer diagnosisValue = buildFrom(codedObs, nonCodedObs);
        Diagnosis diagnosis = new Diagnosis(diagnosisValue, getDiagnosisOrderFrom(orderObs));
        return diagnosis;
    }

    /**
     * @param order
     * @return the Concept representing this diagnosis order
     */
    public Concept getConceptFor(Diagnosis.Order order) {
        if (order == null) {
            return null;
        }
        return findAnswer(getDiagnosisOrderConcept(), order.getCodeInEmrConceptSource());
    }

    private Diagnosis.Order getDiagnosisOrderFrom(Obs obs) {
        String mapping = findMapping(obs.getValueCoded());
        return Diagnosis.Order.parseConceptReferenceCode(mapping);

    }

    private String findMapping(Concept concept) {
        for (ConceptMap conceptMap : concept.getConceptMappings()) {
            ConceptReferenceTerm conceptReferenceTerm = conceptMap.getConceptReferenceTerm();
            if (conceptReferenceTerm.getConceptSource().equals(emrConceptSource)) {
                return conceptReferenceTerm.getCode();
            }
        }
        return null;
    }

    private CodedOrFreeTextAnswer buildFrom(Obs codedObs, Obs nonCodedObs) {
        if (codedObs != null) {
            if (codedObs.getValueCodedName() != null) {
                return new CodedOrFreeTextAnswer(codedObs.getValueCodedName());
            } else {
                return new CodedOrFreeTextAnswer(codedObs.getValueCoded());
            }
        } else {
            return new CodedOrFreeTextAnswer(nonCodedObs.getValueText());
        }
    }
}
