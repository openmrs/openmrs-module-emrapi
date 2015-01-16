package org.openmrs.module.emrapi.encounter.matcher;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;


public class ObservationTypeMatcher {
    private EmrApiProperties emrApiProperties;
    private DiagnosisMetadata diagnosisMetadata;
    private ConceptService conceptService;

    public static enum ObservationType {DIAGNOSIS,DISPOSITION,OBSERVATION};

    public ObservationTypeMatcher(EmrApiProperties emrApiProperties, ConceptService conceptService) {
        this.emrApiProperties = emrApiProperties;
        this.conceptService = conceptService;
    }

    public ObservationType getObservationType(Obs obs){
        if (getDiagnosisMetadata().isDiagnosis(obs)) {
            return ObservationType.DIAGNOSIS;
        } else if(isDispositionGroup(obs) || isDisposition(obs)) {
            return ObservationType.DISPOSITION;
        }
        return ObservationType.OBSERVATION;
    }

    private DiagnosisMetadata getDiagnosisMetadata() {
        if (this.diagnosisMetadata == null) {
            this.diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
        }
        return this.diagnosisMetadata;
    }

    private boolean isDispositionGroup(Obs obs) {
        Concept dispositionGroupConcept = getDispositionGroupConcept();
        return obs.getConcept().getUuid().equals(dispositionGroupConcept.getUuid());
    }

    private boolean isDisposition(Obs obs) {
        Concept dispositionConcept = getDispositionConcept();
        return obs.getConcept().getUuid().equals(dispositionConcept.getUuid());
    }

    private Concept getDispositionGroupConcept() {
        Concept concept = conceptService.getConceptByMapping(EmrApiConstants.CONCEPT_CODE_DISPOSITION_CONCEPT_SET, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
        if (concept == null) {
            throw new ConceptNotFoundException("Disposition group concept does not exist. Code : " + EmrApiConstants.CONCEPT_CODE_DISPOSITION_CONCEPT_SET);
        }
        return concept;
    }

    private Concept getDispositionConcept() {
        Concept concept = conceptService.getConceptByMapping(EmrApiConstants.CONCEPT_CODE_DISPOSITION, EmrApiConstants.EMR_CONCEPT_SOURCE_NAME);
        if (concept == null) {
            throw new ConceptNotFoundException("Disposition concept does not exist. Code : " + EmrApiConstants.CONCEPT_CODE_DISPOSITION);
        }
        return concept;
    }
}
