package org.openmrs.module.emrapi.rest;

import org.openmrs.module.emrapi.patient.EmrPatientProfileService;
import org.openmrs.module.emrapi.person.image.EmrPersonImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmrModuleContext {

    private static EmrPersonImageService emrPersonImageService;
    private static EmrPatientProfileService emrPatientProfileService;
    
    @Autowired
    public void setEmrPersonImageService(EmrPersonImageService emrPersonImageService, EmrPatientProfileService emrPatientProfileService) {
        EmrModuleContext.emrPersonImageService = emrPersonImageService;
        EmrModuleContext.emrPatientProfileService = emrPatientProfileService;
    }

    public static EmrPersonImageService getEmrPersonImageService() {
        return EmrModuleContext.emrPersonImageService;
    }

    public static EmrPatientProfileService getEmrPatientProfileService() {
        return EmrModuleContext.emrPatientProfileService;
    }

}
