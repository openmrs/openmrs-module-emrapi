/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.emrapi.patient;

import org.openmrs.Patient;
import org.openmrs.Relationship;
import org.openmrs.module.emrapi.person.image.PersonImage;

import java.util.List;

public class PatientProfile {
	
	private PersonImage personImage = new PersonImage();
	
	private Patient patient;
	
	private List<Relationship> relationships;
	
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
	
	public List<Relationship> getRelationships() {
		return relationships;
	}
	
	public void setRelationships(List<Relationship> relationships) {
		this.relationships = relationships;
	}
	
}
