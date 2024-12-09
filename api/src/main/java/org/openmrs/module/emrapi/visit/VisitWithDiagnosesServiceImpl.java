package org.openmrs.module.emrapi.visit;

import org.hibernate.ObjectNotFoundException;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.db.VisitDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisitWithDiagnosesServiceImpl extends BaseOpenmrsService implements VisitWithDiagnosesService {

    @Autowired
    PatientService patientService;

    @Autowired
    VisitDAO visitDAO;

    @Override
    public List<VisitWithDiagnoses> getVisitsByPatientId(String patientUuid, int startIndex, int limit) {

        Patient patient = patientService.getPatientByUuid(patientUuid);

        if (patient == null) {
            throw new ObjectNotFoundException("No patient found with uuid " + patientUuid, Patient.class.getName());
        }

        return visitDAO.getVisitsWithNotesAndDiagnosesByPatient(patient, startIndex, limit);
    }
}
