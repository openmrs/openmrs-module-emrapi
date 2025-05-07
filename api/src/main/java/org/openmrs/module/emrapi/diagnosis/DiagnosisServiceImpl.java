package org.openmrs.module.emrapi.diagnosis;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiConstants;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * class implementing the DiagnosisService while delegating calls to the core module
 * */
public class DiagnosisServiceImpl extends BaseOpenmrsService implements DiagnosisService {

	private CoreDiagnosisService coreDiagnosisService;

	private ObsGroupDiagnosisService obsGroupDiagnosisService;

	private AdministrationService adminService;

	public void setCoreDiagnosisService(CoreDiagnosisService coreDiagnosisService) {
		this.coreDiagnosisService = coreDiagnosisService;
	}

	public void setObsGroupDiagnosisService(ObsGroupDiagnosisService obsGroupDiagnosisService) {
		this.obsGroupDiagnosisService = obsGroupDiagnosisService;
	}

	public void setAdminService(AdministrationService adminService) {
		this.adminService = adminService;
	}

	protected boolean useDiagnosesAsObs() {
		return adminService.getGlobalProperty(EmrApiConstants.GP_USE_LEGACY_DIAGNOSIS_SERVICE, "false").equalsIgnoreCase("true");
	}

	public List<Diagnosis> getDiagnoses(Patient patient, Date fromDate) {
		if (useDiagnosesAsObs()) {
			return obsGroupDiagnosisService.getDiagnoses(patient, fromDate);
		}
		 else {
			return coreDiagnosisService.getDiagnoses(patient, fromDate);
		}
	}

	public 	List<Diagnosis> getUniqueDiagnoses(Patient patient, Date fromDate) {
		if (useDiagnosesAsObs()) {
			return obsGroupDiagnosisService.getUniqueDiagnoses(patient, fromDate);
		}
		else {
			return coreDiagnosisService.getUniqueDiagnoses(patient, fromDate);
		}

	}

	public List<Diagnosis> getPrimaryDiagnoses(Encounter encounter) {
		if (useDiagnosesAsObs()) {
			return obsGroupDiagnosisService.getPrimaryDiagnoses(encounter);
		}
		else {
			return coreDiagnosisService.getPrimaryDiagnoses(encounter);
		}
	}

	public boolean hasDiagnosis(Encounter encounter, Diagnosis diagnosis) {
		if (useDiagnosesAsObs()) {
			return obsGroupDiagnosisService.hasDiagnosis(encounter, diagnosis);
		}
		else {
			return coreDiagnosisService.hasDiagnosis(encounter, diagnosis);
		}
	}

	public List<Obs> codeNonCodedDiagnosis(Obs nonCodedObs, List<Diagnosis> diagnoses) {
		if (useDiagnosesAsObs()) {
			return obsGroupDiagnosisService.codeNonCodedDiagnosis(nonCodedObs, diagnoses);
		}
		else {
			return coreDiagnosisService.codeNonCodedDiagnosis(nonCodedObs, diagnoses);
		}
	}

	public Map<Visit, List<org.openmrs.Diagnosis>> getDiagnoses(Collection<Visit> visits) {
		if (useDiagnosesAsObs()) {
			return obsGroupDiagnosisService.getDiagnoses(visits);
		}
		else {
			return coreDiagnosisService.getDiagnoses(visits);
		}
	}

	public List<Obs> getDiagnosesAsObs(Visit visit, DiagnosisMetadata diagnosisMetadata, Boolean primaryOnly, Boolean confirmedOnly) {
		if (useDiagnosesAsObs()) {
			return obsGroupDiagnosisService.getDiagnosesAsObs(visit, diagnosisMetadata, primaryOnly, confirmedOnly);
		}
		else {
			return coreDiagnosisService.getDiagnosesAsObs(visit, diagnosisMetadata, primaryOnly, confirmedOnly);
		}
	}

}
