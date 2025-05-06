package org.openmrs.module.emrapi.diagnosis;

import lombok.Setter;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.db.EmrApiDAO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * class implementing the DiagnosisService while delegating calls to the core module
 * */
@Setter
public class CoreDiagnosisService {

	private EmrDiagnosisDAO emrDiagnosisDAO;

	EmrApiDAO emrApiDAO;

	public List<Diagnosis> getDiagnoses(Patient patient, Date fromDate) {
		return DiagnosisUtils.convert(Context.getDiagnosisService().getDiagnoses(patient, fromDate));
	}

	public 	List<Diagnosis> getUniqueDiagnoses(Patient patient, Date fromDate){
		return DiagnosisUtils.convert(Context.getDiagnosisService().getUniqueDiagnoses(patient, fromDate));
	}

	public List<Diagnosis> getPrimaryDiagnoses(Encounter encounter){
		return DiagnosisUtils.convert(Context.getDiagnosisService().getPrimaryDiagnoses(encounter));
	}

	public boolean hasDiagnosis(Encounter encounter, Diagnosis diagnosis){
		return true;
	}

	public List<Obs> codeNonCodedDiagnosis(Obs nonCodedObs, List<Diagnosis> diagnoses){
		return null;
	}

	public List<Obs> getDiagnosesAsObs(Visit visit, DiagnosisMetadata diagnosisMetadata, Boolean primaryOnly, Boolean confirmedOnly) {
		List<org.openmrs.Diagnosis> diagnoses = emrDiagnosisDAO.getDiagnoses(visit, primaryOnly, confirmedOnly);
		List<Obs> diagnosisList = new ArrayList<Obs>();
		for (Diagnosis diagnosis : DiagnosisUtils.convert(diagnoses)) {
			diagnosisList.add(diagnosisMetadata.buildDiagnosisObsGroup(diagnosis));
		}
		return diagnosisList;
	}

	public Map<Visit, List<org.openmrs.Diagnosis>> getDiagnoses(Collection<Visit> visits) {
		Map<Visit, List<org.openmrs.Diagnosis>> ret = new HashMap<>();
        String query =
				"select distinct diag FROM Diagnosis diag " +
                "where diag.encounter.visit in :visits " +
                "and diag.voided = false " +
                "order by diag.rank";
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("visits", visits);
		List<org.openmrs.Diagnosis> diagnoses = emrApiDAO.executeHql(query, parameters, org.openmrs.Diagnosis.class);
		for (Visit visit : visits) {
			ret.put(visit, new ArrayList<>());
		}
		for (org.openmrs.Diagnosis diagnosis : diagnoses) {
			ret.get(diagnosis.getEncounter().getVisit()).add(diagnosis);
		}
		return ret;
	}
}
