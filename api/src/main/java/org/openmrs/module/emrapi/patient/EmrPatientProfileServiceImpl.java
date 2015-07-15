package org.openmrs.module.emrapi.patient;

import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.module.emrapi.person.image.EmrPersonImageService;
import org.openmrs.module.emrapi.person.image.PersonImage;

import java.util.List;

public class EmrPatientProfileServiceImpl implements EmrPatientProfileService {

    private PatientService patientService;
    private PersonService personService;
    private EmrPersonImageService emrPersonImageService;
    
    @Override
    public PatientProfile save(PatientProfile patientProfile) {
        Patient patient = patientService.savePatient(patientProfile.getPatient());

        saveRelationships(patientProfile.getRelationships());

        patientProfile.setPatient(patient);

        PersonImage personImage = new PersonImage();
        personImage.setPerson(patient);
        personImage.setBase64EncodedImage(patientProfile.getImage());

        emrPersonImageService.savePersonImage(personImage);
        return patientProfile;
    }

    @Override
    public PatientProfile get(String patientUuid) {
        PatientProfile delegate = new PatientProfile();

        Patient patient = patientService.getPatientByUuid(patientUuid);
        delegate.setPatient(patient);

        Person person = personService.getPerson(patient.getPersonId());
        List<Relationship> relationships = personService.getRelationshipsByPerson(person);
        delegate.setRelationships(relationships);

        return delegate;
    }

    private void saveRelationships(List<Relationship> relationships) {
        for(Relationship relationship: relationships) {
            personService.saveRelationship(relationship);
        }
    }

    public void setPatientService(PatientService patientService) {
        this.patientService = patientService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setEmrPersonImageService(EmrPersonImageService emrPersonImageService) {
        this.emrPersonImageService = emrPersonImageService;
    }
}
