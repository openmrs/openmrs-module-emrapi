package org.openmrs.module.emrapi.visit;

import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.db.VisitDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisitWithDiagnosesServiceImpl extends BaseOpenmrsService implements VisitWithDiagnosesService {
    // Existing methods and dependencies

    @Autowired
    PatientService patientService;

    @Autowired
    VisitDAO visitDAO;

    @Override
    public List<VisitWithDiagnoses> getVisitsByPatientId(Integer patientId) {

        Patient patient = patientService.getPatient(patientId);

        return visitDAO.getVisitsByPatientId(patient);
//        return null;
    }
}