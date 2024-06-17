package org.openmrs.module.emrapi.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.openmrs.Location;
import org.openmrs.Visit;
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

@Controller
@RequestMapping(value = "/rest/emrapi/inpatient")
public class InpatientVisitsController {

    @Autowired
    private AdtService adtService;

    @RequestMapping(method = RequestMethod.GET, value = "/visits")
    @ResponseBody
    public List<SimpleObject> getInpatientVisits(@RequestParam(value = "currentLocation") Location currentLocation) {

        // TODO expand to allow null current location

        // TODO handle null response if possible
        // TODO this getInpatentVisits method is almost certainly not performant enough for production use and will likely need to be refactored into a HQL query
        List<VisitDomainWrapper> visits = adtService.getInpatientVisits(adtService.getLocationThatSupportsVisits(currentLocation), currentLocation);

        // TODO type this?
        List<SimpleObject> response = new ArrayList<SimpleObject>();

        for (VisitDomainWrapper visit : visits) {
            SimpleObject inpatientVisit = new SimpleObject();
            inpatientVisit.put("visit", ConversionUtil.convertToRepresentation(visit.getVisit(), Representation.DEFAULT));
            inpatientVisit.put("patient", ConversionUtil.convertToRepresentation(visit.getVisit().getPatient(), Representation.DEFAULT));
            inpatientVisit.put("currentLocation", ConversionUtil.convertToRepresentation(currentLocation, Representation.DEFAULT));
            inpatientVisit.put("timeSinceAdmissionInMinutes",  Minutes.minutesBetween(new DateTime(visit.getAdmissionEncounter().getEncounterDatetime()), new DateTime()).getMinutes());
            inpatientVisit.put("timeOnWardInMinutes",  Minutes.minutesBetween(new DateTime(visit.getLatestAdtEncounter().getEncounterDatetime()), new DateTime()).getMinutes());  // TODO: assumption, an ADT ecounter always results in change of ward?
            response.add(inpatientVisit);
        }

        return response;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admissionRequests")
    @ResponseBody
    public List<SimpleObject> getVisitsAwaitingAdmission(@RequestParam("admissionLocation") Location admissionLocation) {

        // TODO note that this service method *only* returns admission requests, while we will need to expand this to include transfer requests (which will be slightly non-trivial)
        // TODO note also that this service method does *not* actually limit by admission location; we will need to expand the underlying service method/hql query to do this
        List<Visit> visits = adtService.getVisitsAwaitingAdmission(admissionLocation, null, null);
        List<SimpleObject> response = new ArrayList<SimpleObject>();
        for (Visit visit : visits) {
            SimpleObject inpatientVisit = new SimpleObject();
            inpatientVisit.put("visit", ConversionUtil.convertToRepresentation(visit, Representation.DEFAULT));
            inpatientVisit.put("patient", ConversionUtil.convertToRepresentation(visit.getPatient(), Representation.DEFAULT));
            response.add(inpatientVisit);
        }
        return response;
    }
}



