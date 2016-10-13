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

import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.descriptor.ConceptSetDescriptor;
import org.openmrs.module.emrapi.descriptor.ConceptSetDescriptorField;

/**
 * Metadata describing how a diagnosis is represented as an Obs group.
 */
public class DiagnosisMetadata extends ConceptSetDescriptor {

    private Concept diagnosisSetConcept;
    private Concept codedDiagnosisConcept;
    private Concept nonCodedDiagnosisConcept;
    private Concept diagnosisOrderConcept;
    private Concept diagnosisCertaintyConcept;

    private ConceptSource emrConceptSource;

    public DiagnosisMetadata(ConceptService conceptService, ConceptSource emrConceptSource) {
        setup(conceptService, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME, ConceptSetDescriptorField.required("diagnosisSetConcept", EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CONCEPT_SET),
                ConceptSetDescriptorField.required("codedDiagnosisConcept", EmrApiConstants.CONCEPT_CODE_CODED_DIAGNOSIS),
                ConceptSetDescriptorField.required("nonCodedDiagnosisConcept", EmrApiConstants.CONCEPT_CODE_NON_CODED_DIAGNOSIS),
                ConceptSetDescriptorField.required("diagnosisOrderConcept", EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_ORDER),
                ConceptSetDescriptorField.required("diagnosisCertaintyConcept", EmrApiConstants.CONCEPT_CODE_DIAGNOSIS_CERTAINTY));
        this.emrConceptSource = emrConceptSource;
    }

    /**
     * Used for testing -- in production you'll use the constructor that takes ConceptService
     */
    public DiagnosisMetadata() {
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

    public Concept getDiagnosisCertaintyConcept() {
        return diagnosisCertaintyConcept;
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

    public void setDiagnosisCertaintyConcept(Concept diagnosisCertaintyConcept) {
        this.diagnosisCertaintyConcept = diagnosisCertaintyConcept;
    }

    public void setEmrConceptSource(ConceptSource emrConceptSource) {
        this.emrConceptSource = emrConceptSource;
    }

    public Obs buildDiagnosisObsGroup(Diagnosis diagnosis) {
        Concept orderAnswer = findAnswer(diagnosisOrderConcept, diagnosis.getOrder().getCodeInEmrConceptSource());
        Concept certaintyAnswer = findAnswer(diagnosisCertaintyConcept, diagnosis.getCertainty().getCodeInEmrConceptSource());

        Obs existingObs = diagnosis.getExistingObs();
        if (existingObs != null) {
            setCodedMember(existingObs, diagnosisOrderConcept, orderAnswer, null);
            setCodedMember(existingObs, diagnosisCertaintyConcept, certaintyAnswer, null);
            setCodedOrFreeTextMember(existingObs, diagnosis.getDiagnosis(), codedDiagnosisConcept, nonCodedDiagnosisConcept);
            return existingObs;
        }
        else {
            Obs order = buildObsFor(diagnosisOrderConcept, orderAnswer, null);
            Obs certainty = buildObsFor(diagnosisCertaintyConcept, certaintyAnswer, null);
            Obs diagnosisObs = buildObsFor(diagnosis.getDiagnosis(), codedDiagnosisConcept, nonCodedDiagnosisConcept);

            Obs obs = new Obs();
            obs.setConcept(diagnosisSetConcept);
            obs.addGroupMember(order);
            obs.addGroupMember(certainty);
            obs.addGroupMember(diagnosisObs);
            return obs;
        }
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

    public Diagnosis toDiagnosis(Obs obsGroup) {
        if (!isDiagnosis(obsGroup)) {
            throw new IllegalArgumentException("Not an obs group for a diagnosis: " + obsGroup);
        }

        Obs orderObs = findMember(obsGroup, diagnosisOrderConcept);
        Obs certaintyObs = findMember(obsGroup, diagnosisCertaintyConcept);
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
        if (certaintyObs != null) {
            diagnosis.setCertainty(getDiagnosisCertaintyFrom(certaintyObs));
        }
        diagnosis.setExistingObs(obsGroup);
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

    public Concept getConceptFor(Diagnosis.Certainty certainty) {
        if (certainty == null) {
            return null;
        }
        return findAnswer(getDiagnosisCertaintyConcept(), certainty.getCodeInEmrConceptSource());
    }

    private Diagnosis.Certainty getDiagnosisCertaintyFrom(Obs certaintyObs) {
        String mapping = findMapping(certaintyObs.getValueCoded());
        return Diagnosis.Certainty.parseConceptReferenceCode(mapping);
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