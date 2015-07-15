package org.openmrs.module.emrapi.patient;

import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.emrapi.person.image.EmrPersonImageService;
import org.openmrs.module.emrapi.person.image.PersonImage;

public class EmrPatientProfileServiceImpl implements EmrPatientProfileService {

    private PatientService patientService;
    private EmrPersonImageService emrPersonImageService;
    
    @Override
    public PatientProfile save(PatientProfile patientProfile) {
        Patient patient = patientService.savePatient(patientProfile.getPatient());
        patientProfile.setPatient(patient);

        PersonImage personImage = new PersonImage();
        personImage.setPerson(patient);
        personImage.setBase64EncodedImage(patientProfile.getImage());
        
        emrPersonImageService.savePersonImage(personImage);
        return patientProfile;
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public void setEmrPersonImageService(EmrPersonImageService emrPersonImageService) {
        this.emrPersonImageService = emrPersonImageService;
    }
}
