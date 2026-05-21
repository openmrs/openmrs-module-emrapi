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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiProperties;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;

public class EmrPersonImageServiceImpl extends BaseOpenmrsService implements EmrPersonImageService {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	private static final String imageFormat = "jpeg";
	
	private EmrApiProperties emrApiProperties;
	
	@Override
	public PersonImage savePersonImage(PersonImage personImage) {
		Person person = personImage.getPerson();
		String base64EncodedImage = personImage.getBase64EncodedImage();
		
		if (base64EncodedImage == null || base64EncodedImage.isEmpty())
			return personImage;
		
		try {
			File imageFile = new File(String.format("%s/%s.%s", emrApiProperties.getPersonImageDirectory().getAbsolutePath(),
			    person.getUuid(), imageFormat));
			
			byte[] decodedBytes = Base64.getDecoder().decode(base64EncodedImage);
			BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(decodedBytes));
			ImageIO.write(bufferedImage, imageFormat, imageFile);
			bufferedImage.flush();
			
			personImage.setSavedImage(imageFile);
			log.info("Successfully created patient image at " + imageFile);
			
		}
		catch (Exception e) {
			log.error("Update patient image failed for : " + person);
			throw new APIException("Could not save patient image", e);
		}
		return personImage;
	}
	
	@Override
	public PersonImage getCurrentPersonImage(Person person) {
		File file = new File(String.format("%s/%s.%s", emrApiProperties.getPersonImageDirectory().getAbsolutePath(),
		    person.getUuid(), imageFormat));
		return new PersonImage(person, file);
	}
	
	public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
		this.emrApiProperties = emrApiProperties;
	}
	
}
