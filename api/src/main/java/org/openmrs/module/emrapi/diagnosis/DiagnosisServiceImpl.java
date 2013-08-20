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
    public Obs codeNonCodedDiagnosis(Integer nonCodedObsId, Concept codedDiagnosis) {

        if ( (nonCodedObsId != null) && (codedDiagnosis != null) ){
            Obs obs = obsService.getObs(nonCodedObsId);
            if(obs != null) {
                Concept codedDiagnosisConcept = emrApiProperties.getDiagnosisMetadata().getCodedDiagnosisConcept();
                obs.setConcept(codedDiagnosisConcept);
                obs.setValueCoded(codedDiagnosis);
                obs.setValueText("");
                obs =  obsService.saveObs(obs, "code a diagnosis");
                return obs;
            }
        }
        return null;
    }
}
