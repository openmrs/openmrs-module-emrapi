package org.openmrs.module.emrapi.person.image;

import org.openmrs.Person;

import java.io.File;

public class PersonImage {

    private Person person;
    private String base64EncodedImage;
    private File savedImage;

    public PersonImage() {
    }

    public PersonImage(Person person, File savedImage) {
        this.person = person;
        this.savedImage = savedImage;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public String getBase64EncodedImage() {
        return base64EncodedImage;
    }

    public void setBase64EncodedImage(String base64EncodedImage) {
        this.base64EncodedImage = base64EncodedImage;
    }

    public File getSavedImage() {
        return savedImage;
    }

    public void setSavedImage(File savedImage) {
        this.savedImage = savedImage;
    }
}
