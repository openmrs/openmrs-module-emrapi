package org.openmrs.module.emrapi.web.controller;

import org.openmrs.Location;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/**/emrapi/inpatient")
@Deprecated
public class InpatientVisitsController {

    @Autowired
    private AdtService adtService;

    @RequestMapping(method = RequestMethod.GET, value = "/visits")
    @ResponseBody
    public List<SimpleObject> getInpatientVisits(@RequestParam(value = "currentLocation") Location currentLocation) {

        if (currentLocation == null) {
            throw new IllegalArgumentException("currentLocation is required");
        }

        List<VisitDomainWrapper> visits = adtService.getInpatientVisits(adtService.getLocationThatSupportsVisits(currentLocation), currentLocation);
        List<SimpleObject> response = new ArrayList<SimpleObject>();

        if (visits == null) {
            return response;
        }

        for (VisitDomainWrapper visit : visits) {
            SimpleObject inpatientVisit = new SimpleObject();
            inpatientVisit.put("visit", ConversionUtil.convertToRepresentation(visit.getVisit(), Representation.DEFAULT));
            inpatientVisit.put("patient", ConversionUtil.convertToRepresentation(visit.getVisit().getPatient(), Representation.DEFAULT));
            inpatientVisit.put("currentLocation", ConversionUtil.convertToRepresentation(currentLocation, Representation.DEFAULT));
            inpatientVisit.put("timeSinceAdmissionInMinutes",  visit.getTimeSinceAdmissionInMinutes());
            inpatientVisit.put("timeAtInpatientLocationInMinutes", visit.getTimeAtCurrentInpatientLocationInMinutes());
            response.add(inpatientVisit);
        }

        return response;
    }
}



