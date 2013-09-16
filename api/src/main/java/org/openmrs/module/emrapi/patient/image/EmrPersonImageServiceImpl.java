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

package org.openmrs.module.emrapi.patient.image;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.ModuleException;
import org.openmrs.module.emrapi.EmrApiProperties;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.*;

public class EmrPersonImageServiceImpl extends BaseOpenmrsService implements EmrPersonImageService {

    private Log log = LogFactory.getLog(EmrPersonImageServiceImpl.class);

    private static final String imageFormat = "jpeg";

    private EmrApiProperties properties;

    public EmrPersonImageServiceImpl(EmrApiProperties properties) {
        this.properties = properties;
    }

    @Override
    public void save(String personUuid, String image) {
        try {
            if (image == null || image.isEmpty()) return;

            File outputFile = getImageFile(personUuid);
            log.info("Creating patient image at " + outputFile);
            byte[] decodedBytes = DatatypeConverter.parseBase64Binary(image);
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(decodedBytes));
            ImageIO.write(bufferedImage, imageFormat, outputFile);
            bufferedImage.flush();
            log.info("Successfully created patient image at " + outputFile);
        } catch (IOException e) {
            log.error("Update patient image failed for : " + personUuid);
            throw new ModuleException("[%s] : Could not save patient image", e);
        }

    }

    @Override
    public InputStream get(String personUuid) {
        try {
            return new FileInputStream(getImageFile(personUuid));
        } catch (FileNotFoundException e) {
            log.error("Get patient image failed for : " + personUuid);
            throw new ModuleException("[%s] : Could not load patient image", e);
        }
    }

    private File getImageFile(String personUuid) {
        return new File(String.format("%s/%s.%s", properties.getPersonImageDirectory(), personUuid, imageFormat));
    }
}
