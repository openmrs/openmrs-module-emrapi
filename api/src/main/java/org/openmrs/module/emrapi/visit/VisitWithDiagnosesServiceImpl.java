package org.openmrs.module.emrapi.visit;

import org.hibernate.ObjectNotFoundException;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.db.EmrApiDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VisitWithDiagnosesServiceImpl extends BaseOpenmrsService implements VisitWithDiagnosesService {

    @Autowired
    PatientService patientService;

    @Autowired
    EmrApiDAO emrApiDAO;

    @Override
    public List<VisitWithDiagnoses> getVisitsWithNotesAndDiagnosesByPatient(String patientUuid, int startIndex, int limit) {

        Patient patient = patientService.getPatientByUuid(patientUuid);

        if (patient == null) {
            throw new ObjectNotFoundException("No patient found with uuid " + patientUuid, Patient.class.getName());
        }

        return emrApiDAO.getVisitsWithNotesAndDiagnosesByPatient(patient, startIndex, limit);
    }
}
