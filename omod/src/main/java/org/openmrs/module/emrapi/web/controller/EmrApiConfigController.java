package org.openmrs.module.emrapi.web.controller;

import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/emrapi/configuration")
public class EmrApiConfigController {

    @Autowired
    private EmrApiProperties emrApiProperties;
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public SimpleObject getEmrApiConfig() {
        SimpleObject response = new SimpleObject();
        response.put("admissionEncounterType",  emrApiProperties.getAdmissionEncounterType().getUuid());
        response.put("transferWithinHospitalEncounterType",  emrApiProperties.getTransferWithinHospitalEncounterType().getUuid());
        response.put("exitFromInpatientEncounterTpye",  emrApiProperties.getExitFromInpatientEncounterType().getUuid());
        return response;
    }

}
