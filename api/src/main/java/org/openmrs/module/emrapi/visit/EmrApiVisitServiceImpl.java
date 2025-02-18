package org.openmrs.module.emrapi.visit;

import lombok.Setter;
import org.hibernate.ObjectNotFoundException;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.db.EmrApiDAO;
import org.springframework.stereotype.Service;

import java.util.List;

@Setter
@Service
public class EmrApiVisitServiceImpl extends BaseOpenmrsService implements EmrApiVisitService {

    private PatientService patientService;
    private EmrApiDAO emrApiDAO;

    @Override
    public List<VisitWithDiagnosesAndNotes> getVisitsWithNotesAndDiagnosesByPatient(String patientUuid, int startIndex, int limit) {
        
        if (patientUuid == null) {
            throw new APIException("Patient uuid is required");
        }
        
        Patient patient = patientService.getPatientByUuid(patientUuid);

        if (patient == null) {
            throw new APIException("No patient found with uuid " + patientUuid);
        }

        return emrApiDAO.getVisitsWithNotesAndDiagnosesByPatient(patient, startIndex, limit);
    }
}
