package org.openmrs.module.emrapi.diagnosis;

import org.openmrs.CodedOrFreeText;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Encounter;
import org.openmrs.Obs;

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

    /**
     * Method to convert an emrapi diagnosis object into a core diagnosis object
     * @return a core diagnosis representation of an emrapi diagnosis
     */
    public static org.openmrs.Diagnosis convert(Diagnosis emrApiDiagnosis) {
        org.openmrs.Diagnosis coreDiagnosis = new org.openmrs.Diagnosis();
        CodedOrFreeText diagnosis = new CodedOrFreeText();
        if (emrApiDiagnosis.getDiagnosis() != null) {
            diagnosis.setCoded(emrApiDiagnosis.getDiagnosis().getCodedAnswer());
            diagnosis.setSpecificName(emrApiDiagnosis.getDiagnosis().getSpecificCodedAnswer());
            diagnosis.setNonCoded(emrApiDiagnosis.getDiagnosis().getNonCodedAnswer());
        }
        if (emrApiDiagnosis.getCertainty() != null) {
            if (emrApiDiagnosis.getCertainty() == Diagnosis.Certainty.CONFIRMED) {
                coreDiagnosis.setCertainty(ConditionVerificationStatus.CONFIRMED);
            }
            else if (emrApiDiagnosis.getCertainty() == Diagnosis.Certainty.PRESUMED) {
                coreDiagnosis.setCertainty(ConditionVerificationStatus.PROVISIONAL);
            }
        }
        if (emrApiDiagnosis.getOrder() != null) {
            if (emrApiDiagnosis.getOrder() == Diagnosis.Order.PRIMARY) {
                coreDiagnosis.setRank(1);
            }
            else if (emrApiDiagnosis.getOrder() == Diagnosis.Order.SECONDARY) {
                coreDiagnosis.setRank(2);
            }
        }
        Obs diagnosisObs = emrApiDiagnosis.getExistingObs();
        if (diagnosisObs != null) {
            Encounter encounter = diagnosisObs.getEncounter();
            coreDiagnosis.setEncounter(encounter);
            coreDiagnosis.setPatient(encounter.getPatient());
            coreDiagnosis.setUuid(diagnosisObs.getUuid());
            coreDiagnosis.setCreator(diagnosisObs.getCreator());
            coreDiagnosis.setDateCreated(diagnosisObs.getDateCreated());
            coreDiagnosis.setVoided(diagnosisObs.getVoided());
            coreDiagnosis.setDateVoided(diagnosisObs.getDateVoided());
            coreDiagnosis.setVoidedBy(diagnosisObs.getVoidedBy());
            coreDiagnosis.setVoidReason(diagnosisObs.getVoidReason());
        }

        return coreDiagnosis;
    }
}
