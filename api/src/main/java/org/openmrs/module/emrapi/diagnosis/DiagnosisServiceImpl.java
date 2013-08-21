package org.openmrs.module.emrapi.diagnosis;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiProperties;

public class DiagnosisServiceImpl extends BaseOpenmrsService implements DiagnosisService {

    private EmrApiProperties emrApiProperties;

    private ObsService obsService;

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public void setObsService(ObsService obsService) {
        this.obsService = obsService;
    }

    @Override
    public Obs codeNonCodedDiagnosis(Obs nonCodedObs, Concept codedDiagnosis) {

        if ( (nonCodedObs != null) && (codedDiagnosis != null) ){
            Concept codedDiagnosisConcept = emrApiProperties.getDiagnosisMetadata().getCodedDiagnosisConcept();
            nonCodedObs.setConcept(codedDiagnosisConcept);
            nonCodedObs.setValueCoded(codedDiagnosis);
            nonCodedObs.setValueText("");
            nonCodedObs =  obsService.saveObs(nonCodedObs, "code a diagnosis");
            return nonCodedObs;

        }
        return null;
    }
}
