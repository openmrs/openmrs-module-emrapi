/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
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
