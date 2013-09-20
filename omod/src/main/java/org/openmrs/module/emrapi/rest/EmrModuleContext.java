package org.openmrs.module.emrapi.rest;

import org.openmrs.module.emrapi.person.image.EmrPersonImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmrModuleContext {

    private static EmrPersonImageService emrPersonImageService;

    @Autowired
    public void setEmrPersonImageService(EmrPersonImageService emrPersonImageService) {
        EmrModuleContext.emrPersonImageService = emrPersonImageService;
    }

    public static EmrPersonImageService getEmrPersonImageService() {
        return EmrModuleContext.emrPersonImageService;
    }
}
