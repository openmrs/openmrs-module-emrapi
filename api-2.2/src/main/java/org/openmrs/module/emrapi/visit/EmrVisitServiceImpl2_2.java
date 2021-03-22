package org.openmrs.module.emrapi.visit;

import org.openmrs.*;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.VisitService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.diagnosis.*;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;

import java.util.ArrayList;
import java.util.List;

public class EmrVisitServiceImpl2_2 extends EmrVisitServiceImpl implements EmrVisitService {

	private AdministrationService adminService;

	private EmrDiagnosisDAO emrDiagnosisDAO;

	private DiagnosisConverter diagnosisConverter;

	public void setAdminService(AdministrationService adminService) {
		this.adminService = adminService;
	}

	public void setEmrDiagnosisDAO(EmrDiagnosisDAO emrDiagnosisDAO) {
		this.emrDiagnosisDAO = emrDiagnosisDAO;
	}

	public void setDiagnosisConverter(DiagnosisConverter diagnosisConverter) {
		this.diagnosisConverter = diagnosisConverter;
	}

	public EmrVisitServiceImpl2_2(VisitService visitService, VisitResponseMapper visitResponseMapper) {
		super(visitService, visitResponseMapper);
	}

	@Override
	public List<Obs> getDiagnoses(Visit visit, DiagnosisMetadata diagnosisMetadata, Boolean primaryOnly, Boolean confirmedOnly) {
		if (adminService.getGlobalProperty(EmrApiConstants.GP_USE_LEGACY_DIAGNOSIS_SERVICE, "false").equalsIgnoreCase("true")) {
			return super.getDiagnoses(visit, diagnosisMetadata, primaryOnly, confirmedOnly);
		} else {
			List<org.openmrs.Diagnosis> diagnoses = emrDiagnosisDAO.getDiagnoses(visit, primaryOnly, confirmedOnly);
			List<Obs> diagnosisList = new ArrayList<Obs>();
			for (Diagnosis diagnosis : diagnosisConverter.convert(diagnoses)) {
				diagnosisList.add(diagnosisMetadata.buildDiagnosisObsGroup(diagnosis));
			}
			return diagnosisList;
		}
	}
}
