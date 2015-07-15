package org.openmrs.module.emrapi.patient;

import org.openmrs.Patient;
import org.openmrs.module.emrapi.person.image.PersonImage;

public class PatientProfile {

    private PersonImage personImage = new PersonImage();
    private Patient patient;

    public PatientProfile() {
    }

    public String getImage() {
        return personImage.getBase64EncodedImage();
    }

    public void setImage(String image) {
        personImage.setBase64EncodedImage(image);
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
        this.personImage.setPerson(patient);
    }

}
