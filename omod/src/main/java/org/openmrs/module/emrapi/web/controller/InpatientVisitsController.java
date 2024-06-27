package org.openmrs.module.emrapi.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Location;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.adt.AdtAction;
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

    @RequestMapping(method = RequestMethod.GET, value = "/admissionRequests")
    @ResponseBody
    public List<SimpleObject> getVisitsAwaitingAdmission(@RequestParam("admissionLocation") Location admissionLocation) {
        return getVisitsAwaitingAdmissionHelper(admissionLocation);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/transferRequests")
    @ResponseBody
    public List<SimpleObject> getVisitsAwaitingTransfer(@RequestParam("transferLocation") Location transferLocation) {
        return getVisitsAwaitingTransferHelper(transferLocation);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/admissionAndTransferRequests")
    @ResponseBody
    public List<SimpleObject> getVisitsAwaitingAdminstOrTransfer(@RequestParam("location") Location location) {
        List<SimpleObject> response = getVisitsAwaitingAdmissionHelper(location);
        response.addAll(getVisitsAwaitingTransferHelper(location));
        return response;
    }

    private List<SimpleObject> getVisitsAwaitingAdmissionHelper(Location admissionLocation) {
        // TODO note also that this service method does *not* actually limit by admission location; we will need to expand the underlying service method/hql query to do this, see: https://openmrs.atlassian.net/browse/O3-3464
        List<Visit> visits = adtService.getVisitsAwaitingAdmission(admissionLocation, null, null);
        List<SimpleObject> visitObjects = new ArrayList<SimpleObject>();
        for (Visit visit : visits) {
            SimpleObject inpatientVisit = new SimpleObject();
            inpatientVisit.put("visit", ConversionUtil.convertToRepresentation(visit, Representation.DEFAULT));
            inpatientVisit.put("patient", ConversionUtil.convertToRepresentation(visit.getPatient(), Representation.DEFAULT));
            inpatientVisit.put("type", AdtAction.Type.ADMISSION);
            visitObjects.add(inpatientVisit);
        }
        return visitObjects;
    }

    private List<SimpleObject> getVisitsAwaitingTransferHelper(Location transferLocation) {
        List<Visit> visits = adtService.getVisitsAwaitingTransfer(transferLocation);
        List<SimpleObject> visitObjects = new ArrayList<SimpleObject>();
        for (Visit visit : visits) {
            SimpleObject inpatientVisit = new SimpleObject();
            inpatientVisit.put("visit", ConversionUtil.convertToRepresentation(visit, Representation.DEFAULT));
            inpatientVisit.put("patient", ConversionUtil.convertToRepresentation(visit.getPatient(), Representation.DEFAULT));
            inpatientVisit.put("type", AdtAction.Type.TRANSFER);
            visitObjects.add(inpatientVisit);
        }
        return visitObjects;
    }

}



