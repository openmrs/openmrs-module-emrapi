package org.openmrs.module.emrapi.diagnosis;

import org.openmrs.CodedOrFreeText;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.api.impl.BaseOpenmrsService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * class implementing the DiagnosisService while delegating calls to the core module
 * */
public class DiagnosisServiceImpl2_2 extends BaseOpenmrsService implements DiagnosisService {

	/**
	 * Method to convert the core diagnosis object into a list of diagnoses compatible with the diagnosis object in the emrapi module
	 * @return diagnoses
	 * */
	private List<Diagnosis> convert(List<org.openmrs.Diagnosis> coreDiagnoses) {
		List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
		for (org.openmrs.Diagnosis coreDiagnosis : coreDiagnoses) {
			Diagnosis diagnosis = new Diagnosis();
			CodedOrFreeText coded = coreDiagnosis.getDiagnosis();
			diagnosis.setDiagnosis(new CodedOrFreeTextAnswer(coded.getCoded(), coded.getSpecificName(), coded.getNonCoded()));
			diagnosis.setCertainty(coreDiagnosis.getCertainty() == ConditionVerificationStatus.CONFIRMED ? Diagnosis.Certainty.CONFIRMED : Diagnosis.Certainty.PRESUMED);
			diagnosis.setOrder(coreDiagnosis.getRank() == 1 ? Diagnosis.Order.PRIMARY : Diagnosis.Order.SECONDARY);
			diagnoses.add(diagnosis);
		}
		return diagnoses;
	}

	public List<Diagnosis> getDiagnoses(Patient patient, Date fromDate) {
		 return convert(Context.getDiagnosisService().getDiagnoses(patient, fromDate));
	}


	public 	List<Diagnosis> getUniqueDiagnoses(Patient patient, Date fromDate){
		return convert(Context.getDiagnosisService().getUniqueDiagnoses(patient, fromDate));

	}

	public List<Diagnosis> getPrimaryDiagnoses(Encounter encounter){
		return convert(Context.getDiagnosisService().getPrimaryDiagnoses(encounter));
	}

	public boolean hasDiagnosis(Encounter encounter, Diagnosis diagnosis){
		return true;
	}

	public List<Obs> codeNonCodedDiagnosis(Obs nonCodedObs, List<Diagnosis> diagnoses){
		return null;
	}

}
