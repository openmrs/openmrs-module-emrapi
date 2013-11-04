/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.emrapi.person.image;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.emrapi.EmrApiProperties;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

public class EmrPersonImageServiceImpl extends BaseOpenmrsService implements EmrPersonImageService {

    protected final Log log = LogFactory.getLog(getClass());

    private static final String imageFormat = "jpeg";

    private EmrApiProperties emrApiProperties;

    @Override
    public PersonImage savePersonImage(PersonImage personImage) {
        Person person = personImage.getPerson();
        String base64EncodedImage = personImage.getBase64EncodedImage();

        if (base64EncodedImage == null || base64EncodedImage.isEmpty()) return personImage;

        try {
            File imageFile = new File(String.format("%s/%s.%s", emrApiProperties.getPersonImageDirectory().getAbsolutePath(), person.getUuid(), imageFormat));

            byte[] decodedBytes = DatatypeConverter.parseBase64Binary(base64EncodedImage);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(decodedBytes));
            ImageIO.write(bufferedImage, imageFormat, imageFile);
            bufferedImage.flush();

            personImage.setSavedImage(imageFile);
            log.info("Successfully created patient image at " + imageFile);

        } catch (Exception e) {
            log.error("Update patient image failed for : " + person);
            throw new APIException("Could not save patient image", e);
        }
        return personImage;
    }

    @Override
    public PersonImage getCurrentPersonImage(Person person) {
        File file = new File(String.format("%s/%s.%s", emrApiProperties.getPersonImageDirectory().getAbsolutePath(), person.getUuid(), imageFormat));
        return new PersonImage(person, file);
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

}
