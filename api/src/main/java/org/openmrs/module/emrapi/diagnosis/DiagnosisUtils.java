package org.openmrs.module.emrapi.diagnosis;

import org.openmrs.CodedOrFreeText;
import org.openmrs.ConditionVerificationStatus;

import java.util.ArrayList;
import java.util.List;

public class DiagnosisUtils {

    /**
     * Method to convert the core diagnosis object into a list of diagnoses compatible with the diagnosis object in the emrapi module
     * @return diagnoses
     * */
    public static List<Diagnosis> convert(List<org.openmrs.Diagnosis> coreDiagnoses) {
        List<Diagnosis> diagnoses = new ArrayList<>();
        for (org.openmrs.Diagnosis coreDiagnosis : coreDiagnoses) {
            diagnoses.add(convert(coreDiagnosis));
        }
        return diagnoses;
    }

    /**
     * Method to convert the core diagnosis object into a diagnosis object in the emrapi module
     * @return diagnoses
     * */
    public static Diagnosis convert(org.openmrs.Diagnosis coreDiagnosis) {
        Diagnosis diagnosis = new Diagnosis();
        CodedOrFreeText coded = coreDiagnosis.getDiagnosis();
        if (coded != null) {
            diagnosis.setDiagnosis(new CodedOrFreeTextAnswer(coded.getCoded(), coded.getSpecificName(), coded.getNonCoded()));
        }
        diagnosis.setCertainty(coreDiagnosis.getCertainty() == ConditionVerificationStatus.CONFIRMED ? Diagnosis.Certainty.CONFIRMED : Diagnosis.Certainty.PRESUMED);
        diagnosis.setOrder(coreDiagnosis.getRank() == 1 ? Diagnosis.Order.PRIMARY : Diagnosis.Order.SECONDARY);
        return diagnosis;
    }
}
